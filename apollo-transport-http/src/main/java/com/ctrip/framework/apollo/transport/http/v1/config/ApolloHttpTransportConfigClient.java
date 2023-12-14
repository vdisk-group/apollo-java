/*
 * Copyright 2022 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.transport.http.v1.config;

import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.core.http.ApolloCoreHttpClient;
import com.ctrip.framework.apollo.core.http.ApolloHttpException;
import com.ctrip.framework.apollo.core.http.ApolloHttpRequest;
import com.ctrip.framework.apollo.core.http.ApolloHttpResponse;
import com.ctrip.framework.apollo.core.http.ApolloHttpStatusCodeException;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.ctrip.framework.apollo.core.utils.CollectionUtil;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigClient;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigException;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigNotFoundException;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportEndpoint;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportGetConfigRequest;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportGetConfigResponse;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportGetConfigResult;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportGetConfigStatus;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportNotificationDefinition;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportNotificationMessages;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportNotificationResult;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportWatchNotificationsRequest;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportWatchNotificationsResponse;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportWatchNotificationsStatus;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApolloHttpTransportConfigClient implements ApolloTransportConfigClient {

  private static final Gson GSON = new Gson();

  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");

  private static final Escaper QUERY_PARAM_ESCAPER = UrlEscapers.urlFormParameterEscaper();

  private static final Type WATCH_NOTIFICATIONS_RESPONSE_TYPE = new TypeToken<List<ApolloConfigNotification>>() {
  }.getType();

  private final ApolloCoreHttpClient apolloCoreHttpClient;

  public ApolloHttpTransportConfigClient(ApolloCoreHttpClient apolloCoreHttpClient) {
    this.apolloCoreHttpClient = apolloCoreHttpClient;
  }

  @Override
  public ApolloTransportWatchNotificationsResponse watchNotifications(
      ApolloTransportEndpoint endpoint, ApolloTransportWatchNotificationsRequest request)
      throws ApolloTransportConfigException {
    ApolloHttpRequest httpRequest = this.toWatchNotificationsHttpRequest(endpoint, request);
    ApolloHttpResponse<List<ApolloConfigNotification>> httpResponse = this.doGet(
        "Watch notifications",
        httpRequest, WATCH_NOTIFICATIONS_RESPONSE_TYPE);

    return this.toWatchNotificationsResponse(httpResponse);
  }

  private ApolloTransportWatchNotificationsResponse toWatchNotificationsResponse(
      ApolloHttpResponse<List<ApolloConfigNotification>> httpResponse) {
    List<ApolloConfigNotification> httpNotifications = httpResponse.getBody();
    if (httpResponse.getStatusCode() == 304) {
      return ApolloTransportWatchNotificationsResponse.builder()
          .status(ApolloTransportWatchNotificationsStatus.NOT_MODIFIED)
          .notifications(Collections.emptyList())
          .build();
    }

    List<ApolloTransportNotificationResult> notifications = this.toNotificationResults(
        httpNotifications);
    return ApolloTransportWatchNotificationsResponse.builder()
        .status(ApolloTransportWatchNotificationsStatus.OK)
        .notifications(notifications)
        .build();
  }

  private List<ApolloTransportNotificationResult> toNotificationResults(
      List<ApolloConfigNotification> httpNotifications) {
    if (CollectionUtil.isEmpty(httpNotifications)) {
      return Collections.emptyList();
    }
    List<ApolloTransportNotificationResult> notifications = new ArrayList<>(
        httpNotifications.size());
    for (ApolloConfigNotification httpNotification : httpNotifications) {
      ApolloNotificationMessages httpNotificationMessages = httpNotification.getMessages();
      ApolloTransportNotificationMessages messages = this.toNotificationMessages(
          httpNotificationMessages);
      ApolloTransportNotificationResult result = ApolloTransportNotificationResult.builder()
          .namespaceName(httpNotification.getNamespaceName())
          .notificationId(httpNotification.getNotificationId())
          .messages(messages)
          .build();
      notifications.add(result);
    }
    return notifications;
  }

  private ApolloTransportNotificationMessages toNotificationMessages(
      ApolloNotificationMessages httpNotificationMessages) {
    Map<String, Long> details =
        httpNotificationMessages != null ? httpNotificationMessages.getDetails() : null;
    if (CollectionUtil.isEmpty(details)) {
      return null;
    }
    return ApolloTransportNotificationMessages.builder()
        .details(details)
        .build();
  }

  private ApolloHttpRequest toWatchNotificationsHttpRequest(ApolloTransportEndpoint endpoint,
      ApolloTransportWatchNotificationsRequest request) {
    Map<String, String> queryParams = new LinkedHashMap<>();
    queryParams.put("appId", request.getAppId());
    queryParams.put("cluster", request.getCluster());
    queryParams.put("notifications", this.assembleNotifications(request.getNotifications()));

    String dataCenter = request.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      queryParams.put("dataCenter", dataCenter);
    }
    String clientIp = request.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }
    String actualAddress = this.getActualAddress(endpoint);
    String query = this.toQueryString(queryParams);
    String uri = MessageFormat.format("{0}/notifications/v2{1}", actualAddress, query);

    ApolloHttpRequest httpRequest = new ApolloHttpRequest(uri);

    this.signWatchNotifications(uri, request, httpRequest);

    return httpRequest;
  }

  private void signWatchNotifications(String uri, ApolloTransportWatchNotificationsRequest request,
      ApolloHttpRequest httpRequest) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, httpRequest);
  }

  private void signHttpRequest(String uri, String appId, String accessKeySecret,
      ApolloHttpRequest httpRequest) {
    if (!StringUtils.isBlank(accessKeySecret)) {
      Map<String, String> headers = Maps.newLinkedHashMap();
      Map<String, String> authorizationHeaders = Signature.buildHttpHeaders(uri, appId,
          accessKeySecret);
      headers.putAll(authorizationHeaders);
      httpRequest.setHeaders(headers);
    }
  }

  private String getActualAddress(ApolloTransportEndpoint endpoint) {
    String address = endpoint.getAddress();
    if (address.endsWith("/")) {
      return address.substring(0, address.length() - 1);
    } else {
      return address;
    }
  }

  String assembleNotifications(List<ApolloTransportNotificationDefinition> notifications) {
    List<ApolloConfigNotification> httpNotifications = new ArrayList<>(notifications.size());
    for (ApolloTransportNotificationDefinition notification : notifications) {
      ApolloConfigNotification httpNotification = new ApolloConfigNotification(
          notification.getNamespaceName(),
          notification.getNotificationId());
      httpNotifications.add(httpNotification);
    }
    return GSON.toJson(httpNotifications);
  }

  private String toQueryString(Map<String, String> queryParams) {
    if (CollectionUtil.isEmpty(queryParams)) {
      return "";
    }
    Map<String, String> escapedQueryParams = Maps.newLinkedHashMapWithExpectedSize(
        queryParams.size());
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      escapedQueryParams.put(key, QUERY_PARAM_ESCAPER.escape(value));
    }
    return "?" + MAP_JOINER.join(escapedQueryParams);
  }

  private <T> ApolloHttpResponse<T> doGet(String scene,
      ApolloHttpRequest httpRequest, Type responseType) {
    ApolloHttpResponse<T> httpResponse;
    try {
      httpResponse = this.apolloCoreHttpClient.doGet(
          httpRequest, responseType);
    } catch (ApolloHttpStatusCodeException e) {
      if (e.getStatusCode() == 404) {
        throw new ApolloTransportConfigNotFoundException();
      } else {
        throw new ApolloTransportConfigException(
            MessageFormat.format("{0} failed. Http status code: {1}",
                scene, e.getStatusCode()),
            e);
      }
    } catch (ApolloHttpException e) {
      throw new ApolloTransportConfigException(
          MessageFormat.format("{0} failed. Http error message: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new ApolloTransportConfigException(
          MessageFormat.format("{0} failed. Error message: {1}",
              scene, e.getLocalizedMessage()), e);
    }
    return httpResponse;
  }

  @Override
  public ApolloTransportGetConfigResponse getConfig(ApolloTransportEndpoint endpoint,
      ApolloTransportGetConfigRequest request)
      throws ApolloTransportConfigException, ApolloTransportConfigNotFoundException {

    ApolloHttpRequest httpRequest = this.toGetConfigHttpRequest(endpoint, request);

    ApolloHttpResponse<ApolloConfig> httpResponse = this.doGet("Get config", httpRequest,
        ApolloConfig.class);

    ApolloConfig apolloConfig = httpResponse.getBody();
    if (httpResponse.getStatusCode() == 304) {
      return ApolloTransportGetConfigResponse.builder()
          .status(ApolloTransportGetConfigStatus.NOT_MODIFIED)
          .build();
    }
    ApolloTransportGetConfigResult configResult = this.toGetConfigResult(apolloConfig);
    return ApolloTransportGetConfigResponse.builder()
        .status(ApolloTransportGetConfigStatus.OK)
        .config(configResult)
        .build();
  }

  private ApolloTransportGetConfigResult toGetConfigResult(
      ApolloConfig apolloConfig) {
    if (apolloConfig == null) {
      return null;
    }
    return ApolloTransportGetConfigResult.builder()
        .appId(apolloConfig.getAppId())
        .cluster(apolloConfig.getCluster())
        .namespaceName(apolloConfig.getNamespaceName())
        .releaseKey(apolloConfig.getReleaseKey())
        .configurations(apolloConfig.getConfigurations())
        .build();
  }

  private ApolloHttpRequest toGetConfigHttpRequest(ApolloTransportEndpoint endpoint,
      ApolloTransportGetConfigRequest request) {
    Map<String, String> queryParams = new LinkedHashMap<>();

    String releaseKey = request.getReleaseKey();
    if (!StringUtils.isEmpty(releaseKey)) {
      queryParams.put("releaseKey", releaseKey);
    }

    String dataCenter = request.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      queryParams.put("dataCenter", dataCenter);
    }

    String clientIp = request.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }

    String label = request.getLabel();
    if (!StringUtils.isEmpty(label)) {
      queryParams.put("label", label);
    }

    ApolloTransportNotificationMessages messages = request.getMessages();
    if (messages != null) {
      queryParams.put("messages", this.assembleMessages(messages));
    }

    String actualAddress = this.getActualAddress(endpoint);

    String query = this.toQueryString(queryParams);

    String uri = MessageFormat.format("{0}/configs/{1}/{2}/{3}{4}", actualAddress,
        request.getAppId(),
        request.getCluster(), request.getNamespace(), query);

    ApolloHttpRequest httpRequest = new ApolloHttpRequest(uri);

    this.signGetConfig(uri, request, httpRequest);

    return httpRequest;
  }

  private void signGetConfig(String uri, ApolloTransportGetConfigRequest request,
      ApolloHttpRequest httpRequest) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, httpRequest);
  }

  private String assembleMessages(ApolloTransportNotificationMessages messages) {
    ApolloNotificationMessages httpMessages = new ApolloNotificationMessages();
    httpMessages.setDetails(messages.getDetails());
    return GSON.toJson(httpMessages);
  }
}

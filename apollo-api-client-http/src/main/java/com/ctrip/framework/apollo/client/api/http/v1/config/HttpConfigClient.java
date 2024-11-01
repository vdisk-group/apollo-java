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
package com.ctrip.framework.apollo.client.api.http.v1.config;

import com.ctrip.framework.apollo.client.api.http.v1.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.api.http.v1.util.InternalHttpUtil;
import com.ctrip.framework.apollo.client.api.v1.Endpoint;
import com.ctrip.framework.apollo.client.api.v1.config.ConfigClient;
import com.ctrip.framework.apollo.client.api.v1.config.ConfigException;
import com.ctrip.framework.apollo.client.api.v1.config.ConfigNotFoundException;
import com.ctrip.framework.apollo.client.api.v1.config.GetConfigRequest;
import com.ctrip.framework.apollo.client.api.v1.config.GetConfigResponse;
import com.ctrip.framework.apollo.client.api.v1.config.GetConfigResult;
import com.ctrip.framework.apollo.client.api.v1.config.GetConfigStatus;
import com.ctrip.framework.apollo.client.api.v1.config.NotificationDefinition;
import com.ctrip.framework.apollo.client.api.v1.config.NotificationMessages;
import com.ctrip.framework.apollo.client.api.v1.config.NotificationResult;
import com.ctrip.framework.apollo.client.api.v1.config.WatchNotificationsRequest;
import com.ctrip.framework.apollo.client.api.v1.config.WatchNotificationsResponse;
import com.ctrip.framework.apollo.client.api.v1.config.WatchNotificationsStatus;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportException;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpConfigClient implements ConfigClient {

  private static final Gson GSON = new Gson();

  private static final Type WATCH_NOTIFICATIONS_RESPONSE_TYPE = new TypeToken<List<ApolloConfigNotification>>() {
  }.getType();

  private final HttpTransport httpTransport;

  private final HttpConfigClientProperties properties;

  public HttpConfigClient(HttpTransport httpTransport, HttpConfigClientProperties properties) {
    Objects.requireNonNull(httpTransport, "httpTransport");
    Objects.requireNonNull(properties, "properties");
    this.httpTransport = httpTransport;
    this.properties = properties;
  }

  @Override
  public String traceWatch(Endpoint endpoint, WatchNotificationsRequest request) {
    return this.toWatchHttpUri(endpoint, request);
  }

  private String toWatchHttpUri(Endpoint endpoint, WatchNotificationsRequest request) {
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
    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);
    String query = InternalHttpUtil.toQueryString(queryParams);
    return MessageFormat.format("{0}/notifications/v2{1}", actualAddress, query);
  }

  @Override
  public WatchNotificationsResponse watch(Endpoint endpoint, WatchNotificationsRequest request)
      throws ConfigException {
    HttpTransportRequest httpTransportRequest = this.toWatchHttpRequest(endpoint, request);
    HttpTransportResponse<List<ApolloConfigNotification>> httpTransportResponse = this.doGet(
        "Watch notifications",
        httpTransportRequest, WATCH_NOTIFICATIONS_RESPONSE_TYPE);

    return this.toWatchResponse(httpTransportResponse);
  }

  private HttpTransportRequest toWatchHttpRequest(Endpoint endpoint,
      WatchNotificationsRequest request) {
    String uri = this.toWatchHttpUri(endpoint, request);

    HttpTransportRequest.Builder requestBuilder = HttpTransportRequest.builder()
        .url(uri)
        .connectTimeout(this.properties.getWatchNotificationConnectTimeout())
        .readTimeout(this.properties.getWatchNotificationReadTimeout());

    this.signWatchNotifications(uri, request, requestBuilder);

    return requestBuilder.build();
  }

  private String assembleNotifications(List<NotificationDefinition> notifications) {
    List<ApolloConfigNotification> httpNotifications = new ArrayList<>(notifications.size());
    for (NotificationDefinition notification : notifications) {
      ApolloConfigNotification httpNotification = new ApolloConfigNotification(
          notification.getNamespaceName(),
          notification.getNotificationId());
      httpNotifications.add(httpNotification);
    }
    return GSON.toJson(httpNotifications);
  }


  private void signWatchNotifications(String uri, WatchNotificationsRequest request,
      HttpTransportRequest.Builder requestBuilder) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, requestBuilder);
  }

  private void signHttpRequest(String uri, String appId, String accessKeySecret,
      HttpTransportRequest.Builder requestBuilder) {
    if (!StringUtils.isBlank(accessKeySecret)) {
      Map<String, String> headers = Maps.newLinkedHashMap();
      Map<String, String> authorizationHeaders = Signature.buildHttpHeaders(uri, appId,
          accessKeySecret);
      headers.putAll(authorizationHeaders);
      requestBuilder.headers(headers);
    }
  }

  private <T> HttpTransportResponse<T> doGet(String scene,
      HttpTransportRequest httpTransportRequest, Type responseType)
      throws ConfigNotFoundException {
    HttpTransportResponse<T> httpTransportResponse;
    try {
      httpTransportResponse = this.httpTransport.doGet(
          httpTransportRequest, responseType);
    } catch (HttpTransportStatusCodeException e) {
      if (e.getStatusCode() == 404) {
        throw new ConfigNotFoundException();
      } else {
        throw new ConfigException(
            MessageFormat.format("{0} failed. Http status code: {1}",
                scene, e.getStatusCode()),
            e);
      }
    } catch (HttpTransportException e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Http error: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Error: {1}",
              scene, e.getLocalizedMessage()), e);
    }
    return httpTransportResponse;
  }

  private WatchNotificationsResponse toWatchResponse(
      HttpTransportResponse<List<ApolloConfigNotification>> httpTransportResponse) {
    List<ApolloConfigNotification> httpNotifications = httpTransportResponse.getBody();
    if (httpTransportResponse.getStatusCode() == 304) {
      return WatchNotificationsResponse.builder()
          .status(WatchNotificationsStatus.NOT_MODIFIED)
          .notifications(Collections.emptyList())
          .build();
    }

    List<NotificationResult> notifications = this.toNotificationResults(
        httpNotifications);
    return WatchNotificationsResponse.builder()
        .status(WatchNotificationsStatus.OK)
        .notifications(notifications)
        .build();
  }

  private List<NotificationResult> toNotificationResults(
      List<ApolloConfigNotification> httpNotifications) {
    if (InternalCollectionUtil.isEmpty(httpNotifications)) {
      return Collections.emptyList();
    }
    List<NotificationResult> notifications = new ArrayList<>(
        httpNotifications.size());
    for (ApolloConfigNotification httpNotification : httpNotifications) {
      ApolloNotificationMessages httpNotificationMessages = httpNotification.getMessages();
      NotificationMessages messages = this.toNotificationMessages(
          httpNotificationMessages);
      NotificationResult result = NotificationResult.builder()
          .namespaceName(httpNotification.getNamespaceName())
          .notificationId(httpNotification.getNotificationId())
          .messages(messages)
          .build();
      notifications.add(result);
    }
    return notifications;
  }

  private NotificationMessages toNotificationMessages(
      ApolloNotificationMessages httpNotificationMessages) {
    Map<String, Long> details =
        httpNotificationMessages != null ? httpNotificationMessages.getDetails() : null;
    if (InternalCollectionUtil.isEmpty(details)) {
      return null;
    }
    return NotificationMessages.builder()
        .details(details)
        .build();
  }

  @Override
  public String traceGetConfig(Endpoint endpoint, GetConfigRequest request) {
    return this.toGetConfigHttpUri(endpoint, request);
  }

  private String toGetConfigHttpUri(Endpoint endpoint, GetConfigRequest request) {
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

    NotificationMessages messages = request.getMessages();
    if (messages != null) {
      queryParams.put("messages", this.assembleMessages(messages));
    }

    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);

    String query = InternalHttpUtil.toQueryString(queryParams);

    return MessageFormat.format("{0}/configs/{1}/{2}/{3}{4}", actualAddress,
        InternalHttpUtil.toPathSegment(request.getAppId()),
        InternalHttpUtil.toPathSegment(request.getCluster()),
        InternalHttpUtil.toPathSegment(request.getNamespace()), query);
  }

  @Override
  public GetConfigResponse getConfig(Endpoint endpoint, GetConfigRequest request)
      throws ConfigException, ConfigNotFoundException {

    HttpTransportRequest httpTransportRequest = this.toGetConfigHttpRequest(endpoint, request);

    HttpTransportResponse<ApolloConfig> httpTransportResponse = this.doGet("Get config",
        httpTransportRequest,
        ApolloConfig.class);

    ApolloConfig apolloConfig = httpTransportResponse.getBody();
    if (httpTransportResponse.getStatusCode() == 304) {
      return GetConfigResponse.builder()
          .status(GetConfigStatus.NOT_MODIFIED)
          .build();
    }
    GetConfigResult configResult = this.toGetConfigResult(apolloConfig);
    return GetConfigResponse.builder()
        .status(GetConfigStatus.OK)
        .config(configResult)
        .build();
  }

  private HttpTransportRequest toGetConfigHttpRequest(Endpoint endpoint, GetConfigRequest request) {
    String uri = this.toGetConfigHttpUri(endpoint, request);

    HttpTransportRequest.Builder requestBuilder = HttpTransportRequest.builder()
        .url(uri)
        .connectTimeout(this.properties.getGetConfigConnectTimeout())
        .readTimeout(this.properties.getGetConfigReadTimeout());

    this.signGetConfig(uri, request, requestBuilder);

    return requestBuilder.build();
  }

  private String assembleMessages(NotificationMessages messages) {
    ApolloNotificationMessages httpMessages = new ApolloNotificationMessages();
    httpMessages.setDetails(messages.getDetails());
    return GSON.toJson(httpMessages);
  }

  private void signGetConfig(String uri, GetConfigRequest request,
      HttpTransportRequest.Builder requestBuilder) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, requestBuilder);
  }

  private GetConfigResult toGetConfigResult(ApolloConfig apolloConfig) {
    if (apolloConfig == null) {
      return null;
    }
    return GetConfigResult.builder()
        .appId(apolloConfig.getAppId())
        .cluster(apolloConfig.getCluster())
        .namespaceName(apolloConfig.getNamespaceName())
        .releaseKey(apolloConfig.getReleaseKey())
        .configurations(apolloConfig.getConfigurations())
        .build();
  }
}

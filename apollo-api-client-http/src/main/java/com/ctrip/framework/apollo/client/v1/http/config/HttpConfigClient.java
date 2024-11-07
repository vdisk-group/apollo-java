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
package com.ctrip.framework.apollo.client.v1.http.config;

import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigException;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigNotFoundException;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigOptions;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigRequest;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigResponse;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigResult;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigStatus;
import com.ctrip.framework.apollo.client.v1.api.config.NotificationDefinition;
import com.ctrip.framework.apollo.client.v1.api.config.NotificationMessages;
import com.ctrip.framework.apollo.client.v1.api.config.NotificationResult;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsOptions;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsRequest;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsResponse;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsStatus;
import com.ctrip.framework.apollo.client.v1.http.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.v1.http.util.InternalHttpUtil;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportException;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.ctrip.framework.apollo.core.http.TypeReference;
import com.ctrip.framework.apollo.core.http.TypeReferences;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpConfigClient implements ConfigClient {

  private static final Gson GSON = new Gson();

  static final TypeReference<ApolloConfig> GET_CONFIG_RESPONSE_TYPE = TypeReferences.ofClass(
      ApolloConfig.class);

  static final TypeReference<List<ApolloConfigNotification>> WATCH_NOTIFICATIONS_RESPONSE_TYPE = TypeReferences.ofTypeToken(
      new TypeToken<List<ApolloConfigNotification>>() {
      });

  private final HttpTransport httpTransport;

  private final HttpConfigClientProperties properties;

  public HttpConfigClient(HttpTransport httpTransport, HttpConfigClientProperties properties) {
    Objects.requireNonNull(httpTransport, "httpTransport");
    Objects.requireNonNull(properties, "properties");
    this.httpTransport = httpTransport;
    this.properties = properties;
  }

  @Override
  public String traceWatch(WatchNotificationsRequest request) {
    return this.toWatchHttpUri(request);
  }

  private String toWatchHttpUri(WatchNotificationsRequest request) {
    String endpoint = request.getEndpoint();
    WatchNotificationsOptions options = request.getOptions();

    Map<String, String> queryParams = new LinkedHashMap<>();
    queryParams.put("appId", options.getAppId());
    queryParams.put("cluster", options.getCluster());
    queryParams.put("notifications", this.assembleNotifications(options.getNotifications()));

    String dataCenter = options.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      queryParams.put("dataCenter", dataCenter);
    }
    String clientIp = options.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }
    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);
    String query = InternalHttpUtil.toQueryString(queryParams);
    return MessageFormat.format("{0}/notifications/v2{1}", actualAddress, query);
  }

  @Override
  public WatchNotificationsResponse watch(WatchNotificationsRequest request)
      throws ConfigException {
    HttpTransportRequest httpTransportRequest = this.toWatchHttpRequest(request);
    HttpTransportResponse<List<ApolloConfigNotification>> httpTransportResponse = this.doGetInternal(
        "Watch notifications",
        () -> this.httpTransport.doGet(httpTransportRequest, WATCH_NOTIFICATIONS_RESPONSE_TYPE));

    return this.toWatchResponse(httpTransportResponse);
  }

  private HttpTransportRequest toWatchHttpRequest(WatchNotificationsRequest request) {
    String uri = this.toWatchHttpUri(request);

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
    WatchNotificationsOptions options = request.getOptions();

    String accessKeySecret = options.getAccessKeySecret();
    String appId = options.getAppId();
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

  private <T> HttpTransportResponse<T> doGetInternal(String scene,
      Supplier<HttpTransportResponse<T>> action)
      throws ConfigNotFoundException {
    HttpTransportResponse<T> httpTransportResponse;
    try {
      httpTransportResponse = action.get();
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
  public String traceGetConfig(GetConfigRequest request) {
    return this.toGetConfigHttpUri(request);
  }

  private String toGetConfigHttpUri(GetConfigRequest request) {
    String endpoint = request.getEndpoint();
    GetConfigOptions options = request.getOptions();

    Map<String, String> queryParams = new LinkedHashMap<>();

    String releaseKey = options.getReleaseKey();
    if (!StringUtils.isEmpty(releaseKey)) {
      queryParams.put("releaseKey", releaseKey);
    }

    String dataCenter = options.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      queryParams.put("dataCenter", dataCenter);
    }

    String clientIp = options.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }

    String label = options.getLabel();
    if (!StringUtils.isEmpty(label)) {
      queryParams.put("label", label);
    }

    NotificationMessages messages = options.getMessages();
    if (messages != null) {
      queryParams.put("messages", this.assembleMessages(messages));
    }

    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);

    String query = InternalHttpUtil.toQueryString(queryParams);

    return MessageFormat.format("{0}/configs/{1}/{2}/{3}{4}", actualAddress,
        InternalHttpUtil.toPathSegment(options.getAppId()),
        InternalHttpUtil.toPathSegment(options.getCluster()),
        InternalHttpUtil.toPathSegment(options.getNamespace()), query);
  }

  @Override
  public GetConfigResponse getConfig(GetConfigRequest request)
      throws ConfigException, ConfigNotFoundException {

    HttpTransportRequest httpTransportRequest = this.toGetConfigHttpRequest(request);

    HttpTransportResponse<ApolloConfig> httpTransportResponse = this.doGetInternal(
        "Get config",
        () -> this.httpTransport.doGet(httpTransportRequest, GET_CONFIG_RESPONSE_TYPE));

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

  private HttpTransportRequest toGetConfigHttpRequest(GetConfigRequest request) {
    String uri = this.toGetConfigHttpUri(request);

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
    GetConfigOptions options = request.getOptions();

    String accessKeySecret = options.getAccessKeySecret();
    String appId = options.getAppId();
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

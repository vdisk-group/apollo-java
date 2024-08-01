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

import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpException;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpRequest;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpResponse;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpStatusCodeException;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpTransport;
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

  private final HttpTransport watchTransport;

  private final HttpTransport getTransport;

  public HttpConfigClient(HttpTransport watchTransport, HttpTransport getTransport) {
    Objects.requireNonNull(watchTransport, "watchTransport");
    Objects.requireNonNull(getTransport, "getTransport");
    this.watchTransport = watchTransport;
    this.getTransport = getTransport;
  }

  @Override
  public WatchNotificationsResponse watch(Endpoint endpoint, WatchNotificationsRequest request)
      throws ConfigException {
    HttpRequest httpRequest = this.toWatchHttpRequest(endpoint, request);
    HttpResponse<List<ApolloConfigNotification>> httpResponse = this.doGet(
        "Watch notifications", this.watchTransport,
        httpRequest, WATCH_NOTIFICATIONS_RESPONSE_TYPE);

    return this.toWatchResponse(httpResponse);
  }

  private HttpRequest toWatchHttpRequest(Endpoint endpoint, WatchNotificationsRequest request) {
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
    String uri = MessageFormat.format("{0}/notifications/v2{1}", actualAddress, query);

    HttpRequest httpRequest = new HttpRequest(uri);

    this.signWatchNotifications(uri, request, httpRequest);

    return httpRequest;
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
      HttpRequest httpRequest) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, httpRequest);
  }

  private void signHttpRequest(String uri, String appId, String accessKeySecret,
      HttpRequest httpRequest) {
    if (!StringUtils.isBlank(accessKeySecret)) {
      Map<String, String> headers = Maps.newLinkedHashMap();
      Map<String, String> authorizationHeaders = Signature.buildHttpHeaders(uri, appId,
          accessKeySecret);
      headers.putAll(authorizationHeaders);
      httpRequest.setHeaders(headers);
    }
  }

  private <T> HttpResponse<T> doGet(String scene,
      HttpTransport transport, HttpRequest httpRequest, Type responseType) {
    HttpResponse<T> httpResponse;
    try {
      httpResponse = transport.doGet(
          httpRequest, responseType);
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode() == 404) {
        throw new ConfigNotFoundException();
      } else {
        throw new ConfigException(
            MessageFormat.format("{0} failed. Http status code: {1}",
                scene, e.getStatusCode()),
            e);
      }
    } catch (HttpException e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Http error message: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Error message: {1}",
              scene, e.getLocalizedMessage()), e);
    }
    return httpResponse;
  }

  private WatchNotificationsResponse toWatchResponse(
      HttpResponse<List<ApolloConfigNotification>> httpResponse) {
    List<ApolloConfigNotification> httpNotifications = httpResponse.getBody();
    if (httpResponse.getStatusCode() == 304) {
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
  public GetConfigResponse get(Endpoint endpoint, GetConfigRequest request)
      throws ConfigException, ConfigNotFoundException {

    HttpRequest httpRequest = this.toGetConfigHttpRequest(endpoint, request);

    HttpResponse<ApolloConfig> httpResponse = this.doGet("Get config", this.getTransport,
        httpRequest,
        ApolloConfig.class);

    ApolloConfig apolloConfig = httpResponse.getBody();
    if (httpResponse.getStatusCode() == 304) {
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

  private HttpRequest toGetConfigHttpRequest(Endpoint endpoint, GetConfigRequest request) {
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

    String uri = MessageFormat.format("{0}/configs/{1}/{2}/{3}{4}", actualAddress,
        request.getAppId(),
        request.getCluster(), request.getNamespace(), query);

    HttpRequest httpRequest = new HttpRequest(uri);

    this.signGetConfig(uri, request, httpRequest);

    return httpRequest;
  }

  private String assembleMessages(NotificationMessages messages) {
    ApolloNotificationMessages httpMessages = new ApolloNotificationMessages();
    httpMessages.setDetails(messages.getDetails());
    return GSON.toJson(httpMessages);
  }

  private void signGetConfig(String uri, GetConfigRequest request,
      HttpRequest httpRequest) {
    String accessKeySecret = request.getAccessKeySecret();
    String appId = request.getAppId();
    this.signHttpRequest(uri, appId, accessKeySecret, httpRequest);
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

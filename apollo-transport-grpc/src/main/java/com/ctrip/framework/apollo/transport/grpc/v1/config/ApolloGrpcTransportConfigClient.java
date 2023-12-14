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
package com.ctrip.framework.apollo.transport.grpc.v1.config;

import com.ctrip.framework.apollo.core.utils.CollectionUtil;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.grpc.api.v1.config.Config;
import com.ctrip.framework.apollo.grpc.api.v1.config.ConfigServiceGrpc;
import com.ctrip.framework.apollo.grpc.api.v1.config.ConfigServiceGrpc.ConfigServiceBlockingStub;
import com.ctrip.framework.apollo.grpc.api.v1.config.GetConfigRequest;
import com.ctrip.framework.apollo.grpc.api.v1.config.GetConfigResponse;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationDefinition;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationMessages;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationResult;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationServiceGrpc;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationServiceGrpc.NotificationServiceBlockingStub;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationRequest;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationRequest.Builder;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationResponse;
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
import io.grpc.ManagedChannel;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ApolloGrpcTransportConfigClient implements ApolloTransportConfigClient {

  private final ApolloGrpcChannelManager channelManager;

  public ApolloGrpcTransportConfigClient(ApolloGrpcChannelManager channelManager) {
    this.channelManager = channelManager;
  }

  @Override
  public ApolloTransportWatchNotificationsResponse watchNotifications(
      ApolloTransportEndpoint endpoint, ApolloTransportWatchNotificationsRequest request)
      throws ApolloTransportConfigException {
    ManagedChannel channel = this.getChannel(endpoint);

    WatchNotificationRequest grpcRequest = this.toWatchNotificationsGrpcRequest(request);

    NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(channel);
    WatchNotificationResponse grpcResponse = this.doGet("Watch notifications",
        () -> blockingStub.watch(grpcRequest));
    if (grpcResponse == null) {
      return ApolloTransportWatchNotificationsResponse.builder()
          .status(ApolloTransportWatchNotificationsStatus.NOT_MODIFIED)
          .notifications(Collections.emptyList())
          .build();
    }
    List<ApolloTransportNotificationResult> notificationResults = this.toNotificationResults(
        grpcResponse);
    return ApolloTransportWatchNotificationsResponse.builder()
        .status(ApolloTransportWatchNotificationsStatus.OK)
        .notifications(notificationResults)
        .build();
  }

  private <T> T doGet(String scene, Callable<Iterator<T>> clientCall) {
    try {
      Iterator<T> responseIterator = clientCall.call();
      if (responseIterator.hasNext()) {
        return responseIterator.next();
      } else {
        // not modified
        return null;
      }
    } catch (StatusRuntimeException e) {
      Code statusCode = e.getStatus().getCode();
      if (statusCode.equals(io.grpc.Status.Code.NOT_FOUND)) {
        throw new ApolloTransportConfigNotFoundException();
      } else {
        throw new ApolloTransportConfigException(
            MessageFormat.format("{0} failed. Grpc status code: {1}", scene, statusCode), e);
      }
    } catch (Throwable e) {
      throw new ApolloTransportConfigException(
          MessageFormat.format("{0} failed. Error message: {1}", scene, e.getLocalizedMessage()),
          e);
    }
  }

  private WatchNotificationRequest toWatchNotificationsGrpcRequest(
      ApolloTransportWatchNotificationsRequest request) {

    List<NotificationDefinition> notificationDefinitions = this.toNotificationDefinitions(request);

    Builder builder = WatchNotificationRequest.newBuilder();
    builder.setAppId(request.getAppId());
    builder.setCluster(request.getCluster());
    builder.addAllNotifications(notificationDefinitions);

    String dataCenter = request.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      builder.setDataCenter(dataCenter);
    }

    String clientIp = request.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      builder.setClientIp(clientIp);
    }

    String label = request.getLabel();
    if (!StringUtils.isEmpty(label)) {
      builder.setLabel(label);
    }

    return builder.build();
  }

  private List<NotificationDefinition> toNotificationDefinitions(
      ApolloTransportWatchNotificationsRequest request) {
    List<ApolloTransportNotificationDefinition> notificationDefinitions = request.getNotifications();

    if (CollectionUtil.isEmpty(notificationDefinitions)) {
      return Collections.emptyList();
    }

    List<NotificationDefinition> notifications = new ArrayList<>(notificationDefinitions.size());
    for (ApolloTransportNotificationDefinition notificationDefinition : notificationDefinitions) {
      NotificationDefinition notification = NotificationDefinition.newBuilder()
          .setNamespaceName(notificationDefinition.getNamespaceName())
          .setNotificationId(notificationDefinition.getNotificationId())
          .build();
      notifications.add(notification);
    }

    return notifications;
  }

  private List<ApolloTransportNotificationResult> toNotificationResults(
      WatchNotificationResponse notificationResponse) {
    List<NotificationResult> notifications = notificationResponse.getNotificationsList();
    if (CollectionUtil.isEmpty(notifications)) {
      return Collections.emptyList();
    }

    List<ApolloTransportNotificationResult> notificationResults = new ArrayList<>(
        notifications.size());
    for (NotificationResult notification : notifications) {
      ApolloTransportNotificationMessages notificationMessages = this.toNotificationMessages(
          notification);
      ApolloTransportNotificationResult notificationResult = ApolloTransportNotificationResult.builder()
          .namespaceName(notification.getNamespaceName())
          .notificationId(notification.getNotificationId())
          .messages(notificationMessages)
          .build();
      notificationResults.add(notificationResult);
    }

    return notificationResults;
  }

  private ApolloTransportNotificationMessages toNotificationMessages(
      NotificationResult notification) {
    if (!notification.hasMessages()) {
      return null;
    }
    NotificationMessages messages = notification.getMessages();
    Map<String, Long> detailsMap = messages.getDetailsMap();
    if (CollectionUtil.isEmpty(detailsMap)) {
      return null;
    }
    return ApolloTransportNotificationMessages.builder()
        .details(detailsMap)
        .build();
  }

  private ManagedChannel getChannel(ApolloTransportEndpoint endpoint) {
    return this.channelManager.getChannel(endpoint);
  }

  @Override
  public ApolloTransportGetConfigResponse getConfig(ApolloTransportEndpoint endpoint,
      ApolloTransportGetConfigRequest request)
      throws ApolloTransportConfigException, ApolloTransportConfigNotFoundException {
    ManagedChannel channel = this.getChannel(endpoint);

    GetConfigRequest grpcRequest = this.toGetConfigGrpcRequest(request);

    ConfigServiceBlockingStub blockingStub = ConfigServiceGrpc.newBlockingStub(
        channel);

    GetConfigResponse grpcResponse = this.doGet("Get config",
        () -> blockingStub.getConfig(grpcRequest));

    if (grpcResponse == null) {
      return ApolloTransportGetConfigResponse.builder()
          .status(ApolloTransportGetConfigStatus.NOT_MODIFIED)
          .build();
    }

    ApolloTransportGetConfigResult configResult = this.toConfigResult(grpcResponse);
    return ApolloTransportGetConfigResponse.builder()
        .status(ApolloTransportGetConfigStatus.OK)
        .config(configResult)
        .build();
  }

  private GetConfigRequest toGetConfigGrpcRequest(ApolloTransportGetConfigRequest request) {
    GetConfigRequest.Builder builder = GetConfigRequest.newBuilder();

    builder.setAppId(request.getAppId());
    builder.setCluster(request.getCluster());
    builder.setNamespace(request.getNamespace());

    String releaseKey = request.getReleaseKey();
    if (!StringUtils.isEmpty(releaseKey)) {
      builder.setReleaseKey(releaseKey);
    }

    String dataCenter = request.getDataCenter();
    if (!StringUtils.isEmpty(dataCenter)) {
      builder.setDataCenter(dataCenter);
    }

    String clientIp = request.getClientIp();
    if (!StringUtils.isEmpty(clientIp)) {
      builder.setClientIp(clientIp);
    }

    String label = request.getLabel();
    if (!StringUtils.isEmpty(label)) {
      builder.setLabel(label);
    }

    NotificationMessages notificationMessages = this.toMessages(request);
    if (notificationMessages != null) {
      builder.setMessages(notificationMessages);
    }

    return builder.build();
  }

  private NotificationMessages toMessages(ApolloTransportGetConfigRequest request) {
    ApolloTransportNotificationMessages messages = request.getMessages();
    if (messages == null) {
      return null;
    }
    return NotificationMessages.newBuilder()
        .putAllDetails(messages.getDetails())
        .build();
  }

  private ApolloTransportGetConfigResult toConfigResult(GetConfigResponse getConfigResponse) {
    if (!getConfigResponse.hasConfig()) {
      return null;
    }
    Config config = getConfigResponse.getConfig();
    return ApolloTransportGetConfigResult.builder()
        .appId(config.getAppId())
        .cluster(config.getCluster())
        .namespaceName(config.getNamespaceName())
        .configurations(config.getConfigurationsMap())
        .releaseKey(config.getReleaseKey())
        .build();
  }
}

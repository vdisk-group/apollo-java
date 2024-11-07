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
package com.ctrip.framework.apollo.client.v1.grpc.config;

import com.ctrip.framework.apollo.api.v1.grpc.config.ConfigServiceGrpc;
import com.ctrip.framework.apollo.api.v1.grpc.config.ConfigServiceGrpc.ConfigServiceBlockingStub;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcApolloConfig;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcGetConfigRequest;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcGetConfigResponse;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcGetConfigResponseData;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcNotificationDefinition;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcNotificationMessages;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcNotificationResult;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcWatchNotificationRequest;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcWatchNotificationRequest.Builder;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcWatchNotificationResponse;
import com.ctrip.framework.apollo.api.v1.grpc.config.GrpcWatchNotificationResponseData;
import com.ctrip.framework.apollo.api.v1.grpc.config.NotificationServiceGrpc;
import com.ctrip.framework.apollo.api.v1.grpc.config.NotificationServiceGrpc.NotificationServiceBlockingStub;
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
import com.ctrip.framework.apollo.client.v1.grpc.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.v1.grpc.util.InternalStringUtil;
import com.ctrip.framework.apollo.grpc.channel.v1.api.GrpcChannelManager;
import com.sun.javaws.exceptions.InvalidArgumentException;
import io.grpc.Context;
import io.grpc.Context.CancellableContext;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class GrpcConfigClient implements ConfigClient {

  private final GrpcChannelManager channelManager;

  private final GrpcConfigClientProperties properties;

  public GrpcConfigClient(GrpcChannelManager channelManager,
      GrpcConfigClientProperties properties) {
    Objects.requireNonNull(channelManager, "channelManager");
    Objects.requireNonNull(properties, "properties");
    this.channelManager = channelManager;
    this.properties = properties;
  }

  @Override
  public String traceWatch(WatchNotificationsRequest request) {
    return this.toWatchHttpUri(request);
  }

  private String toWatchHttpUri(WatchNotificationsRequest request) {
    return "";
  }

  @Override
  public WatchNotificationsResponse watch(WatchNotificationsRequest request)
      throws ConfigException {
    String endpoint = request.getEndpoint();

    ManagedChannel channel = this.channelManager.getChannel(endpoint);
    NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(
        channel);
    GrpcWatchNotificationRequest grpcRequest = this.toWatchGrpcRequest(request);

    GrpcWatchNotificationResponse grpcResponse = this.doCallInternal("Watch notifications",
        () -> {
          Iterator<GrpcWatchNotificationResponse> responseIterator = blockingStub.watch(
              grpcRequest);
          if (responseIterator.hasNext()) {
            return responseIterator.next();
          } else {
            throw new ConfigException("Watch notifications failed. No response");
          }
        });
    return this.toWatchResponse(grpcResponse);

  }

  private GrpcWatchNotificationRequest toWatchGrpcRequest(WatchNotificationsRequest request) {
    String endpoint = request.getEndpoint();
    WatchNotificationsOptions options = request.getOptions();

    Builder builder = GrpcWatchNotificationRequest.newBuilder();

    builder.setAppId(options.getAppId());
    builder.setCluster(options.getCluster());

    List<GrpcNotificationDefinition> grpcNotifications = this.toGrpcNotificationDefinitions(
        options.getNotifications());
    builder.addAllNotifications(grpcNotifications);

    String dataCenter = options.getDataCenter();
    if (!InternalStringUtil.isEmpty(dataCenter)) {
      builder.setDataCenter(dataCenter);
    }

    String clientIp = options.getClientIp();
    if (!InternalStringUtil.isEmpty(clientIp)) {
      builder.setClientIp(clientIp);
    }

    String label = options.getLabel();
    if (!InternalStringUtil.isEmpty(label)) {
      builder.setLabel(label);
    }

    return builder.build();
  }

  private List<GrpcNotificationDefinition> toGrpcNotificationDefinitions(
      List<NotificationDefinition> notifications) {

    if (InternalCollectionUtil.isEmpty(notifications)) {
      return Collections.emptyList();
    }

    List<GrpcNotificationDefinition> grpcNotifications = new ArrayList<>(notifications.size());
    for (NotificationDefinition notification : notifications) {
      GrpcNotificationDefinition grpcNotification = GrpcNotificationDefinition.newBuilder()
          .setNamespaceName(notification.getNamespaceName())
          .setNotificationId(notification.getNotificationId())
          .build();
      grpcNotifications.add(grpcNotification);
    }
    return grpcNotifications;
  }

  private <T> T doCallInternal(String scene, Callable<T> action)
      throws ConfigNotFoundException {
    try (CancellableContext cancellableContext = Context.current().withCancellation()) {
      return cancellableContext.call(action);
    } catch (ConfigException e) {
      throw e;
    } catch (StatusRuntimeException e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Grpc Error: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new ConfigException(
          MessageFormat.format("{0} failed. Error: {1}",
              scene, e.getLocalizedMessage()), e);
    }
  }

  private WatchNotificationsResponse toWatchResponse(
      GrpcWatchNotificationResponse grpcResponse) {
    int statusValue = grpcResponse.getStatusValue();
    GrpcWatchNotificationResponse.Status status = grpcResponse.getStatus();
    String errorMessage = grpcResponse.getErrorMessage();
    switch (status) {
      case UNKNOWN: {
        throw new ConfigException(
            MessageFormat.format("Watch notifications failed. status: UNKNOWN {0}", errorMessage));
      }
      case NOT_MODIFIED: {
        return WatchNotificationsResponse.builder()
            .status(WatchNotificationsStatus.NOT_MODIFIED)
            .notifications(Collections.emptyList())
            .build();
      }
      case OK: {
        return this.toOkWatchResponse(grpcResponse);
      }
      case NOT_FOUND: {
        throw new ConfigNotFoundException();
      }
      case INVALID_ARGUMENT: {
        throw new ConfigException(
            MessageFormat.format("Watch notifications failed. status: INVALID_ARGUMENT {0}",
                errorMessage));
      }
      case INTERNAL_SERVER_ERROR: {
        throw new ConfigException(
            MessageFormat.format("Watch notifications failed. status: INTERNAL_SERVER_ERROR {0}",
                errorMessage));
      }
      case UNRECOGNIZED: {
        throw new ConfigException(
            MessageFormat.format("Watch notifications failed. status: UNRECOGNIZED ({0}) {1}",
                statusValue, errorMessage));
      }
      default: {
        throw new ConfigException(
            MessageFormat.format("Watch notifications failed. Unexpected status: {0} {1}", status,
                errorMessage));
      }
    }
  }

  private WatchNotificationsResponse toOkWatchResponse(
      GrpcWatchNotificationResponse grpcResponse) {
    if (!grpcResponse.hasData()) {
      throw new ConfigException("Watch notifications failed. No data");
    }
    GrpcWatchNotificationResponseData data = grpcResponse.getData();
    List<GrpcNotificationResult> grpcNotifications = data.getNotificationsList();
    List<NotificationResult> notifications = this.toNotificationResults(
        grpcNotifications);
    return WatchNotificationsResponse.builder()
        .status(WatchNotificationsStatus.OK)
        .notifications(notifications)
        .build();
  }

  private List<NotificationResult> toNotificationResults(
      List<GrpcNotificationResult> grpcNotifications) {
    if (InternalCollectionUtil.isEmpty(grpcNotifications)) {
      return Collections.emptyList();
    }
    List<NotificationResult> notifications = new ArrayList<>(grpcNotifications.size());
    for (GrpcNotificationResult grpcNotification : grpcNotifications) {
      GrpcNotificationMessages grpcNotificationMessages = grpcNotification.getMessages();
      NotificationMessages messages = this.toNotificationMessages(
          grpcNotificationMessages);
      NotificationResult result = NotificationResult.builder()
          .namespaceName(grpcNotification.getNamespaceName())
          .notificationId(grpcNotification.getNotificationId())
          .messages(messages)
          .build();
      notifications.add(result);
    }
    return notifications;
  }

  private NotificationMessages toNotificationMessages(
      GrpcNotificationMessages grpcNotificationMessages) {
    Map<String, Long> details =
        grpcNotificationMessages != null ? grpcNotificationMessages.getDetailsMap() : null;
    if (InternalCollectionUtil.isEmpty(details)) {
      return null;
    }
    return NotificationMessages.builder()
        .details(new LinkedHashMap<>(details))
        .build();
  }

  @Override
  public String traceGetConfig(GetConfigRequest request) {
    return this.toGetConfigHttpUri(request);
  }

  private String toGetConfigHttpUri(GetConfigRequest request) {
    return "";
  }

  @Override
  public GetConfigResponse getConfig(GetConfigRequest request)
      throws ConfigException, ConfigNotFoundException {
    String endpoint = request.getEndpoint();

    ManagedChannel channel = this.channelManager.getChannel(endpoint);
    ConfigServiceBlockingStub blockingStub = ConfigServiceGrpc.newBlockingStub(
        channel);
    GrpcGetConfigRequest grpcRequest = this.toGetConfigGrpcRequest(request);

    GrpcGetConfigResponse grpcResponse = this.doCallInternal(
        "Get config",
        () -> blockingStub.getConfig(grpcRequest));

    return this.toGetConfigResponse(grpcResponse);
  }

  private GrpcGetConfigRequest toGetConfigGrpcRequest(GetConfigRequest request) {
    String endpoint = request.getEndpoint();
    GetConfigOptions options = request.getOptions();

    GrpcGetConfigRequest.Builder builder = GrpcGetConfigRequest.newBuilder();

    builder.setAppId(options.getAppId());
    builder.setCluster(options.getCluster());
    builder.setNamespace(options.getNamespace());

    String releaseKey = options.getReleaseKey();
    if (!InternalStringUtil.isEmpty(releaseKey)) {
      builder.setReleaseKey(releaseKey);
    }

    String dataCenter = options.getDataCenter();
    if (!InternalStringUtil.isEmpty(dataCenter)) {
      builder.setDataCenter(dataCenter);
    }

    String clientIp = options.getClientIp();
    if (!InternalStringUtil.isEmpty(clientIp)) {
      builder.setClientIp(clientIp);
    }

    String label = options.getLabel();
    if (!InternalStringUtil.isEmpty(label)) {
      builder.setLabel(label);
    }

    GrpcNotificationMessages grpcMessages = this.toGrpcNotificationMessages(options.getMessages());
    if (grpcMessages != null) {
      builder.setMessages(grpcMessages);
    }

    return builder.build();
  }

  private GrpcNotificationMessages toGrpcNotificationMessages(NotificationMessages messages) {
    if (messages == null) {
      return null;
    }
    return GrpcNotificationMessages.newBuilder()
        .putAllDetails(messages.getDetails())
        .build();
  }

  private GetConfigResponse toGetConfigResponse(GrpcGetConfigResponse grpcResponse) {
    int statusValue = grpcResponse.getStatusValue();
    GrpcGetConfigResponse.Status status = grpcResponse.getStatus();
    String errorMessage = grpcResponse.getErrorMessage();
    switch (status) {
      case UNKNOWN: {
        throw new ConfigException(MessageFormat.format("Get config failed. status: UNKNOWN {0}",
            errorMessage));
      }
      case NOT_MODIFIED: {
        return GetConfigResponse.builder()
            .status(GetConfigStatus.NOT_MODIFIED)
            .build();
      }
      case OK: {
        return this.toOkGetConfigResponse(grpcResponse);
      }
      case NOT_FOUND: {
        throw new ConfigNotFoundException();
      }
      case INVALID_ARGUMENT: {
        throw new ConfigException(
            MessageFormat.format("Get config failed. status: INVALID_ARGUMENT {0}",
                errorMessage));
      }
      case INTERNAL_SERVER_ERROR: {
        throw new ConfigException(
            MessageFormat.format("Get config failed. status: INTERNAL_SERVER_ERROR {0}",
                errorMessage));
      }
      case UNRECOGNIZED: {
        throw new ConfigException(
            MessageFormat.format("Get config failed. status: UNRECOGNIZED ({0}) {1}", statusValue,
                errorMessage));
      }
      default:
        throw new ConfigException(
            MessageFormat.format("Get config failed. Unexpected status: {0} {1}",
                status, errorMessage));
    }
  }

  private GetConfigResponse toOkGetConfigResponse(GrpcGetConfigResponse grpcResponse) {
    if (!grpcResponse.hasData()) {
      throw new ConfigException("Get config failed. No data");
    }
    GrpcGetConfigResponseData data = grpcResponse.getData();
    if (!data.hasConfig()) {
      throw new ConfigException("Get config failed. No config");
    }
    GrpcApolloConfig grpcApolloConfig = data.getConfig();
    GetConfigResult configResult = this.toGetConfigResult(grpcApolloConfig);
    return GetConfigResponse.builder()
        .status(GetConfigStatus.OK)
        .config(configResult)
        .build();
  }

  private GetConfigResult toGetConfigResult(GrpcApolloConfig grpcApolloConfig) {
    return GetConfigResult.builder()
        .appId(grpcApolloConfig.getAppId())
        .cluster(grpcApolloConfig.getCluster())
        .namespaceName(grpcApolloConfig.getNamespaceName())
        .releaseKey(grpcApolloConfig.getReleaseKey())
        .configurations(new LinkedHashMap<>(grpcApolloConfig.getConfigurationsMap()))
        .build();
  }
}

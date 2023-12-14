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
import com.ctrip.framework.apollo.grpc.api.v1.config.Config;
import com.ctrip.framework.apollo.grpc.api.v1.config.GetConfigResponse;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationResult;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationResponse;
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
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApolloGrpcTransportConfigClientTest {

  private static final Logger log = LoggerFactory.getLogger(
      ApolloGrpcTransportConfigClientTest.class);

  private MockNotificationService mockNotificationService;

  private MockConfigService mockConfigService;

  private Server server;

  @BeforeEach
  void setUp() {
    Server server = this.server;
    if (server == null) {
      MockNotificationService mockNotificationService = new MockNotificationService();
      MockConfigService mockConfigService = new MockConfigService();
      Server newServer = InProcessServerBuilder.forName("test")
          .addService(mockNotificationService)
          .addService(mockConfigService)
          .build();
      try {
        newServer.start();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      this.mockNotificationService = mockNotificationService;
      this.mockConfigService = mockConfigService;
      this.server = newServer;
    }
  }

  @AfterEach
  void tearDown() {
    Server server = this.server;
    if (server != null) {
      server.shutdownNow();
      this.server = null;
    }
  }

  @Test
  void watchNotifications() {
    ManagedChannel channel = InProcessChannelBuilder.forName("test")
        .build();
    ApolloGrpcTransportConfigClient client = new ApolloGrpcTransportConfigClient(
        new MockApolloGrpcChannelManager(channel));
    final long notificationId = -1L;
    final long nextNotificationId = notificationId + 1;
    ApolloTransportWatchNotificationsRequest request = ApolloTransportWatchNotificationsRequest.builder()
        .appId("test-id")
        .cluster("test-cluster")
        .notifications(Collections.singletonList(ApolloTransportNotificationDefinition.builder()
            .notificationId(notificationId)
            .namespaceName("test-namespace")
            .build()))
        .build();
    CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
      NotificationResult result = NotificationResult.newBuilder()
          .setNamespaceName("test-namespace")
          .setNotificationId(nextNotificationId)
          .build();
      WatchNotificationResponse response = WatchNotificationResponse.newBuilder()
          .addNotifications(result)
          .build();
      this.mockNotificationService.completeWatching(1000L, "test-id/test-cluster/test-namespace/-1",
          response);
      log.info("server completeWatching");
    });
    log.info("client startWatching");
    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder().build();
    ApolloTransportWatchNotificationsResponse response = client.watchNotifications(endpoint,
        request);
    log.info("client completeWatching response: {}", response);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(ApolloTransportWatchNotificationsStatus.OK, response.getStatus());
    List<ApolloTransportNotificationResult> notifications = response.getNotifications();
    Assertions.assertNotNull(notifications);
    Assertions.assertEquals(1, notifications.size());
    ApolloTransportNotificationResult notification = notifications.get(0);
    Assertions.assertTrue(notificationId < notification.getNotificationId());

    serverFuture.join();
  }

  @Test
  void watchNotificationsNotModified() {
    ManagedChannel channel = InProcessChannelBuilder.forName("test")
        .build();
    ApolloGrpcTransportConfigClient client = new ApolloGrpcTransportConfigClient(
        new MockApolloGrpcChannelManager(channel));
    final long notificationId = -1L;
    final long nextNotificationId = notificationId + 1;
    ApolloTransportWatchNotificationsRequest request = ApolloTransportWatchNotificationsRequest.builder()
        .appId("test-id")
        .cluster("test-cluster")
        .notifications(Collections.singletonList(ApolloTransportNotificationDefinition.builder()
            .notificationId(notificationId)
            .namespaceName("test-namespace")
            .build()))
        .build();
    CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
      this.mockNotificationService.notModifiedWatching(1000L,
          "test-id/test-cluster/test-namespace/-1");
      log.info("server completeWatching");
    });
    log.info("client startWatching");
    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder().build();
    ApolloTransportWatchNotificationsResponse response = client.watchNotifications(endpoint,
        request);
    log.info("client completeWatching response: {}", response);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(ApolloTransportWatchNotificationsStatus.NOT_MODIFIED,
        response.getStatus());
    List<ApolloTransportNotificationResult> notifications = response.getNotifications();
    Assertions.assertTrue(CollectionUtil.isEmpty(notifications));

    serverFuture.join();
  }

  @Test
  void getConfig() {
    ManagedChannel channel = InProcessChannelBuilder.forName("test")
        .build();
    ApolloGrpcTransportConfigClient client = new ApolloGrpcTransportConfigClient(
        new MockApolloGrpcChannelManager(channel));

    ApolloTransportNotificationMessages messages = ApolloTransportNotificationMessages.builder()
        .details(new LinkedHashMap<>())
        .build();
    ApolloTransportGetConfigRequest request = ApolloTransportGetConfigRequest.builder()
        .appId("test-id")
        .cluster("test-cluster")
        .namespace("test-namespace")
        .releaseKey("test-release-key")
        .messages(messages)
        .build();
    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder().build();

    CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
      Map<String, String> configurations = new LinkedHashMap<>();
      configurations.put("test-key", "test-value");
      Config config = Config.newBuilder()
          .setAppId("test-id")
          .setCluster("test-cluster")
          .setNamespaceName("test-namespace")
          .putAllConfigurations(configurations)
          .build();
      GetConfigResponse response = GetConfigResponse.newBuilder()
          .setConfig(config)
          .build();
      this.mockConfigService.completeGetting(1000L, "test-id/test-cluster/test-namespace",
          response);
      log.info("server completeGetting");
    });

    log.info("client startGetting");
    ApolloTransportGetConfigResponse response = client.getConfig(endpoint, request);
    log.info("client completeGetting response: {}", response);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(ApolloTransportGetConfigStatus.OK, response.getStatus());
    ApolloTransportGetConfigResult configResult = response.getConfig();
    Assertions.assertNotNull(configResult);

    serverFuture.join();
  }

  @Test
  void getConfigNotModified() {
    ManagedChannel channel = InProcessChannelBuilder.forName("test")
        .build();
    ApolloGrpcTransportConfigClient client = new ApolloGrpcTransportConfigClient(
        new MockApolloGrpcChannelManager(channel));

    ApolloTransportNotificationMessages messages = ApolloTransportNotificationMessages.builder()
        .details(new LinkedHashMap<>())
        .build();
    ApolloTransportGetConfigRequest request = ApolloTransportGetConfigRequest.builder()
        .appId("test-id")
        .cluster("test-cluster")
        .namespace("test-namespace")
        .releaseKey("test-release-key")
        .messages(messages)
        .build();
    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder().build();

    CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
      this.mockConfigService.notModifiedGetting(1000L, "test-id/test-cluster/test-namespace");
      log.info("server completeGetting");
    });

    log.info("client startGetting");
    ApolloTransportGetConfigResponse response = client.getConfig(endpoint, request);
    log.info("client completeGetting response: {}", response);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(ApolloTransportGetConfigStatus.NOT_MODIFIED, response.getStatus());
    ApolloTransportGetConfigResult configResult = response.getConfig();
    Assertions.assertNull(configResult);

    serverFuture.join();
  }
}
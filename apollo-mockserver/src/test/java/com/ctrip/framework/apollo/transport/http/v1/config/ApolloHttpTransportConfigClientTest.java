package com.ctrip.framework.apollo.transport.http.v1.config;

import com.ctrip.framework.apollo.core.http.ApolloCoreHttpClient;
import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
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
import com.ctrip.framework.apollo.util.http.DefaultApolloCoreHttpClient;
import com.ctrip.framework.apollo.util.http.DefaultHttpClient;
import com.ctrip.framework.apollo.util.http.HttpClient;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApolloHttpTransportConfigClientTest {

  private static final Logger log = LoggerFactory.getLogger(
      ApolloHttpTransportConfigClientTest.class);

  private ApolloTestingServer server;

  @BeforeEach
  void setUp() {
    ApolloTestingServer server = this.server;
    if (server == null) {
      ApolloTestingServer newServer = new ApolloTestingServer();
      try {
        newServer.start();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
      this.server = newServer;
    }
  }

  @AfterEach
  void tearDown() {
    ApolloTestingServer server = this.server;
    if (server != null) {
      server.close();
      this.server = null;
    }
  }

  @Test
  void watchNotifications() {
    String configServerUrl = server.getConfigServiceUrl();
    HttpClient httpClient = new DefaultHttpClient();
    ApolloCoreHttpClient apolloCoreHttpClient = new DefaultApolloCoreHttpClient(httpClient);
    ApolloHttpTransportConfigClient client = new ApolloHttpTransportConfigClient(
        apolloCoreHttpClient);

    long notificationId = -1L;
    ApolloTransportWatchNotificationsRequest request = ApolloTransportWatchNotificationsRequest.builder()
        .appId("test-id")
        .cluster("test-cluster")
        .notifications(Collections.singletonList(ApolloTransportNotificationDefinition.builder()
            .notificationId(notificationId)
            .namespaceName("test-namespace")
            .build()))
        .build();

    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder()
        .address(configServerUrl)
        .build();

    log.info("client startWatching");
    ApolloTransportWatchNotificationsResponse response = client.watchNotifications(
        endpoint, request);
    log.info("client completeWatching response: {}", response);

    Assertions.assertNotNull(response);
    List<ApolloTransportNotificationResult> notifications = response.getNotifications();
    Assertions.assertNotNull(notifications);
    Assertions.assertEquals(1, notifications.size());
    ApolloTransportNotificationResult notification = notifications.get(0);
    Assertions.assertTrue(notificationId < notification.getNotificationId());
  }

  @Test
  void getConfig() {
    String configServerUrl = server.getConfigServiceUrl();
    HttpClient httpClient = new DefaultHttpClient();
    ApolloCoreHttpClient apolloCoreHttpClient = new DefaultApolloCoreHttpClient(httpClient);
    ApolloHttpTransportConfigClient client = new ApolloHttpTransportConfigClient(
        apolloCoreHttpClient);

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

    ApolloTransportEndpoint endpoint = ApolloTransportEndpoint.builder()
        .address(configServerUrl)
        .build();

    log.info("client startGetting");
    ApolloTransportGetConfigResponse response = client.getConfig(endpoint, request);
    log.info("client completeGetting response: {}", response);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(ApolloTransportGetConfigStatus.OK, response.getStatus());
    ApolloTransportGetConfigResult configResult = response.getConfig();
    Assertions.assertNotNull(configResult);
  }
}
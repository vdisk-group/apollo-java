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

import com.ctrip.framework.apollo.client.v1.api.Endpoint;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
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
import com.ctrip.framework.apollo.client.v1.http.server.ConfigControllerTest;
import com.ctrip.framework.apollo.client.v1.http.server.HttpServerApplicationTest;
import com.ctrip.framework.apollo.client.v1.http.server.NotificationControllerV2Test;
import com.ctrip.framework.apollo.client.v1.http.server.PollNotificationRequest;
import com.ctrip.framework.apollo.client.v1.http.server.QueryConfigRequest;
import com.ctrip.framework.apollo.client.v1.http.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportFactory;
import com.ctrip.framework.apollo.core.http.HttpTransportProperties;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = HttpServerApplicationTest.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpConfigClientTest {

  @LocalServerPort
  private int port;

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void watch_ok() {
    ConfigClient client = this.createClient();

    Endpoint endpoint = Endpoint.builder()
        .address("http://localhost:" + this.port)
        .build();

    WatchNotificationsOptions options = this.createWatchRequest();
    WatchNotificationsRequest request = WatchNotificationsRequest.builder()
        .endpoint(endpoint)
        .options(options)
        .build();

    WatchNotificationsResponse response;
    PollNotificationRequest lastRequest;
    ReentrantLock clientLock = NotificationControllerV2Test.getClientLock();
    clientLock.lock();
    try {
      this.setUpWatchResponse(request);
      response = client.watch(request);
      lastRequest = NotificationControllerV2Test.getRequest();
    } finally {
      clientLock.unlock();
    }

    Assertions.assertNotNull(response);
    List<NotificationDefinition> notifications = options.getNotifications();
    List<NotificationResult> serverNotifications = response.getNotifications();
    Assertions.assertEquals(notifications.size(), serverNotifications.size());
    NotificationDefinition notification = notifications.get(0);
    NotificationResult serverNotification = serverNotifications.get(0);
    Assertions.assertEquals(notification.getNamespaceName(), serverNotification.getNamespaceName());
    Assertions.assertEquals(notification.getNotificationId() + 1,
        serverNotification.getNotificationId());

    assertWatchRequest(request, lastRequest);
  }

  private ConfigClient createClient() {

    HttpTransportProperties transportProperties = HttpTransportProperties.builder()
        .defaultConnectTimeout(1_000)
        .defaultReadTimeout(5_000)
        .build();
    HttpTransportFactory transportFactory = ServiceBootstrap.loadPrimary(
        HttpTransportFactory.class);
    HttpTransport httpTransport = transportFactory.create(
        transportProperties);
    HttpConfigClientProperties properties = HttpConfigClientProperties.builder()
        .watchNotificationConnectTimeout(1_000)
        .watchNotificationReadTimeout(90_000)
        .getConfigConnectTimeout(1_000)
        .getConfigReadTimeout(5_000)
        .build();
    return HttpConfigClientFactory.createClient(httpTransport, properties);
  }

  private static void assertWatchRequest(WatchNotificationsRequest request,
      PollNotificationRequest lastRequest) {
    WatchNotificationsOptions options = request.getOptions();

    List<NotificationDefinition> notifications = options.getNotifications();
    NotificationDefinition notification = notifications.get(0);
    Assertions.assertNotNull(lastRequest);
    Assertions.assertEquals(options.getAppId(), lastRequest.getAppId());
    Assertions.assertEquals(options.getCluster(), lastRequest.getCluster());
    Assertions.assertEquals(options.getDataCenter(), lastRequest.getDataCenter());
    List<ApolloConfigNotification> lastRequestNotifications = lastRequest.getNotifications();
    Assertions.assertEquals(notifications.size(), lastRequestNotifications.size());
    Assertions.assertEquals(options.getClientIp(), lastRequest.getClientIp());
    ApolloConfigNotification lastRequestNotification = lastRequestNotifications.get(0);
    Assertions.assertEquals(notification.getNamespaceName(),
        lastRequestNotification.getNamespaceName());
    Assertions.assertEquals(notification.getNotificationId(),
        lastRequestNotification.getNotificationId());
  }

  private WatchNotificationsOptions createWatchRequest() {
    List<NotificationDefinition> notifications = new ArrayList<>();
    NotificationDefinition notification = NotificationDefinition.builder()
        .namespaceName("someNamespace")
        .notificationId(1L)
        .build();
    notifications.add(notification);
    return WatchNotificationsOptions.builder()
        .appId("someAppId")
        .cluster("someCluster")
        .notifications(notifications)
        .dataCenter("someDataCenter")
        .clientIp("someClientIp")
        .build();
  }

  private void setUpWatchResponse(WatchNotificationsRequest request) {
    WatchNotificationsOptions options = request.getOptions();

    List<NotificationDefinition> clientNotifications = options.getNotifications();
    List<ApolloConfigNotification> serverNotifications = new ArrayList<>();
    for (NotificationDefinition clientNotification : clientNotifications) {
      serverNotifications.add(new ApolloConfigNotification(clientNotification.getNamespaceName(),
          clientNotification.getNotificationId() + 1));
    }
    NotificationControllerV2Test.setResponse(ResponseEntity.ok()
        .body(serverNotifications));
  }

  @Test
  void watch_not_modified() {
    ConfigClient client = this.createClient();

    Endpoint endpoint = Endpoint.builder()
        .address("http://localhost:" + this.port)
        .build();

    WatchNotificationsOptions options = this.createWatchRequest();
    WatchNotificationsRequest request = WatchNotificationsRequest.builder()
        .endpoint(endpoint)
        .options(options)
        .build();

    WatchNotificationsResponse response;
    PollNotificationRequest lastRequest;
    ReentrantLock clientLock = NotificationControllerV2Test.getClientLock();
    clientLock.lock();
    try {
      this.setUpWatchResponse_not_modified(request);
      response = client.watch(request);
      lastRequest = NotificationControllerV2Test.getRequest();
    } finally {
      clientLock.unlock();
    }

    Assertions.assertNotNull(response);
    Assertions.assertEquals(WatchNotificationsStatus.NOT_MODIFIED, response.getStatus());
    Assertions.assertTrue(InternalCollectionUtil.isEmpty(response.getNotifications()));

    assertWatchRequest(request, lastRequest);
  }

  private void setUpWatchResponse_not_modified(WatchNotificationsRequest request) {
    NotificationControllerV2Test.setResponse(ResponseEntity.status(HttpStatus.NOT_MODIFIED)
        .build());
  }

  @Test
  void get_ok() {
    ConfigClient client = this.createClient();

    Endpoint endpoint = Endpoint.builder()
        .address("http://localhost:" + this.port)
        .build();

    Map<String, Long> details = new LinkedHashMap<>();
    details.put("someNamespace", 1L);
    details.put("anotherNamespace", 2L);
    NotificationMessages messages = NotificationMessages.builder()
        .details(details)
        .build();

    GetConfigOptions options = GetConfigOptions.builder()
        .appId("someAppId")
        .cluster("someCluster")
        .namespace("someNamespace")
        .releaseKey("someReleaseKey")
        .dataCenter("someDataCenter")
        .clientIp("someClientIp")
        .label("someLabel")
        .messages(messages)
        .build();
    GetConfigRequest request = GetConfigRequest.builder()
        .endpoint(endpoint)
        .options(options)
        .build();

    ReentrantLock clientLock = ConfigControllerTest.getClientLock();
    GetConfigResponse response;
    QueryConfigRequest lastRequest;
    clientLock.lock();
    try {
      this.setUpGetResponse(request);
      response = client.getConfig(request);
      lastRequest = ConfigControllerTest.getRequest();
    } finally {
      clientLock.unlock();
    }

    Assertions.assertNotNull(response);
    Assertions.assertEquals(GetConfigStatus.OK, response.getStatus());
    GetConfigResult config = response.getConfig();
    Assertions.assertEquals("someAppId", config.getAppId());
    Assertions.assertEquals("someCluster", config.getCluster());
    Assertions.assertEquals("someNamespace", config.getNamespaceName());
    Assertions.assertEquals("someReleaseKey", config.getReleaseKey());
    Map<String, String> configurations = config.getConfigurations();
    Assertions.assertEquals(2, configurations.size());
    Assertions.assertEquals("value1", configurations.get("someKey1"));
    Assertions.assertEquals("value2", configurations.get("someKey2"));

    assertConfigRequest(request, lastRequest);
  }

  private void setUpGetResponse(GetConfigRequest request) {
    GetConfigOptions options = request.getOptions();

    Map<String, String> configurations = new LinkedHashMap<>();
    configurations.put("someKey1", "value1");
    configurations.put("someKey2", "value2");

    ApolloConfig config = new ApolloConfig();
    config.setAppId(options.getAppId());
    config.setCluster(options.getCluster());
    config.setNamespaceName(options.getNamespace());
    config.setConfigurations(configurations);
    config.setReleaseKey(options.getReleaseKey());

    ConfigControllerTest.setResponse(ResponseEntity.ok()
        .body(config));
  }

  private static void assertConfigRequest(GetConfigRequest request,
      QueryConfigRequest lastRequest) {
    GetConfigOptions options = request.getOptions();

    Assertions.assertNotNull(lastRequest);
    Assertions.assertEquals(options.getAppId(), lastRequest.getAppId());
    Assertions.assertEquals(options.getCluster(), lastRequest.getCluster());
    Assertions.assertEquals(options.getNamespace(), lastRequest.getNamespace());
    Assertions.assertEquals(options.getReleaseKey(), lastRequest.getReleaseKey());
    Assertions.assertEquals(options.getDataCenter(), lastRequest.getDataCenter());
    Assertions.assertEquals(options.getClientIp(), lastRequest.getClientIp());
    Assertions.assertEquals(options.getLabel(), lastRequest.getLabel());
    NotificationMessages messages = options.getMessages();
    ApolloNotificationMessages lastRequestMessages = lastRequest.getMessages();
    Assertions.assertNotNull(lastRequestMessages);
    Map<String, Long> details = messages.getDetails();
    Map<String, Long> lastRequestMessagesDetails = lastRequestMessages.getDetails();
    Assertions.assertEquals(details.size(), lastRequestMessagesDetails.size());
    for (Entry<String, Long> entry : details.entrySet()) {
      Assertions.assertEquals(entry.getValue(), lastRequestMessagesDetails.get(entry.getKey()));
    }
  }


  @Test
  void get_not_modified() {
    ConfigClient client = this.createClient();

    Endpoint endpoint = Endpoint.builder()
        .address("http://localhost:" + this.port)
        .build();

    Map<String, Long> details = new LinkedHashMap<>();
    details.put("someNamespace", 1L);
    details.put("anotherNamespace", 2L);
    NotificationMessages messages = NotificationMessages.builder()
        .details(details)
        .build();

    GetConfigOptions options = GetConfigOptions.builder()
        .appId("someAppId")
        .cluster("someCluster")
        .namespace("someNamespace")
        .releaseKey("someReleaseKey")
        .dataCenter("someDataCenter")
        .clientIp("someClientIp")
        .label("someLabel")
        .messages(messages)
        .build();
    GetConfigRequest request = GetConfigRequest.builder()
        .endpoint(endpoint)
        .options(options)
        .build();

    ReentrantLock clientLock = ConfigControllerTest.getClientLock();
    GetConfigResponse response;
    QueryConfigRequest lastRequest;
    clientLock.lock();
    try {
      this.setUpGetResponse_not_modified(request);
      response = client.getConfig(request);
      lastRequest = ConfigControllerTest.getRequest();
    } finally {
      clientLock.unlock();
    }

    Assertions.assertNotNull(response);
    Assertions.assertEquals(GetConfigStatus.NOT_MODIFIED, response.getStatus());
    Assertions.assertNull(response.getConfig());

    assertConfigRequest(request, lastRequest);
  }

  private void setUpGetResponse_not_modified(GetConfigRequest request) {
    ConfigControllerTest.setResponse(ResponseEntity.status(HttpStatus.NOT_MODIFIED)
        .build());
  }
}
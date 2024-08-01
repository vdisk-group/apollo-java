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
package com.ctrip.framework.apollo.client.api.http.v1.server;

import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/notifications/v2")
@RestController
public class NotificationControllerV2Test {

  private static final Gson GSON = new Gson();

  private static final Type TYPE_REFERENCE =
      new TypeToken<List<ApolloConfigNotification>>() {
      }.getType();

  private static final ReentrantLock CLIENT_LOCK = new ReentrantLock();

  private static final ReentrantLock SERVER_LOCK = new ReentrantLock();

  private static volatile ResponseEntity<List<ApolloConfigNotification>> RESPONSE;

  private static volatile PollNotificationRequest REQUEST;

  public static ReentrantLock getClientLock() {
    return CLIENT_LOCK;
  }

  public static void setResponse(ResponseEntity<List<ApolloConfigNotification>> response) {
    if (!CLIENT_LOCK.isHeldByCurrentThread()) {
      throw new IllegalStateException("Client lock is not held by current thread");
    }
    SERVER_LOCK.lock();
    try {
      RESPONSE = response;
    } finally {
      SERVER_LOCK.unlock();
    }
  }

  public static PollNotificationRequest getRequest() {
    if (!CLIENT_LOCK.isHeldByCurrentThread()) {
      throw new IllegalStateException("Client lock is not held by current thread");
    }
    SERVER_LOCK.lock();
    try {
      return REQUEST;
    } finally {
      SERVER_LOCK.unlock();
    }
  }

  @GetMapping
  public ResponseEntity<List<ApolloConfigNotification>> pollNotification(
      @RequestParam(value = "appId") String appId,
      @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "notifications") String notificationsAsString,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "ip", required = false) String clientIp) {
    List<ApolloConfigNotification> clientNotifications = GSON.fromJson(notificationsAsString,
        TYPE_REFERENCE);

    PollNotificationRequest request = new PollNotificationRequest();
    request.setAppId(appId);
    request.setCluster(cluster);
    request.setNotifications(clientNotifications);
    request.setDataCenter(dataCenter);
    request.setClientIp(clientIp);

    ResponseEntity<List<ApolloConfigNotification>> response;
    SERVER_LOCK.lock();
    try {
      REQUEST = request;
      response = RESPONSE;
    } finally {
      SERVER_LOCK.unlock();
    }

    return response;
  }
}

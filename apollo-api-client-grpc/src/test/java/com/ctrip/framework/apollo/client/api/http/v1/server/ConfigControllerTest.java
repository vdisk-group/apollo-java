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

import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.google.gson.Gson;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/configs")
@RestController
public class ConfigControllerTest {

  private static final Gson GSON = new Gson();

  private static final ReentrantLock CLIENT_LOCK = new ReentrantLock();

  private static final ReentrantLock SERVER_LOCK = new ReentrantLock();

  private static volatile ResponseEntity<ApolloConfig> RESPONSE;

  private static volatile QueryConfigRequest REQUEST;

  public static ReentrantLock getClientLock() {
    return CLIENT_LOCK;
  }

  public static void setResponse(ResponseEntity<ApolloConfig> response) {
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

  public static QueryConfigRequest getRequest() {
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


  @GetMapping(value = "/{appId}/{clusterName}/{namespace:.+}")
  public ResponseEntity<ApolloConfig> queryConfig(@PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespace,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "releaseKey", defaultValue = "-1") String clientSideReleaseKey,
      @RequestParam(value = "ip", required = false) String clientIp,
      @RequestParam(value = "label", required = false) String clientLabel,
      @RequestParam(value = "messages", required = false) String messagesAsString) {

    ApolloNotificationMessages messages = GSON.fromJson(messagesAsString,
        ApolloNotificationMessages.class);

    QueryConfigRequest request = new QueryConfigRequest();
    request.setAppId(appId);
    request.setCluster(clusterName);
    request.setNamespace(namespace);
    request.setDataCenter(dataCenter);
    request.setReleaseKey(clientSideReleaseKey);
    request.setClientIp(clientIp);
    request.setLabel(clientLabel);
    request.setMessages(messages);

    ResponseEntity<ApolloConfig> response;
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

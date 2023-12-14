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

import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationDefinition;
import com.ctrip.framework.apollo.grpc.api.v1.config.NotificationServiceGrpc;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationRequest;
import com.ctrip.framework.apollo.grpc.api.v1.config.WatchNotificationResponse;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MockNotificationService extends NotificationServiceGrpc.NotificationServiceImplBase {

  private final Object lock = new Object();

  private final Map<String, CompletableFuture<StreamObserver<WatchNotificationResponse>>> mockWatchingMap = new LinkedHashMap<>();

  @Override
  public void watch(WatchNotificationRequest request,
      StreamObserver<WatchNotificationResponse> responseObserver) {
    String appId = request.getAppId();
    String cluster = request.getCluster();
    List<NotificationDefinition> notificationsList = request.getNotificationsList();
    synchronized (this.lock) {
      for (NotificationDefinition definition : notificationsList) {
        String namespaceName = definition.getNamespaceName();
        long notificationId = definition.getNotificationId();
        String mockKey = appId + "/" + cluster + "/" + namespaceName + "/" + notificationId;
        CompletableFuture<StreamObserver<WatchNotificationResponse>> future = this.mockWatchingMap.computeIfAbsent(
            mockKey, key -> new CompletableFuture<>());
        future.complete(responseObserver);
      }
    }
  }

  public Set<String> mockWatchingKeys() {
    return Collections.unmodifiableSet(this.mockWatchingMap.keySet());
  }

  public void completeWatching(long timeoutMillis, String mockKey,
      WatchNotificationResponse response) {
    this.finishWatching(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    });
  }

  private void finishWatching(long timeoutMillis, String mockKey,
      Consumer<StreamObserver<WatchNotificationResponse>> consumer) {
    CompletableFuture<StreamObserver<WatchNotificationResponse>> future;
    synchronized (this.lock) {
      future = this.mockWatchingMap.computeIfAbsent(mockKey, key -> new CompletableFuture<>());
    }

    boolean completed = false;
    try {
      StreamObserver<WatchNotificationResponse> responseObserver = CompletableFutureUtil.await(
          future,
          timeoutMillis);
      consumer.accept(responseObserver);
      completed = true;
    } finally {
      synchronized (this.lock) {
        this.mockWatchingMap.remove(mockKey, future);
        if (!completed) {
          if (future.isDone()) {
            StreamObserver<WatchNotificationResponse> responseObserver = CompletableFutureUtil.await(
                future, 0L);
            consumer.accept(responseObserver);
          }
        }
      }
    }
  }

  public void notModifiedWatching(long timeoutMillis, String mockKey) {
    this.finishWatching(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onCompleted();
    });
  }

  public void errorWatching(long timeoutMillis, String mockKey, StatusRuntimeException exception) {
    this.finishWatching(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onError(exception);
    });
  }

}

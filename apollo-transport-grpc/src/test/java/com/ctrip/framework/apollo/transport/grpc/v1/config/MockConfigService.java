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

import com.ctrip.framework.apollo.grpc.api.v1.config.ConfigServiceGrpc;
import com.ctrip.framework.apollo.grpc.api.v1.config.GetConfigRequest;
import com.ctrip.framework.apollo.grpc.api.v1.config.GetConfigResponse;
import io.grpc.stub.StreamObserver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MockConfigService extends ConfigServiceGrpc.ConfigServiceImplBase {

  private final Object lock = new Object();

  private final Map<String, CompletableFuture<StreamObserver<GetConfigResponse>>> mockMap = new LinkedHashMap<>();

  @Override
  public void getConfig(GetConfigRequest request,
      StreamObserver<GetConfigResponse> responseObserver) {
    String appId = request.getAppId();
    String cluster = request.getCluster();
    String namespace = request.getNamespace();
    String mockKey = appId + "/" + cluster + "/" + namespace;
    synchronized (this.lock) {
      CompletableFuture<StreamObserver<GetConfigResponse>> future = this.mockMap.computeIfAbsent(
          mockKey, key -> new CompletableFuture<>());
      future.complete(responseObserver);
    }
  }

  public void completeGetting(long timeoutMillis, String mockKey, GetConfigResponse response) {
    this.finishGetting(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    });
  }

  private void finishGetting(long timeoutMillis, String mockKey,
      Consumer<StreamObserver<GetConfigResponse>> consumer) {
    CompletableFuture<StreamObserver<GetConfigResponse>> future;
    synchronized (this.lock) {
      future = this.mockMap.computeIfAbsent(mockKey, key -> new CompletableFuture<>());
    }

    boolean completed = false;
    try {
      StreamObserver<GetConfigResponse> responseObserver = CompletableFutureUtil.await(future,
          timeoutMillis);
      consumer.accept(responseObserver);
      completed = true;
    } finally {
      synchronized (this.lock) {
        this.mockMap.remove(mockKey, future);
        if (!completed) {
          if (future.isDone()) {
            StreamObserver<GetConfigResponse> responseObserver = CompletableFutureUtil.await(future,
                0L);
            consumer.accept(responseObserver);
          }
        }
      }
    }
  }

  public void notModifiedGetting(long timeoutMillis, String mockKey) {
    this.finishGetting(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onCompleted();
    });
  }

  public void errorGetting(long timeoutMillis, String mockKey, Throwable exception) {
    this.finishGetting(timeoutMillis, mockKey, responseObserver -> {
      responseObserver.onError(exception);
    });
  }

}

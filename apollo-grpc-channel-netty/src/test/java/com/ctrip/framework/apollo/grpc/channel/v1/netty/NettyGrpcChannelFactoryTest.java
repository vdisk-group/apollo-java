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
package com.ctrip.framework.apollo.grpc.channel.v1.netty;

import com.ctrip.framework.apollo.grpc.channel.v1.api.DefaultGrpcChannelManager;
import com.ctrip.framework.apollo.test.net.SocketAddressUtil;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NettyGrpcChannelFactoryTest {

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void createChannel() throws IOException {
    HealthStatusManager healthStatusManager = new HealthStatusManager();
    int port = SocketAddressUtil.findFreePort();
    SocketAddress address = new InetSocketAddress("localhost", port);
    Server server = NettyServerBuilder.forAddress(address)
        .addService(healthStatusManager.getHealthService())
        .build();
    server.start();
    DefaultGrpcChannelManager nettyGrpcChannelManager = new DefaultGrpcChannelManager(
        new NettyGrpcChannelFactory());

    String endpoint = "dns:///localhost:" + port;
    ManagedChannel channel = nettyGrpcChannelManager.getChannel(endpoint);
    Assertions.assertNotNull(channel);

    HealthBlockingStub blockingStub = HealthGrpc.newBlockingStub(channel);
    HealthCheckRequest checkRequest = HealthCheckRequest.newBuilder()
        .setService("")
        .build();
    HealthCheckResponse checkResponse = blockingStub.check(checkRequest);
    Assertions.assertNotNull(checkResponse);
    ServingStatus status = checkResponse.getStatus();
    Assertions.assertEquals(ServingStatus.SERVING, status);
  }
}
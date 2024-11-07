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
package com.ctrip.framework.apollo.client.v1.grpc.channel.netty.shaded;

import com.ctrip.framework.apollo.grpc.channel.v1.api.GrpcChannelFactory;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

public class NettyGrpcChannelFactory implements GrpcChannelFactory {

  @Override
  public ManagedChannel createChannel(String endpoint) {
    ManagedChannel channel = NettyChannelBuilder.forTarget(endpoint)
        .usePlaintext()
        .build();
    return channel;
  }
}

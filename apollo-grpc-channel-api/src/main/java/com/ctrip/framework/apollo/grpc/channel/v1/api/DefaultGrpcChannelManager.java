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
package com.ctrip.framework.apollo.grpc.channel.v1.api;

import io.grpc.ManagedChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultGrpcChannelManager implements GrpcChannelManager {

  /**
   * channel factory
   */
  private final GrpcChannelFactory channelFactory;

  /**
   * channel store. endpoint -> channel
   */
  private final Map<String, ManagedChannel> channelMap;

  private final ReentrantLock mainLock;

  public DefaultGrpcChannelManager(GrpcChannelFactory channelFactory) {
    this.channelFactory = channelFactory;
    this.channelMap = new ConcurrentHashMap<>();
    this.mainLock = new ReentrantLock();
  }

  @Override
  public ManagedChannel getChannel(String endpoint) {
    ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
      return this.getChannelInternal(endpoint);
    } finally {
      mainLock.unlock();
    }
  }

  private ManagedChannel getChannelInternal(String endpoint) {
    ManagedChannel channel = this.channelMap.get(endpoint);
    if (channel != null) {
      return channel;
    }
    ManagedChannel newChannel = this.channelFactory.createChannel(endpoint);
    Objects.requireNonNull(newChannel, "newChannel");
    this.channelMap.put(endpoint, newChannel);
    return newChannel;
  }
}

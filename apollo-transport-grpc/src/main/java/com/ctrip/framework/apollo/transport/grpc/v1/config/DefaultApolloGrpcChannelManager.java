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

import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportEndpoint;
import io.grpc.ManagedChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultApolloGrpcChannelManager implements ApolloGrpcChannelManager {

  private final Lock lock;

  private final ConcurrentMap<String, ManagedChannel> channels;

  private final ApolloGrpcChannelFactory channelFactory;

  public DefaultApolloGrpcChannelManager(ApolloGrpcChannelFactory channelFactory) {
    this.lock = new ReentrantLock();
    this.channels = new ConcurrentHashMap<>();
    this.channelFactory = channelFactory;
  }

  @Override
  public void evict(Collection<ApolloTransportEndpoint> activeEndpoints) {
    Set<String> activeAddresses = this.getActiveAddresses(activeEndpoints);
    this.lock.lock();
    try {
      this.evictInternal(activeAddresses);
    } finally {
      this.lock.unlock();
    }
  }

  private Set<String> getActiveAddresses(Collection<ApolloTransportEndpoint> activeEndpoints) {
    Set<String> activeAddresses = new HashSet<>();
    for (ApolloTransportEndpoint activeEndpoint : activeEndpoints) {
      String address = activeEndpoint.getAddress();
      activeAddresses.add(address);
    }
    return activeAddresses;
  }

  private void evictInternal(Set<String> activeAddresses) {
    for (Iterator<Entry<String, ManagedChannel>> it = this.channels.entrySet().iterator();
        it.hasNext(); ) {
      Entry<String, ManagedChannel> entry = it.next();
      String address = entry.getKey();
      if (!activeAddresses.contains(address)) {
        ManagedChannel channel = entry.getValue();
        channel.shutdown();
        it.remove();
      }
    }
  }

  @Override
  public ManagedChannel getChannel(ApolloTransportEndpoint endpoint) {
    this.lock.lock();
    try {
      return this.getChannelInternal(endpoint);
    } finally {
      this.lock.unlock();
    }
  }

  private ManagedChannel getChannelInternal(ApolloTransportEndpoint endpoint) {
    ManagedChannel existedChannel = this.channels.get(endpoint.getAddress());
    if (existedChannel != null) {
      return existedChannel;
    }
    ManagedChannel newChannel = this.createChannel(endpoint);
    this.channels.put(endpoint.getAddress(), newChannel);
    return newChannel;
  }

  private ManagedChannel createChannel(ApolloTransportEndpoint endpoint) {
    return this.channelFactory.createChannel(endpoint);
  }
}

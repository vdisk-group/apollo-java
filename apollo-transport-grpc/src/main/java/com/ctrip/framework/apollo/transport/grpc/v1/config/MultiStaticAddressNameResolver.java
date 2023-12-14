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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class MultiStaticAddressNameResolver extends NameResolver {

  private final URI targetUri;

  private final String authority;

  private final List<EquivalentAddressGroup> addresses;

  private volatile NameResolver.Listener2 listener;

  public MultiStaticAddressNameResolver(URI targetUri, NameResolver.Args args) {
    Preconditions.checkNotNull(targetUri, "targetUri");
    Preconditions.checkNotNull(args, "args");
    this.targetUri = targetUri;
    this.authority = targetUri.getAuthority();
    this.addresses = this.toAddresses(targetUri, args);
  }

  private List<EquivalentAddressGroup> toAddresses(URI targetUri, Args args) {
    List<EquivalentAddressGroup> addresses = new ArrayList<>();
    String authority = targetUri.getAuthority();
    String[] parts = authority.split(",");
    for (String part : parts) {
      String address = part.trim();
      URI uri = URI.create("static://" + address);
      String host = uri.getHost();
      int port = uri.getPort();
      int actualPort;
      if (port > 0) {
        actualPort = port;
      } else {
        actualPort = args.getDefaultPort();
      }
      SocketAddress socketAddress = new InetSocketAddress(host, actualPort);
      addresses.add(new EquivalentAddressGroup(Collections.singletonList(socketAddress)));
    }
    return addresses;
  }

  @Override
  public String getServiceAuthority() {
    return this.authority;
  }

  @VisibleForTesting
  List<EquivalentAddressGroup> getAddresses() {
    return this.addresses;
  }

  @Override
  public void start(Listener2 listener) {
    Preconditions.checkState(this.listener == null, "already started");
    this.listener = Preconditions.checkNotNull(listener, "listener");
    this.resolve(listener);
  }

  private void resolve(Listener2 listener) {
    ResolutionResult result = ResolutionResult.newBuilder()
        .setAddresses(this.addresses)
        .build();
    listener.onResult(result);
  }

  @Override
  public void refresh() {
    Listener2 listener = this.listener;
    Preconditions.checkState(listener != null, "not started");
    this.resolve(listener);
  }

  @Override
  public void shutdown() {
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        MultiStaticAddressNameResolver.class.getSimpleName() + "[", "]")
        // fields
        .add("targetUri=" + this.targetUri)
        .add("addresses=" + this.addresses)
        .toString();
  }
}

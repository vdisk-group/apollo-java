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

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.SynchronizationContext;
import io.grpc.internal.AutoConfiguredLoadBalancerFactory;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.ScParser;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MultiStaticAddressNameResolverTest {

  private static final Logger log = LoggerFactory.getLogger(
      MultiStaticAddressNameResolverTest.class);

  @Test
  void toAddresses() {
    URI targetUri = URI.create("multi-static://127.0.0.1:8081,127.0.0.2:8080,127.0.0.3");
    NameResolver.Args args = NameResolver.Args.newBuilder()
        .setDefaultPort(8099)
        .setProxyDetector(GrpcUtil.NOOP_PROXY_DETECTOR)
        .setSynchronizationContext(new SynchronizationContext(
            (t, e) -> log.error("uncaughtException {}", e.getLocalizedMessage(), e)))
        .setServiceConfigParser(new ScParser(
            false,
            1,
            1,
            new AutoConfiguredLoadBalancerFactory(GrpcUtil.DEFAULT_LB_POLICY)))
        .build();
    MultiStaticAddressNameResolver nameResolver = new MultiStaticAddressNameResolver(targetUri,
        args);
    List<EquivalentAddressGroup> addresses = nameResolver.getAddresses();
    Assertions.assertNotNull(addresses);
    Assertions.assertEquals(3, addresses.size());
    Assertions.assertEquals(Collections.singletonList(new InetSocketAddress("127.0.0.1", 8081)),
        addresses.get(0).getAddresses());
    Assertions.assertEquals(Collections.singletonList(new InetSocketAddress("127.0.0.2", 8080)),
        addresses.get(1).getAddresses());
    Assertions.assertEquals(Collections.singletonList(new InetSocketAddress("127.0.0.3", 8099)),
        addresses.get(2).getAddresses());
  }
}
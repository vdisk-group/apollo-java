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
package com.ctrip.framework.apollo.grpc;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.spi.ApolloInjectorCustomizer;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigClientManager;
import com.ctrip.framework.apollo.transport.grpc.v1.config.ApolloGrpcChannelFactory;
import com.ctrip.framework.apollo.transport.grpc.v1.config.ApolloGrpcChannelManager;
import com.ctrip.framework.apollo.transport.grpc.v1.config.DefaultApolloGrpcChannelFactory;
import com.ctrip.framework.apollo.transport.grpc.v1.config.DefaultApolloGrpcChannelManager;
import com.ctrip.framework.apollo.util.ConfigUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ApolloGrpcInjectorCustomizer implements ApolloInjectorCustomizer {

  /**
   * the order of the injector customizer
   */
  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 300;

  private static final Map<Class<?>, Supplier<?>> INSTANCE_SUPPLIERS = initInstanceSuppliers();

  private static Map<Class<?>, Supplier<?>> initInstanceSuppliers() {
    Map<Class<?>, Supplier<?>> instanceSuppliers = new ConcurrentHashMap<>();
    ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    boolean transportGrpcEnabled = configUtil.isTransportGrpcEnabled();
    if (transportGrpcEnabled) {
      instanceSuppliers.put(ApolloGrpcChannelFactory.class, DefaultApolloGrpcChannelFactory::new);
      instanceSuppliers.put(ApolloGrpcChannelManager.class, () -> {
        ApolloGrpcChannelFactory channelFactory = ApolloInjector.getInstance(
            ApolloGrpcChannelFactory.class);
        return new DefaultApolloGrpcChannelManager(channelFactory);
      });
      instanceSuppliers.put(ApolloTransportConfigClientManager.class,
          GrpcApolloTransportConfigClientManager::new);
    }
    return instanceSuppliers;
  }

  private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();


  @Override
  public <T> T getInstance(Class<T> clazz) {
    @SuppressWarnings("unchecked")
    Supplier<T> instanceSupplier = (Supplier<T>) INSTANCE_SUPPLIERS.get(clazz);
    if (instanceSupplier == null) {
      return null;
    }
    return this.getInstance(clazz, instanceSupplier);
  }

  @SuppressWarnings("unchecked")
  private <T> T getInstance(Class<T> type, Supplier<T> instanceSupplier) {
    T instance = (T) INSTANCES.get(type);
    if (instance != null) {
      return instance;
    }
    // prebuild an newInstance to prevent dead lock when recursive call computeIfAbsent
    // https://bugs.openjdk.java.net/browse/JDK-8062841
    T newInstance = instanceSupplier.get();
    return (T) INSTANCES.computeIfAbsent(type, key -> newInstance);
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    return null;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

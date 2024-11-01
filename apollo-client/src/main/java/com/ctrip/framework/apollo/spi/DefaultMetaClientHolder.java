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
package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultMetaClientHolder implements MetaClientHolder {

  @Override
  public MetaClient getMetaClient() {
    return LazyHolder.CLIENT;
  }

  /**
   * lazy holder
   */
  private static class LazyHolder {

    static final MetaClient CLIENT = initClient();

    private static MetaClient initClient() {
      ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
      String clientType = configUtil.getClientType();
      MetaClientProvider provider = getProvider(clientType);
      return provider.createClient();
    }

    private static MetaClientProvider getProvider(String clientType) {
      List<MetaClientProvider> providers = ServiceBootstrap.loadAllOrdered(
          MetaClientProvider.class);
      if (providers.isEmpty()) {
        throw new IllegalStateException(String.format(
            "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
            MetaClientProvider.class.getName()));
      }
      List<MetaClientProvider> typedProviders = new ArrayList<>(providers.size());
      for (MetaClientProvider provider : providers) {
        if (Objects.equals(clientType, provider.getClientType())) {
          typedProviders.add(provider);
        }
      }
      if (typedProviders.isEmpty()) {
        throw new IllegalStateException(String.format(
            "No implementation defined in /META-INF/services/%s for [client-type:%s], please check whether the file exists and has the right implementation class!",
            MetaClientProvider.class.getName(), clientType));
      }
      return typedProviders.get(0);
    }
  }
}

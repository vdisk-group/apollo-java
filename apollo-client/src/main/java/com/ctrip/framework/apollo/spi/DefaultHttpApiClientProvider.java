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
import com.ctrip.framework.apollo.client.v1.api.ApiClient;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;
import com.ctrip.framework.apollo.client.v1.http.HttpApiClient;
import com.ctrip.framework.apollo.client.v1.http.config.HttpConfigClient;
import com.ctrip.framework.apollo.client.v1.http.config.HttpConfigClientFactory;
import com.ctrip.framework.apollo.client.v1.http.config.HttpConfigClientProperties;
import com.ctrip.framework.apollo.client.v1.http.meta.HttpMetaClient;
import com.ctrip.framework.apollo.client.v1.http.meta.HttpMetaClientFactory;
import com.ctrip.framework.apollo.client.v1.http.meta.HttpMetaClientProperties;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.util.ConfigUtil;

public class DefaultHttpApiClientProvider implements ApiClientProvider {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 200;

  // 90 seconds, should be longer than server side's long polling timeout, which is now 60 seconds
  private static final int LONG_POLLING_READ_TIMEOUT = 90_000;

  @Override
  public String getClientType() {
    return "http";
  }

  @Override
  public String getName() {
    return "http-default";
  }

  @Override
  public ApiClient createClient() {
    HttpTransport httpTransport = ApolloInjector.getInstance(HttpTransport.class);

    ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);

    HttpMetaClientProperties metaClientProperties = HttpMetaClientProperties.builder()
        .discoveryConnectTimeout(configUtil.getDiscoveryConnectTimeout())
        .discoveryReadTimeout(configUtil.getDiscoveryConnectTimeout())
        .build();
    HttpMetaClient metaClient = HttpMetaClientFactory.createClient(httpTransport, metaClientProperties);

    HttpConfigClientProperties configClientProperties = HttpConfigClientProperties.builder()
        .watchNotificationConnectTimeout(configUtil.getConnectTimeout())
        .watchNotificationReadTimeout(LONG_POLLING_READ_TIMEOUT)
        .getConfigConnectTimeout(configUtil.getConnectTimeout())
        .getConfigReadTimeout(configUtil.getReadTimeout())
        .build();

    HttpConfigClient configClient = HttpConfigClientFactory.createClient(httpTransport, configClientProperties);
    return new HttpApiClient(metaClient, configClient);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

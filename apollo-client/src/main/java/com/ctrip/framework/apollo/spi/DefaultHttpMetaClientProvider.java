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
import com.ctrip.framework.apollo.client.api.http.v1.meta.HttpMetaClientFactory;
import com.ctrip.framework.apollo.client.api.http.v1.meta.HttpMetaClientProperties;
import com.ctrip.framework.apollo.client.api.v1.meta.MetaClient;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.util.ConfigUtil;

public class DefaultHttpMetaClientProvider implements MetaClientProvider {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 200;

  @Override
  public String getClientType() {
    return "http";
  }

  @Override
  public String getName() {
    return "http-default";
  }

  @Override
  public MetaClient createClient() {
    HttpTransport httpTransport = ApolloInjector.getInstance(HttpTransport.class);

    ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);

    HttpMetaClientProperties properties = HttpMetaClientProperties.builder()
        .discoveryConnectTimeout(configUtil.getDiscoveryConnectTimeout())
        .discoveryReadTimeout(configUtil.getDiscoveryConnectTimeout())
        .build();

    return HttpMetaClientFactory.createClient(httpTransport, properties);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

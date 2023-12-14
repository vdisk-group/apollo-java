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
package com.ctrip.framework.apollo.util.http;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigClient;
import com.ctrip.framework.apollo.transport.api.v1.config.ApolloTransportConfigClientManager;
import com.ctrip.framework.apollo.transport.http.v1.config.ApolloHttpTransportConfigClient;

public class DefaultApolloTransportConfigClientManager implements
    ApolloTransportConfigClientManager {

  private final ApolloTransportConfigClient client;

  public DefaultApolloTransportConfigClientManager() {
    HttpClient httpClient = ApolloInjector.getInstance(HttpClient.class);
    DefaultApolloCoreHttpClient apolloHttpClient = new DefaultApolloCoreHttpClient(httpClient);
    this.client = new ApolloHttpTransportConfigClient(apolloHttpClient);
  }

  @Override
  public ApolloTransportConfigClient getClient() {
    return this.client;
  }
}

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

import com.ctrip.framework.apollo.client.api.http.v1.config.HttpConfigClientFactory;
import com.ctrip.framework.apollo.client.api.http.v1.config.HttpConfigClientProperties;
import com.ctrip.framework.apollo.client.api.v1.config.ConfigClient;
import com.ctrip.framework.apollo.core.spi.Ordered;

public class DefaultHttpConfigClientProvider implements ConfigClientProvider {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 200;

  @Override
  public String getName() {
    return "http";
  }

  @Override
  public ConfigClient createClient() {
    HttpConfigClientProperties properties = HttpConfigClientProperties.builder()
        .watchNotificationConnectTimeout(1_000)
        .watchNotificationReadTimeout(90_000)
        .getConfigConnectTimeout(1_000)
        .getConfigReadTimeout(5_000)
        .build();
    return HttpConfigClientFactory.createClient(properties);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

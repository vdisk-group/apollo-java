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
package com.ctrip.framework.apollo.client.api.http.v1.config;

import com.ctrip.framework.apollo.client.api.v1.config.ConfigClient;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportFactory;
import com.ctrip.framework.apollo.core.http.HttpTransportProperties;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.Objects;

public class HttpConfigClientFactory {

  private HttpConfigClientFactory() {
    throw new UnsupportedOperationException();
  }

  public static ConfigClient createClient(HttpConfigClientProperties properties) {

    HttpTransportFactory transportFactory = ServiceBootstrap.loadPrimary(
        HttpTransportFactory.class);

    HttpTransportProperties watchProperties = HttpTransportProperties.builder()
        .defaultConnectTimeout(properties.getWatchNotificationConnectTimeout())
        .defaultReadTimeout(properties.getWatchNotificationReadTimeout())
        .build();
    HttpTransport watchTransport = transportFactory.create(watchProperties);
    Objects.requireNonNull(watchTransport, "watchTransport");

    HttpTransportProperties getProperties = HttpTransportProperties.builder()
        .defaultConnectTimeout(properties.getGetConfigConnectTimeout())
        .defaultReadTimeout(properties.getGetConfigReadTimeout())
        .build();
    HttpTransport getTransport = transportFactory.create(getProperties);
    Objects.requireNonNull(getTransport, "getTransport");

    return new HttpConfigClient(watchTransport, getTransport);
  }
}

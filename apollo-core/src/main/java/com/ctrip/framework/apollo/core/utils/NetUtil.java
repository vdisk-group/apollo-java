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
package com.ctrip.framework.apollo.core.utils;

import com.ctrip.framework.apollo.core.http.HttpPingClient;
import com.ctrip.framework.apollo.core.http.HttpPingClientProvider;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.Objects;

/**
 * Created by gl49 on 2018/6/8.
 */
public class NetUtil {

  private static final HttpPingClientProvider HTTP_PING_CLIENT_PROVIDER;

  static {
    HttpPingClientProvider provider = ServiceBootstrap.loadPrimary(
        HttpPingClientProvider.class);
    HTTP_PING_CLIENT_PROVIDER = Objects.requireNonNull(provider,
        "HttpPingClientProvider must not be null");
  }

  /**
   * ping the url, return true if ping ok, false otherwise
   */
  public static boolean pingUrl(String address) {
    HttpPingClient httpPingClient = HTTP_PING_CLIENT_PROVIDER.get();
    Objects.requireNonNull(httpPingClient, "HttpPingClient must not be null");
    return httpPingClient.pingUrl(address);
  }
}

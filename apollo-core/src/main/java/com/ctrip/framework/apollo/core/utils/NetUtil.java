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

import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportFactory;
import com.ctrip.framework.apollo.core.http.HttpTransportProperties;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.Objects;

/**
 * Created by gl49 on 2018/6/8.
 */
public class NetUtil {

  private static final int DEFAULT_TIMEOUT_IN_SECONDS = 5000;

  private static final HttpTransport TRANSPORT = initTransport();

  private static HttpTransport initTransport() {
    HttpTransportFactory factory = ServiceBootstrap.loadPrimary(
        HttpTransportFactory.class);
    HttpTransportProperties properties = HttpTransportProperties.builder()
        .defaultConnectTimeout(DEFAULT_TIMEOUT_IN_SECONDS)
        .defaultReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS)
        .build();
    HttpTransport transport = factory.create(properties);
    Objects.requireNonNull(transport, "transport");
    return transport;
  }

  /**
   * ping the url, return true if ping ok, false otherwise
   */
  public static boolean pingUrl(String address) {
    try {
      HttpTransportRequest request = HttpTransportRequest.builder()
          .url(address)
          .build();
      HttpTransportResponse<Void> httpTransportResponse = TRANSPORT.doGet(request);
      int statusCode = httpTransportResponse.getStatusCode();
      return (200 <= statusCode && statusCode <= 399);
    } catch (Throwable ignore) {
    }
    return false;
  }
}

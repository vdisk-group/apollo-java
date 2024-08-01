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

import com.ctrip.framework.apollo.client.api.http.v1.transport.DefaultHttpTransport;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpTransport;
import com.ctrip.framework.apollo.core.spi.Ordered;

public class DefaultConfigHttpTransportFactory implements ConfigHttpTransportFactory {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 100;

  private static final DefaultHttpTransport HTTP_TRANSPORT = new DefaultHttpTransport();

  @Override
  public HttpTransport getHttpTransport() {
    return HTTP_TRANSPORT;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
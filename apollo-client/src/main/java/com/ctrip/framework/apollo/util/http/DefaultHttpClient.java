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
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportFactory;
import com.ctrip.framework.apollo.core.http.HttpTransportProperties;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Deprecated
public class DefaultHttpClient implements HttpClient {

  private final HttpTransportWrapper wrapper;

  public DefaultHttpClient() {
    ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);

    HttpTransportFactory transportFactory = ServiceBootstrap.loadPrimary(
        HttpTransportFactory.class);

    HttpTransportProperties properties = HttpTransportProperties.builder()
        .defaultConnectTimeout(configUtil.getConnectTimeout())
        .defaultReadTimeout(configUtil.getReadTimeout())
        .build();

    HttpTransport transport = transportFactory.create(properties);
    Objects.requireNonNull(transport, "transport");
    this.wrapper = new HttpTransportWrapper(transport);
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Class<T> responseType)
      throws ApolloConfigException {
    return this.wrapper.doGet(httpRequest, responseType);
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Type responseType)
      throws ApolloConfigException {
    return this.wrapper.doGet(httpRequest, responseType);
  }
}

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
package com.ctrip.framework.apollo.util.http.tls;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class HttpTlsContextHolder {

  @Nullable
  private final HttpTlsContext httpTlsContext;

  public HttpTlsContextHolder() {
    HttpTlsConfig httpTlsConfig = ApolloInjector.getInstance(HttpTlsConfig.class);
    this.httpTlsContext = this.initHttpTlsContext(httpTlsConfig);
  }

  private HttpTlsContext initHttpTlsContext(HttpTlsConfig httpTlsConfig) {
    List<HttpTlsFactory> factories = ServiceBootstrap.loadAllOrdered(HttpTlsFactory.class);
    for (HttpTlsFactory factory : factories) {
      if (factory.isAvailable()) {
        return factory.create(httpTlsConfig);
      }
    }
    return null;
  }

  @Nullable
  public HttpTlsContext getHttpTlsContext() {
    return this.httpTlsContext;
  }
}

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
package com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsContext;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsContextHolder;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class HttpComponentsClientHttpConnectorFactory implements ClientHttpConnectorFactory {

  @Override
  public HttpComponentsClientHttpConnector create() {
    HttpTlsContextHolder httpTlsContextHolder = ApolloInjector.getInstance(
        HttpTlsContextHolder.class);
    TlsStrategy tlsStrategy = this.initTlsStrategy(httpTlsContextHolder);
    PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
        .setTlsStrategy(tlsStrategy)
        .build();
    CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
        .setConnectionManager(connectionManager)
        .build();
    return new HttpComponentsClientHttpConnector(httpClient);
  }

  private TlsStrategy initTlsStrategy(HttpTlsContextHolder httpTlsContextHolder) {
    HttpTlsContext httpTlsContext = httpTlsContextHolder.getHttpTlsContext();
    if (httpTlsContext == null) {
      return null;
    }
    SSLContext sslContext = httpTlsContext.getSslContext();
    if (sslContext == null) {
      return null;
    }
    return new DefaultClientTlsStrategy(sslContext, httpTlsContext.getHostnameVerifier());
  }
}

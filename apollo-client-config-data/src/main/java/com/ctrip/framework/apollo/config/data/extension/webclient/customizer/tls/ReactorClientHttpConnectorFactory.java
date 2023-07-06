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
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.spi.ReactorClientHttpTlsFactory;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsConfig;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.List;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ReactorClientHttpConnectorFactory implements ClientHttpConnectorFactory {

  @Override
  public ReactorClientHttpConnector create() {
    HttpTlsConfig tlsConfig = ApolloInjector.getInstance(HttpTlsConfig.class);
    SslProvider sslProvider = this.initSslProvider(tlsConfig);
    HttpClient builder = HttpClient.create();
    HttpClient httpClient;
    if (sslProvider != null) {
      httpClient = builder.secure(sslProvider);
    } else {
      httpClient = builder.secure();
    }
    return new ReactorClientHttpConnector(httpClient);
  }

  private SslProvider initSslProvider(HttpTlsConfig httpTlsConfig) {
    List<ReactorClientHttpTlsFactory> factories = ServiceBootstrap.loadAllOrdered(
        ReactorClientHttpTlsFactory.class);
    for (ReactorClientHttpTlsFactory factory : factories) {
      if (factory.isAvailable()) {
        return factory.create(httpTlsConfig);
      }
    }
    return null;
  }
}

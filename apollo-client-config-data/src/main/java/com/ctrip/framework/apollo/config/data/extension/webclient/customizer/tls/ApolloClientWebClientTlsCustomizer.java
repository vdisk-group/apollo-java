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

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.client.WebClient.Builder;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientWebClientTlsCustomizer implements WebClientCustomizer {

  private static final boolean reactorClientPresent;

  private static final boolean jettyClientPresent;

  private static final boolean httpComponentsClientPresent;

  static {
    ClassLoader loader = ApolloClientWebClientTlsCustomizerFactory.class.getClassLoader();
    reactorClientPresent = ClassUtils.isPresent("reactor.netty.http.client.HttpClient", loader);
    jettyClientPresent = ClassUtils.isPresent("org.eclipse.jetty.client.HttpClient", loader);
    httpComponentsClientPresent =
        ClassUtils.isPresent("org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient",
            loader) &&
            ClassUtils.isPresent("org.apache.hc.core5.reactive.ReactiveDataConsumer", loader);
  }

  public ApolloClientWebClientTlsCustomizer() {
  }

  public static boolean isAvailable() {
    return reactorClientPresent || jettyClientPresent || httpComponentsClientPresent;
  }

  static boolean reactorClientPresent() {
    return reactorClientPresent;
  }

  @Override
  public void customize(Builder webClientBuilder) {
    ClientHttpConnector connector = this.initConnector();
    webClientBuilder.clientConnector(connector);
  }

  private ClientHttpConnector initConnector() {
    if (reactorClientPresent) {
      return new ReactorClientHttpConnectorFactory().create();
    } else if (jettyClientPresent) {
      return new JettyClientHttpConnectorFactory().create();
    } else if (httpComponentsClientPresent) {
      return new HttpComponentsClientHttpConnectorFactory().create();
    }
    throw new IllegalStateException("No suitable default ClientHttpConnector found");
  }
}

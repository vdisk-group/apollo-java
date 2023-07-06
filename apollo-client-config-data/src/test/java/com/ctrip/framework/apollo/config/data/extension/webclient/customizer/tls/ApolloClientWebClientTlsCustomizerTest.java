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

import com.ctrip.framework.apollo.config.data.extension.webclient.ApolloWebClientHttpClient;
import com.ctrip.framework.apollo.config.data.injector.ApolloMockInjectorCustomizer;
import com.ctrip.framework.apollo.core.http.HttpPingClient;
import com.ctrip.framework.apollo.util.http.HttpClient;
import com.ctrip.framework.apollo.util.http.HttpClientV2;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsConfig;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsContextHolder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientWebClientTlsCustomizerTest {

  public static HttpClient createWebClient(HttpTlsConfig config,
      ClientHttpConnectorFactory factory) {
    ApolloMockInjectorCustomizer.register(HttpTlsConfig.class, () -> config);
    HttpTlsContextHolder httpTlsContextHolder = new HttpTlsContextHolder();
    ApolloMockInjectorCustomizer.register(HttpTlsContextHolder.class, () -> httpTlsContextHolder);

    ClientHttpConnector connector = factory.create();
    WebClient webClient = WebClient.builder()
        .clientConnector(connector)
        .build();

    ApolloWebClientHttpClient httpClient = new ApolloWebClientHttpClient(webClient);
    ApolloMockInjectorCustomizer.register(HttpClient.class, () -> httpClient);
    ApolloMockInjectorCustomizer.register(HttpPingClient.class, () -> httpClient);
    ApolloMockInjectorCustomizer.register(HttpClientV2.class, () -> httpClient);
    return httpClient;
  }
}
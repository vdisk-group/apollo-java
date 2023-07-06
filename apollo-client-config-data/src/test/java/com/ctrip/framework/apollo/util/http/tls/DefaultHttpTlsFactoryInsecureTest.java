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

import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.ApolloClientWebClientTlsCustomizerTest;
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.ClientHttpConnectorFactory;
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.HttpComponentsClientHttpConnectorFactory;
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.JettyClientHttpConnectorFactory;
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.ReactorClientHttpConnectorFactory;
import com.ctrip.framework.apollo.config.data.injector.ApolloMockInjectorCustomizer;
import com.ctrip.framework.apollo.core.utils.NetUtil;
import com.ctrip.framework.apollo.util.http.HttpClient;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test-tls-insecure"})
@SpringBootTest(classes = {
    DefaultHttpTlsFactoryTestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultHttpTlsFactoryInsecureTest {

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    ApolloMockInjectorCustomizer.clear();
  }

  @AfterEach
  void tearDown() {
    ApolloMockInjectorCustomizer.clear();
  }

  @Test
  void testDefaultClientTls() {
    HttpTlsConfig config = this.initTlsConfig();

    HttpClient httpClient = DefaultHttpTlsFactoryTest.createHttpClient(config);

    this.execute(httpClient);
  }

  private HttpTlsConfig initTlsConfig() {
    return HttpTlsConfig.builder()
        .enabled(true)
        .trustManagerType("IN_SECURE")
        .hostnameVerifierType("EXACT_SERVER_NAME")
        .hostnameVerifierExactServerNames(Collections.singletonList("apollo-test-cert"))
        .build();
  }

  private void execute(HttpClient httpClient) {
    boolean result = NetUtil.pingUrl("https://localhost:" + this.port + "/ping");
    Assertions.assertTrue(result);

    HttpRequest httpRequest = new HttpRequest("https://localhost:" + this.port + "/test");
    HttpResponse<Object> httpResponse = httpClient.doGet(httpRequest, Object.class);
    Assertions.assertEquals(200, httpResponse.getStatusCode());
  }

  @Test
  void testReactorClientTls() {
    HttpTlsConfig config = this.initTlsConfig();

    ClientHttpConnectorFactory factory = new ReactorClientHttpConnectorFactory();
    HttpClient httpClient = ApolloClientWebClientTlsCustomizerTest.createWebClient(config, factory);

    this.execute(httpClient);
  }

  @Test
  void testHttpComponentsClientTls() {
    HttpTlsConfig config = this.initTlsConfig();

    ClientHttpConnectorFactory factory = new HttpComponentsClientHttpConnectorFactory();
    HttpClient httpClient = ApolloClientWebClientTlsCustomizerTest.createWebClient(config, factory);

    this.execute(httpClient);
  }

  @Test
  void testJettyClientTls() {
    HttpTlsConfig config = this.initTlsConfig();

    ClientHttpConnectorFactory factory = new JettyClientHttpConnectorFactory();
    HttpClient httpClient = ApolloClientWebClientTlsCustomizerTest.createWebClient(config, factory);

    this.execute(httpClient);
  }
}
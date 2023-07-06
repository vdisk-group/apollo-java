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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.http.client.reactive.JettyClientHttpConnector;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class JettyClientHttpConnectorFactory implements ClientHttpConnectorFactory {

  @Override
  public JettyClientHttpConnector create() {
    HttpTlsContextHolder httpTlsContextHolder = ApolloInjector.getInstance(
        HttpTlsContextHolder.class);
    SslContextFactory sslContextFactory = this.initSslContextFactory(httpTlsContextHolder);
    HttpClient httpClient = new HttpClient(sslContextFactory);
    return new JettyClientHttpConnector(httpClient);
  }

  private SslContextFactory initSslContextFactory(HttpTlsContextHolder httpTlsContextHolder) {
    HttpTlsContext httpTlsContext = httpTlsContextHolder.getHttpTlsContext();
    if (httpTlsContext == null) {
      return null;
    }
    SSLContext sslContext = httpTlsContext.getSslContext();
    if (sslContext == null) {
      return null;
    }
    SslContextFactory sslContextFactory = new SslContextFactory.Client();
    sslContextFactory.setSslContext(sslContext);
    HostnameVerifier hostnameVerifier = httpTlsContext.getHostnameVerifier();
    sslContextFactory.setHostnameVerifier(hostnameVerifier);
    if (hostnameVerifier == null) {
      // strict mode
      sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
    } else {
      // custom mode
      sslContextFactory.setEndpointIdentificationAlgorithm("");
    }
    return sslContextFactory;
  }
}

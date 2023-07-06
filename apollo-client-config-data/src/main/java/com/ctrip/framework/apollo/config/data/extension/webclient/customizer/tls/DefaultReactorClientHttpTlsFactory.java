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

import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.spi.ReactorClientHttpTlsFactory;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsConfig;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsExtendFactory;
import com.ctrip.framework.apollo.util.http.tls.X509HostnameVerifier;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.SslProvider.Builder;
import reactor.netty.tcp.TcpSslContextSpec;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class DefaultReactorClientHttpTlsFactory implements ReactorClientHttpTlsFactory {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 100;

  private final HttpTlsExtendFactory factory;

  public DefaultReactorClientHttpTlsFactory() {
    this.factory = this.initFactory();
  }

  private HttpTlsExtendFactory initFactory() {
    List<HttpTlsExtendFactory> factories = ServiceBootstrap.loadAllOrdered(
        HttpTlsExtendFactory.class);
    for (HttpTlsExtendFactory factory : factories) {
      if (factory.isExtendAvailable()) {
        return factory;
      }
    }
    throw new IllegalStateException("no HttpTlsExtendFactory available");
  }

  @Override
  public boolean isAvailable() {
    return ApolloClientWebClientTlsCustomizer.reactorClientPresent();
  }

  @Nullable
  @Override
  public SslProvider create(HttpTlsConfig tlsConfig) {
    if (!tlsConfig.isEnabled()) {
      return null;
    }
    @Nullable
    X509HostnameVerifier hostnameVerifier = this.factory.initHostnameVerifier(tlsConfig);
    TcpSslContextSpec spec;
    try {
      spec = this.initTcpSslContextSpec(tlsConfig, hostnameVerifier);
    } catch (Exception e) {
      throw new IllegalStateException(
          "init sslContext failed " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(),
          e);
    }
    Builder builder = SslProvider.builder().sslContext(spec);
    this.configureHostnameVerifier(builder, hostnameVerifier);
    return builder.build();
  }

  private void configureHostnameVerifier(Builder builder,
      @Nullable X509HostnameVerifier hostnameVerifier) {
    String endpointIdentificationAlgorithm = this.getEndpointIdentificationAlgorithm(
        hostnameVerifier);
    builder.handlerConfigurator(handler -> {
      SSLEngine sslEngine = handler.engine();
      SSLParameters sslParameters = sslEngine.getSSLParameters();
      sslParameters.setEndpointIdentificationAlgorithm(endpointIdentificationAlgorithm);
      sslEngine.setSSLParameters(sslParameters);
    });
  }

  private String getEndpointIdentificationAlgorithm(
      @Nullable X509HostnameVerifier hostnameVerifier) {
    if (hostnameVerifier == null) {
      // strict mode
      return "HTTPS";
    } else {
      // custom mode
      return "";
    }
  }

  private TcpSslContextSpec initTcpSslContextSpec(HttpTlsConfig tlsConfig,
      @Nullable X509HostnameVerifier hostnameVerifier) throws Exception {
    @Nullable
    KeyManagerFactory keyManagerFactory = this.factory.initKeyManagerFactory(tlsConfig);
    @Nullable
    TrustManagerFactory trustManagerFactory = this.factory.initTrustManagerFactory(
        tlsConfig);
    TrustManagerFactory wrappedTrustManagerFactory = this.wrapTrustManagerFactory(
        trustManagerFactory, hostnameVerifier);
    String protocol = tlsConfig.getProtocol();
    return TcpSslContextSpec.forClient().configure(builder -> {
      if (keyManagerFactory != null) {
        builder.keyManager(keyManagerFactory);
      }
      builder.trustManager(wrappedTrustManagerFactory);
      if (protocol != null) {
        builder.protocols(protocol);
      }
    });
  }

  private TrustManagerFactory wrapTrustManagerFactory(
      @Nullable TrustManagerFactory trustManagerFactory,
      @Nullable X509HostnameVerifier hostnameVerifier) {
    if (trustManagerFactory == null) {
      return null;
    }
    if (hostnameVerifier == null) {
      return trustManagerFactory;
    }
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
    if (trustManagers == null || trustManagers.length == 0) {
      return trustManagerFactory;
    }
    // checked by HostnameVerifierTrustManagerWrapper
    TrustManager[] wrappedTrustManagers = this.wrapTrustManagers(trustManagers, hostnameVerifier);
    return new TrustManagerFactoryWrapper(wrappedTrustManagers);
  }

  private TrustManager[] wrapTrustManagers(TrustManager[] trustManagers,
      X509HostnameVerifier hostnameVerifier) {
    TrustManager[] wrappedTrustManagers = new TrustManager[trustManagers.length];
    for (int i = 0; i < trustManagers.length; i++) {
      TrustManager trustManager = trustManagers[i];
      if (trustManager == null) {
        throw new NullPointerException("trustManagers[" + i + "]");
      }
      wrappedTrustManagers[i] = new HostnameVerifierTrustManagerWrapper(
          (X509TrustManager) trustManager, hostnameVerifier);
    }
    return wrappedTrustManagers;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

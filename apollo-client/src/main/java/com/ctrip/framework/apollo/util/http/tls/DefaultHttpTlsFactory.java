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

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.google.common.base.Strings;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class DefaultHttpTlsFactory implements HttpTlsFactory, HttpTlsExtendFactory {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 100;

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public boolean isExtendAvailable() {
    return true;
  }

  @Nullable
  @Override
  public HttpTlsContext create(HttpTlsConfig tlsConfig) {
    Objects.requireNonNull(tlsConfig, "tlsConfig");
    if (!tlsConfig.isEnabled()) {
      return null;
    }
    SSLContext sslContext;
    try {
      sslContext = this.initSslContext(tlsConfig);
    } catch (Exception e) {
      throw new IllegalStateException(
          "init sslContext failed " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(),
          e);
    }
    HostnameVerifier hostnameVerifier = this.initHostnameVerifier(tlsConfig);
    return new HttpTlsContext(sslContext, hostnameVerifier);
  }

  private SSLContext initSslContext(HttpTlsConfig tlsConfig) throws Exception {
    KeyManagerFactory keyManagerFactory = this.initKeyManagerFactory(tlsConfig);
    TrustManagerFactory trustManagerFactory = this.initTrustManagerFactory(tlsConfig);

    String protocol = tlsConfig.getProtocol();
    String actualProtocol = protocol != null ? protocol : "TLS";
    SSLContext sslContext = SSLContext.getInstance(actualProtocol);
    KeyManager[] keyManagers =
        keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : new KeyManager[0];
    TrustManager[] trustManagers =
        trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : new TrustManager[0];
    sslContext.init(keyManagers, trustManagers, null);
    return sslContext;
  }

  @Nullable
  @Override
  public KeyManagerFactory initKeyManagerFactory(HttpTlsConfig tlsConfig) throws Exception {
    @Nullable
    String actualKeyPassword = this.getActualKeyPassword(tlsConfig);
    KeyStore keyStore = this.loadKeyStore(tlsConfig, actualKeyPassword);
    if (keyStore == null) {
      return null;
    }
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
        KeyManagerFactory.getDefaultAlgorithm());
    char[] keyPassword = actualKeyPassword != null ? actualKeyPassword.toCharArray() : null;
    keyManagerFactory.init(keyStore, keyPassword);
    return keyManagerFactory;
  }

  private String getActualKeyPassword(HttpTlsConfig tlsConfig) {
    String keyPassword = tlsConfig.getKeyPassword();
    String keyStorePassword = tlsConfig.getKeyStorePassword();
    return !Strings.isNullOrEmpty(keyPassword) ? keyPassword : keyStorePassword;
  }

  private KeyStore loadKeyStore(HttpTlsConfig tlsConfig, String actualKeyPassword)
      throws Exception {
    String keyStore = tlsConfig.getKeyStore();
    if (Strings.isNullOrEmpty(keyStore)) {
      return null;
    }
    return HttpTlsFactoryUtil.loadStore(keyStore, tlsConfig.getKeyStoreType(),
        tlsConfig.getKeyStoreProvider(), actualKeyPassword);
  }

  @Nullable
  @Override
  public TrustManagerFactory initTrustManagerFactory(HttpTlsConfig tlsConfig) throws Exception {
    String type = tlsConfig.getTrustManagerType();
    String actualType = !Strings.isNullOrEmpty(type) ? type : "WELL_KNOWN_CA";
    switch (actualType) {
      case "WELL_KNOWN_CA":
        return this.getWellKnownCaTrustManagerFactory(tlsConfig);
      case "SELF_SIGNED_CA":
        return this.getSelfSignedCaTrustManagerFactory(tlsConfig);
      case "SELF_SIGNED_CERT":
        return this.getSelfSignedCertTrustManagerFactory(tlsConfig);
      case "IN_SECURE":
        return this.getInsecureTrustManagerFactory(tlsConfig);
      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

  }

  private TrustManagerFactory getWellKnownCaTrustManagerFactory(HttpTlsConfig tlsConfig)
      throws KeyStoreException, NoSuchAlgorithmException {
    // just use the jdk default
    return null;
  }

  private TrustManagerFactory getSelfSignedCaTrustManagerFactory(HttpTlsConfig tlsConfig)
      throws Exception {
    KeyStore store = this.loadTrustStore(tlsConfig);
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(store);
    return trustManagerFactory;
  }

  private KeyStore loadTrustStore(HttpTlsConfig tlsConfig) throws Exception {
    return HttpTlsFactoryUtil.loadStore(tlsConfig.getTrustStore(), tlsConfig.getTrustStoreType(),
        tlsConfig.getTrustStoreProvider(), tlsConfig.getTrustStorePassword());
  }


  private TrustManagerFactory getSelfSignedCertTrustManagerFactory(HttpTlsConfig tlsConfig)
      throws Exception {
    KeyStore store = this.loadTrustStore(tlsConfig);
    String algorithm = "SHA-256";
    byte[][] fingerprints = this.getFingerprints(algorithm, store);
    return new FingerprintTrustManagerFactory(algorithm, fingerprints);
  }

  private byte[][] getFingerprints(String algorithm, KeyStore store)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateEncodingException {
    List<String> aliases = Collections.list(store.aliases());
    List<byte[]> fingerprints = new ArrayList<>(aliases.size());
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    for (String alias : aliases) {
      X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
      messageDigest.reset();
      byte[] digest = messageDigest.digest(certificate.getEncoded());
      fingerprints.add(digest);
    }
    return fingerprints.toArray(new byte[0][]);
  }

  private TrustManagerFactory getInsecureTrustManagerFactory(HttpTlsConfig tlsConfig) {
    return InsecureTrustManagerFactory.INSTANCE;
  }

  @Nullable
  @Override
  public ExtendHostnameVerifier initHostnameVerifier(HttpTlsConfig tlsConfig) {
    String type = tlsConfig.getHostnameVerifierType();
    String actualType = !Strings.isNullOrEmpty(type) ? type : "STRICT";
    switch (actualType) {
      case "STRICT":
        // just use the jdk default
        return null;
      case "EXACT_SERVER_NAME":
        return new ExactServerNameHostnameVerifier(tlsConfig.getHostnameVerifierExactServerNames());
      case "NOOP":
        return NoopHostnameVerifier.INSTANCE;
      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

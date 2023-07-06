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

import com.ctrip.framework.apollo.util.http.tls.ClientX509ExtendedTrustManagerWrapper;
import com.ctrip.framework.apollo.util.http.tls.X509HostnameVerifier;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class HostnameVerifierTrustManagerWrapper extends ClientX509ExtendedTrustManagerWrapper {

  private final X509HostnameVerifier hostnameVerifier;

  public HostnameVerifierTrustManagerWrapper(X509TrustManager delegate,
      X509HostnameVerifier hostnameVerifier) {
    super(delegate);
    this.hostnameVerifier = Objects.requireNonNull(hostnameVerifier, "hostnameVerifier");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    super.checkServerTrusted(chain, authType, socket);
    SSLSocket sslSocket = (SSLSocket) socket;
    SSLSession handshakeSession = sslSocket.getHandshakeSession();
    this.verify(handshakeSession, chain);
  }

  private void verify(SSLSession handshakeSession, X509Certificate[] chain)
      throws CertificateException {
    Objects.requireNonNull(handshakeSession, "handshakeSession");
    boolean verify = this.hostnameVerifier.verify(handshakeSession.getPeerHost(), chain);
    if (!verify) {
      throw new CertificateException("hostname verification failed");
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType,
      SSLEngine sslEngine)
      throws CertificateException {
    super.checkServerTrusted(chain, authType, sslEngine);
    SSLSession handshakeSession = sslEngine.getHandshakeSession();
    this.verify(handshakeSession, chain);
  }
}

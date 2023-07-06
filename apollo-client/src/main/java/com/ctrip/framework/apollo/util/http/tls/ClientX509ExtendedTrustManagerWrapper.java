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

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ClientX509ExtendedTrustManagerWrapper extends X509ExtendedTrustManager {

  private final X509TrustManager delegate;

  @Nullable
  private final X509ExtendedTrustManager extendedDelegate;

  public ClientX509ExtendedTrustManagerWrapper(X509TrustManager delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.extendedDelegate = this.toExtendedDelegate(delegate);
  }

  private X509ExtendedTrustManager toExtendedDelegate(X509TrustManager delegate) {
    if (delegate instanceof X509ExtendedTrustManager) {
      return (X509ExtendedTrustManager) delegate;
    } else {
      return null;
    }
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    throw new UnsupportedOperationException("unsupported check client");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    if (this.extendedDelegate != null) {
      this.extendedDelegate.checkServerTrusted(chain, authType, socket);
    } else {
      this.delegate.checkServerTrusted(chain, authType);
    }
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType,
      SSLEngine sslEngine)
      throws CertificateException {
    throw new UnsupportedOperationException("unsupported check client");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType,
      SSLEngine sslEngine)
      throws CertificateException {
    if (this.extendedDelegate != null) {
      this.extendedDelegate.checkServerTrusted(chain, authType, sslEngine);
    } else {
      this.delegate.checkServerTrusted(chain, authType);
    }
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    throw new UnsupportedOperationException("unsupported check client");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    throw new UnsupportedOperationException("unsupported check without SSLSession");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return this.delegate.getAcceptedIssuers();
  }
}

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

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class NoopHostnameVerifier implements ExtendHostnameVerifier {

  public static final NoopHostnameVerifier INSTANCE = new NoopHostnameVerifier();
  private static final Logger log = LoggerFactory.getLogger(NoopHostnameVerifier.class);

  private NoopHostnameVerifier() {
  }

  @Override
  public boolean verify(String host, SSLSession session) {
    if (log.isWarnEnabled()) {
      log.warn("NoopHostnameVerifier is intended for development and testing purposes only, "
          + "it will accept any hostname");
    }
    if (log.isDebugEnabled()) {
      X509Certificate cert = this.tryGetCertificate(session);
      List<String> subjectAlternativeNames = this.tryGetSubjectAlternativeNames(cert);
      String subjectCommonName = this.tryGetSubjectCommonName(cert);
      log.debug("Accepting a hostname [host:{}][alternativeNames:{}][commonName:{}]", host,
          subjectAlternativeNames, subjectCommonName);
    }
    return true;
  }

  private X509Certificate tryGetCertificate(SSLSession session) {
    try {
      final Certificate[] chain = session.getPeerCertificates();
      return X509CertificateUtil.getServerCert(chain);
    } catch (Throwable ignore) {
      return null;
    }
  }

  private List<String> tryGetSubjectAlternativeNames(X509Certificate cert) {
    if (cert == null) {
      return null;
    }
    try {
      return X509CertificateUtil.getSubjectAlternativeNames(cert);
    } catch (Throwable e) {
      return null;
    }
  }

  private String tryGetSubjectCommonName(X509Certificate cert) {
    if (cert == null) {
      return null;
    }
    try {
      return X509CertificateUtil.getSubjectCommonName(cert);
    } catch (Throwable ignore) {
      return null;
    }
  }

  @Override
  public boolean verify(String host, X509Certificate[] chain) {
    if (log.isWarnEnabled()) {
      log.warn("NoopHostnameVerifier is intended for development and testing purposes only, "
          + "it will accept any hostname");
    }
    if (log.isDebugEnabled()) {
      X509Certificate cert = X509CertificateUtil.getServerCert(chain);
      List<String> subjectAlternativeNames = this.tryGetSubjectAlternativeNames(cert);
      String subjectCommonName = this.tryGetSubjectCommonName(cert);
      log.debug("Accepting a hostname [host:{}][alternativeNames:{}][commonName:{}]", host,
          subjectAlternativeNames, subjectCommonName);
    }
    return true;
  }

  @Override
  public String toString() {
    return "NoopHostnameVerifier";
  }
}

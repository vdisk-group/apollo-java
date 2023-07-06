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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ExactServerNameHostnameVerifier implements ExtendHostnameVerifier {

  private static final Logger log = LoggerFactory.getLogger(ExactServerNameHostnameVerifier.class);

  /**
   * expected exact server names
   */
  private final String[] expectedServerNames;

  public ExactServerNameHostnameVerifier(List<String> expectedServerNames) {
    Objects.requireNonNull(expectedServerNames, "expectedServerNames");
    if (expectedServerNames.isEmpty()) {
      throw new IllegalArgumentException("expectedServerNames must not be empty");
    }
    this.expectedServerNames = expectedServerNames.toArray(new String[0]);
  }

  @Override
  public boolean verify(String host, SSLSession session) {
    try {
      final Certificate[] certs = session.getPeerCertificates();
      Certificate cert = certs[0];
      if (!(cert instanceof X509Certificate)) {
        throw new SSLPeerUnverifiedException("unsupported cert type " + cert.getClass());
      }
      final X509Certificate x509 = (X509Certificate) cert;
      return this.verifyHost(x509);
    } catch (final SSLException e) {
      if (log.isDebugEnabled()) {
        log.debug("hostname verify error [err:{}]",
            e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), e);
      }
      return false;
    }
  }

  private boolean verifyHost(X509Certificate x509) throws SSLException {
    if (x509 == null) {
      throw new SSLException("cert is null");
    }
    List<String> subjectAltNames = X509CertificateUtil.getSubjectAlternativeNames(x509);
    boolean foundSubjectAltName = this.findMatchSubjectAltNames(subjectAltNames);
    if (foundSubjectAltName) {
      return true;
    }
    String commonName = X509CertificateUtil.getSubjectCommonName(x509);
    boolean foundCommonName = this.findMatchServerName(commonName, this.expectedServerNames);
    if (foundCommonName) {
      if (log.isDebugEnabled()) {
        log.debug("commonName matching was deprecated in RFC 2818, "
            + "Please use subjectAlternativeName extension instead. "
            + "see https://tools.ietf.org/html/rfc2818#section-3.1");
      }
      if (log.isDebugEnabled()) {
        log.debug(
            "hostname verify success deprecated [expected:{}][alternativeNames:{}][commonName:{}]",
            this.expectedServerNames, subjectAltNames, commonName);
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("hostname verify failed [expected:{}][alternativeNames:{}][commonName:{}]",
            this.expectedServerNames, subjectAltNames, commonName);
      }
    }
    return foundCommonName;
  }


  private boolean findMatchSubjectAltNames(List<String> subjectAltNames) {
    String[] expectedServerNames = this.expectedServerNames;
    for (String subjectAltName : subjectAltNames) {
      if (this.findMatchServerName(subjectAltName, expectedServerNames)) {
        return true;
      }
    }
    return false;
  }

  private boolean findMatchServerName(String serverName, String[] expectedServerNames) {
    for (String expectedServerName : expectedServerNames) {
      if (expectedServerName.equals(serverName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean verify(String host, X509Certificate[] chain) {
    try {
      final X509Certificate x509 = X509CertificateUtil.getServerCert(chain);
      return this.verifyHost(x509);
    } catch (final SSLException e) {
      if (log.isDebugEnabled()) {
        log.debug("hostname verify error [err:{}]",
            e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), e);
      }
      return false;
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        ExactServerNameHostnameVerifier.class.getSimpleName() + "[", "]")
        // fields
        .add("expectedServerNames=" + Arrays.toString(this.expectedServerNames))
        .toString();
  }
}

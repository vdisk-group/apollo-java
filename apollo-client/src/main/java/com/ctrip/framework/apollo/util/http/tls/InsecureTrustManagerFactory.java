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

import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class InsecureTrustManagerFactory extends AbstractTrustManagerFactory {

  private static final TrustManager TRUST_MANAGER = new ClientX509ExtendedTrustManagerWrapper(
      new InsecureTrustManager());

  public static final TrustManagerFactory INSTANCE = new InsecureTrustManagerFactory();

  private InsecureTrustManagerFactory() {
    super(new TrustManagerFactorySpiWrapper(TRUST_MANAGER), "insecure");
  }

  private static class InsecureTrustManager implements X509TrustManager {

    private static final Logger log = LoggerFactory.getLogger(InsecureTrustManager.class);

    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = new X509Certificate[0];

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      throw new UnsupportedOperationException("unsupported check client");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      if (log.isWarnEnabled()) {
        log.warn("InsecureTrustManager is intended for development and testing purposes only, "
            + "it will accept any certificate");
      }
      if (log.isDebugEnabled()) {
        X509Certificate cert = X509CertificateUtil.getServerCert(chain);
        List<String> subjectAlternativeNames = this.tryGetSubjectAlternativeNames(cert);
        String subjectCommonName = this.tryGetSubjectCommonName(cert);
        log.debug("Accepting a server certificate [alternativeNames:{}][commonName:{}]",
            subjectAlternativeNames, subjectCommonName);
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
    public X509Certificate[] getAcceptedIssuers() {
      return EMPTY_X509_CERTIFICATES;
    }
  }
}

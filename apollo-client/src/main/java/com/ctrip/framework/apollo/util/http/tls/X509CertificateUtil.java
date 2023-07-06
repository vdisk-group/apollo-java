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
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLException;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class X509CertificateUtil {

  private static final int DNS = 2;
  private static final int IP = 7;

  private X509CertificateUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Get server certificate from certificate chain
   */
  public static X509Certificate getServerCert(Certificate[] chain) {
    if (chain == null || chain.length == 0) {
      return null;
    }
    Certificate cert = chain[0];
    if (cert instanceof X509Certificate) {
      return (X509Certificate) cert;
    }
    return null;
  }

  /**
   * Get subjectAlternativeNames from X509Certificate
   *
   * @param cert X509Certificate
   * @return subjectAlternativeNames
   */
  public static List<String> getSubjectAlternativeNames(X509Certificate cert) {
    try {
      final Collection<List<?>> entries = cert.getSubjectAlternativeNames();
      if (entries == null || entries.isEmpty()) {
        return Collections.emptyList();
      }
      final List<String> result = new ArrayList<>();
      for (final List<?> entry : entries) {
        final Integer type;
        final Object value;
        if (entry.size() >= 2) {
          type = (Integer) entry.get(0);
          value = entry.get(1);
        } else {
          type = null;
          value = null;
        }
        if (type != null) {
          if (type == DNS || type == IP) {
            result.add((String) value);
          }
        }
      }
      return result;
    } catch (final CertificateParsingException ignore) {
      return Collections.emptyList();
    }
  }

  /**
   * Get subjectCommonName from X509Certificate
   *
   * @param x509 X509Certificate
   * @return subjectCommonName
   * @throws SSLException if subject DN is null or has no CN attribute
   */
  public static String getSubjectCommonName(X509Certificate x509) throws SSLException {
    String subjectDn = x509.getSubjectDN()
        .getName();
    if (subjectDn == null) {
      throw new SSLException("subject DN is null");
    }
    int commonNameIndex = subjectDn.toUpperCase()
        .indexOf("CN=");
    if (commonNameIndex == -1) {
      throw new SSLException("subject DN has no CN attribute, subject DN=" + subjectDn);
    }
    int splitIndex = subjectDn.indexOf(",", commonNameIndex);
    if (splitIndex != -1) {
      return subjectDn.substring(commonNameIndex + 3, splitIndex).trim();
    } else {
      return subjectDn.substring(commonNameIndex + 3).trim();
    }
  }
}

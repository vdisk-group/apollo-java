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

import com.google.common.base.Strings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class FingerprintTrustManagerFactory extends AbstractTrustManagerFactory {

  public FingerprintTrustManagerFactory(String algorithm, byte[][] fingerprints) {
    super(new TrustManagerFactorySpiWrapper(new ClientX509ExtendedTrustManagerWrapper(
        new FingerprintTrustManager(algorithm, fingerprints))), "fingerprint");
  }

  private static class FingerprintTrustManager implements X509TrustManager {

    private static final Logger log = LoggerFactory.getLogger(FingerprintTrustManager.class);

    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = new X509Certificate[0];

    private final String algorithm;

    private final byte[][] fingerprints;

    private final ThreadLocal<MessageDigest> messageDigestThreadLocal;

    public FingerprintTrustManager(String algorithm, byte[][] fingerprints) {
      this.validateAlgorithm(algorithm, fingerprints);
      this.algorithm = algorithm;
      this.fingerprints = fingerprints;
      this.messageDigestThreadLocal = ThreadLocal.withInitial(this::initMessageDigest);
    }

    private void validateAlgorithm(String algorithm, byte[][] fingerprints) {
      if (Strings.isNullOrEmpty(algorithm)) {
        throw new IllegalArgumentException("algorithm must not be null or empty");
      }
      Objects.requireNonNull(fingerprints, "fingerprints");
      if (fingerprints.length == 0) {
        throw new IllegalArgumentException("fingerprints must not be empty");
      }
      MessageDigest messageDigest = this.createMessageDigest(algorithm);
      int digestLength = messageDigest.getDigestLength();
      for (int i = 0, fingerprintsLength = fingerprints.length; i < fingerprintsLength; i++) {
        byte[] fingerprint = fingerprints[i];
        if (fingerprint == null) {
          throw new IllegalArgumentException(
              "fingerprints[" + i + "] must not be null");
        }
        int length = fingerprint.length;
        if (length != digestLength) {
          throw new IllegalArgumentException(
              "fingerprints[" + i + "] length is " + length + " but expected "
                  + digestLength);
        }
      }
    }

    private MessageDigest createMessageDigest(String algorithm) {
      try {
        return MessageDigest.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("Unsupported hash algorithm: " + algorithm, e);
      }
    }

    private MessageDigest initMessageDigest() {
      return this.createMessageDigest(this.algorithm);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      throw new UnsupportedOperationException("unsupported check client");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      this.checkTrusted(chain);
    }

    private void checkTrusted(X509Certificate[] chain) throws CertificateException {
      X509Certificate cert = chain[0];
      byte[] fingerprint = this.fingerprint(cert);
      boolean found = this.findMatch(fingerprint);
      if (!found) {
        throw new CertificateException(
            "server certificate with unknown fingerprint: " + cert.getSubjectDN());
      }
    }

    private boolean findMatch(byte[] fingerprint) {
      byte[][] fingerprints = this.fingerprints;
      for (byte[] allowedFingerprint : fingerprints) {
        if (Arrays.equals(fingerprint, allowedFingerprint)) {
          return true;
        }
      }
      return false;
    }

    private byte[] fingerprint(X509Certificate certificate) throws CertificateEncodingException {
      MessageDigest messageDigest = this.messageDigestThreadLocal.get();
      messageDigest.reset();
      return messageDigest.digest(certificate.getEncoded());
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return EMPTY_X509_CERTIFICATES;
    }
  }
}

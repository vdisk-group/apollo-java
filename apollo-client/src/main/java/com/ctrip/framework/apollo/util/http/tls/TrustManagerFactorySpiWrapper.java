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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collection;
import java.util.Objects;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class TrustManagerFactorySpiWrapper extends TrustManagerFactorySpi {

  private static final TrustManager[] EMPTY_TRUST_MANAGER_ARRAY = new TrustManager[0];

  private final TrustManager[] trustManagers;

  public TrustManagerFactorySpiWrapper(TrustManager... trustManagers) {
    this.validateTrustManagers(trustManagers);
    this.trustManagers = trustManagers;
  }

  public TrustManagerFactorySpiWrapper(Collection<TrustManager> trustManagers) {
    this(trustManagers.toArray(EMPTY_TRUST_MANAGER_ARRAY));
  }

  private void validateTrustManagers(TrustManager[] trustManagers) {
    Objects.requireNonNull(trustManagers, "trustManagers");
    if (trustManagers.length == 0) {
      throw new IllegalArgumentException("trustManagers must not be empty");
    }
    for (int i = 0, trustManagersLength = trustManagers.length; i < trustManagersLength; i++) {
      TrustManager trustManager = trustManagers[i];
      if (trustManager == null) {
        throw new NullPointerException("trustManagers[" + i + "]");
      }
    }
  }

  @Override
  protected void engineInit(KeyStore keyStore) throws KeyStoreException {

  }

  @Override
  protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
      throws InvalidAlgorithmParameterException {

  }

  @Override
  protected TrustManager[] engineGetTrustManagers() {
    return this.trustManagers.clone();
  }
}

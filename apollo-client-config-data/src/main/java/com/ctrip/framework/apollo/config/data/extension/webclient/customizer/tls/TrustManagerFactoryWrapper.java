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

import com.ctrip.framework.apollo.util.http.tls.AbstractTrustManagerFactory;
import com.ctrip.framework.apollo.util.http.tls.TrustManagerFactorySpiWrapper;
import java.util.Collection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class TrustManagerFactoryWrapper extends AbstractTrustManagerFactory {

  public TrustManagerFactoryWrapper(TrustManagerFactorySpi factorySpi) {
    super(factorySpi, "wrapper");
  }

  public TrustManagerFactoryWrapper(TrustManager... trustManagers) {
    this(new TrustManagerFactorySpiWrapper(trustManagers));
  }

  public TrustManagerFactoryWrapper(Collection<TrustManager> trustManagers) {
    this(new TrustManagerFactorySpiWrapper(trustManagers));
  }
}

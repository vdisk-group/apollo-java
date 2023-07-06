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

import java.security.Provider;
import java.util.Objects;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public abstract class AbstractTrustManagerFactory extends TrustManagerFactory {

  private static final Provider NOOP_PROVIDER = new NoopProvider();

  protected AbstractTrustManagerFactory(TrustManagerFactorySpi factorySpi,
      String name) {
    super(Objects.requireNonNull(factorySpi, "factorySpi"), NOOP_PROVIDER,
        Objects.requireNonNull(name, "name"));
  }

  private static class NoopProvider extends Provider {

    private static final long serialVersionUID = 4731781698894238477L;

    private NoopProvider() {
      super("noop", 0.0, "");
    }
  }
}

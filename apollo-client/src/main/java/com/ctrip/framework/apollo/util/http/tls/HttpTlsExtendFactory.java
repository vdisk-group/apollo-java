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

import com.ctrip.framework.apollo.core.spi.Ordered;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface HttpTlsExtendFactory extends Ordered {

  /**
   * Whether this factory is available for use, taking the current environment into consideration.
   * If {@code false}, {@link #initKeyManagerFactory}, {@link #initTrustManagerFactory} and
   * {@link #initHostnameVerifier} is not safe to be called.
   */
  boolean isExtendAvailable();

  /**
   * init KeyManagerFactory
   *
   * @param tlsConfig config
   * @return KeyManagerFactory
   * @throws Exception init failed
   */
  @Nullable
  KeyManagerFactory initKeyManagerFactory(HttpTlsConfig tlsConfig) throws Exception;

  /**
   * init TrustManagerFactory
   *
   * @param tlsConfig config
   * @return TrustManagerFactory
   * @throws Exception init failed
   */
  @Nullable
  TrustManagerFactory initTrustManagerFactory(HttpTlsConfig tlsConfig) throws Exception;

  /**
   * init HostnameVerifier
   *
   * @param tlsConfig config
   * @return HostnameVerifier
   */
  @Nullable
  ExtendHostnameVerifier initHostnameVerifier(HttpTlsConfig tlsConfig);
}

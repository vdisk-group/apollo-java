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
package com.ctrip.framework.apollo.config.data.extension.webclient.customizer.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsConfig;
import javax.annotation.Nullable;
import reactor.netty.tcp.SslProvider;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface ReactorClientHttpTlsFactory extends Ordered {

  /**
   * Whether this factory is available for use, taking the current environment into consideration.
   * If {@code false}, {@link #create} is not safe to be called.
   */
  boolean isAvailable();

  /**
   * Creates a new {@link SslProvider} using the given {@link HttpTlsConfig}.
   */
  @Nullable
  SslProvider create(HttpTlsConfig tlsConfig);
}

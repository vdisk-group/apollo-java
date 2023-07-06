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

import com.ctrip.framework.apollo.config.data.extension.webclient.WebClientCustomizerArgs;
import com.ctrip.framework.apollo.core.spi.Ordered;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.lang.Nullable;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface ApolloClientWebClientCustomizerFactoryV2 extends Ordered {

  /**
   * create a WebClientCustomizer instance
   *
   * @param args arguments
   * @return WebClientCustomizer instance or null
   */
  @Nullable
  WebClientCustomizer createWebClientCustomizer(WebClientCustomizerArgs args);
}

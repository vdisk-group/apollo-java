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

import com.ctrip.framework.apollo.config.data.extension.properties.ApolloClientExtensionProperties;
import com.ctrip.framework.apollo.config.data.extension.webclient.WebClientCustomizerArgs;
import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.spi.ApolloClientWebClientCustomizerFactoryV2;
import com.ctrip.framework.apollo.core.spi.Ordered;
import org.apache.commons.logging.Log;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientWebClientTlsCustomizerFactory implements
    ApolloClientWebClientCustomizerFactoryV2 {

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 100;

  @Override
  public WebClientCustomizer createWebClientCustomizer(WebClientCustomizerArgs args) {
    Log log = args.getLogFactory().getLog(ApolloClientWebClientTlsCustomizerFactory.class);
    if (!ApolloClientWebClientTlsCustomizer.isAvailable()) {
      // not available
      if (log.isInfoEnabled()) {
        log.info("apollo client webclient tls extension not available");
      }
      return null;
    }
    ApolloClientExtensionProperties extension = args.getApolloClientProperties().getExtension();
    if (!extension.getTls().isEnabled()) {
      // disabled
      if (log.isInfoEnabled()) {
        log.info("apollo client webclient tls extension disabled");
      }
      return null;
    }
    return new ApolloClientWebClientTlsCustomizer();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

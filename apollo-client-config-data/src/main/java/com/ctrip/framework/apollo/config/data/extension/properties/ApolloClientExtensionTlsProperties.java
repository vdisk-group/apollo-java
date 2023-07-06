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
package com.ctrip.framework.apollo.config.data.extension.properties;

import com.ctrip.framework.apollo.config.data.extension.webclient.customizer.tls.ApolloClientWebClientTlsCustomizerFactory;
import com.ctrip.framework.apollo.util.http.tls.HttpTlsConfig;
import java.util.StringJoiner;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientExtensionTlsProperties {

  /**
   * webclient tls extension enabled
   * <p>
   * only for the extension {@link ApolloClientWebClientTlsCustomizerFactory} enabled, the actual
   * tls enabled is controlled by the {@link HttpTlsConfig#isEnabled()}. it's useful when you want
   * to use your own tls extension and disable the default.
   * </p>
   */
  private boolean enabled = true;

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        ApolloClientExtensionTlsProperties.class.getSimpleName() + "[", "]")
        // fields
        .add("enabled=" + this.enabled)
        .toString();
  }
}

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

import com.ctrip.framework.apollo.config.data.extension.enums.ApolloClientMessagingType;
import java.util.StringJoiner;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientExtensionProperties {

  /**
   * enable apollo client extension(webclient/websocket and authentication)
   */
  private Boolean enabled = false;

  /**
   * apollo client listening type
   */
  private ApolloClientMessagingType messagingType = ApolloClientMessagingType.LONG_POLLING;

  /**
   * enable apollo client webclient tls extension
   */
  @NestedConfigurationProperty
  private final ApolloClientExtensionTlsProperties tls = new ApolloClientExtensionTlsProperties();

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public ApolloClientMessagingType getMessagingType() {
    return messagingType;
  }

  public void setMessagingType(
      ApolloClientMessagingType messagingType) {
    this.messagingType = messagingType;
  }

  public ApolloClientExtensionTlsProperties getTls() {
    return this.tls;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        ApolloClientExtensionProperties.class.getSimpleName() + "[", "]")
        // fields
        .add("enabled=" + this.enabled)
        .add("messagingType=" + this.messagingType)
        .add("tls=" + this.tls)
        .toString();
  }
}

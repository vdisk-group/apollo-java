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
package com.ctrip.framework.apollo.client.v1.http.meta;

import java.util.Objects;
import javax.annotation.Nonnull;

public class HttpMetaClientProperties {

  @Nonnull
  private final Integer discoveryConnectTimeout;

  @Nonnull
  private final Integer discoveryReadTimeout;

  HttpMetaClientProperties(Builder builder) {
    HttpMetaClientProperties.validateBuilder(builder);
    this.discoveryConnectTimeout = builder.discoveryConnectTimeout;
    this.discoveryReadTimeout = builder.discoveryReadTimeout;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.discoveryConnectTimeout, "discoveryConnectTimeout");
    Objects.requireNonNull(builder.discoveryReadTimeout, "discoveryReadTimeout");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.discoveryConnectTimeout = this.discoveryConnectTimeout;
    builder.discoveryReadTimeout = this.discoveryReadTimeout;
    return builder;
  }

  @Nonnull
  public Integer getDiscoveryConnectTimeout() {
    return this.discoveryConnectTimeout;
  }

  @Nonnull
  public Integer getDiscoveryReadTimeout() {
    return this.discoveryReadTimeout;
  }

  public static final class Builder {

    private Integer discoveryConnectTimeout;
    private Integer discoveryReadTimeout;

    Builder() {
    }

    public Builder discoveryConnectTimeout(Integer discoveryConnectTimeout) {
      this.discoveryConnectTimeout = discoveryConnectTimeout;
      return this;
    }

    public Builder discoveryReadTimeout(Integer discoveryReadTimeout) {
      this.discoveryReadTimeout = discoveryReadTimeout;
      return this;
    }

    public HttpMetaClientProperties build() {
      return new HttpMetaClientProperties(this);
    }
  }
}

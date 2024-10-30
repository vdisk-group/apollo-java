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
package com.ctrip.framework.apollo.core.http;

import java.util.Objects;

public class HttpTransportProperties {

  private final Integer defaultConnectTimeout;

  private final Integer defaultReadTimeout;

  HttpTransportProperties(Builder builder) {
    HttpTransportProperties.validateBuilder(builder);
    this.defaultConnectTimeout = builder.defaultConnectTimeout;
    this.defaultReadTimeout = builder.defaultReadTimeout;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.defaultConnectTimeout, "defaultConnectTimeout");
    Objects.requireNonNull(builder.defaultReadTimeout, "defaultReadTimeout");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.defaultConnectTimeout = this.defaultConnectTimeout;
    builder.defaultReadTimeout = this.defaultReadTimeout;
    return builder;
  }

  public Integer getDefaultConnectTimeout() {
    return this.defaultConnectTimeout;
  }

  public Integer getDefaultReadTimeout() {
    return this.defaultReadTimeout;
  }

  public static final class Builder {

    private Integer defaultConnectTimeout;
    private Integer defaultReadTimeout;

    Builder() {
    }

    public Builder defaultConnectTimeout(Integer defaultConnectTimeout) {
      this.defaultConnectTimeout = defaultConnectTimeout;
      return this;
    }

    public Builder defaultReadTimeout(Integer defaultReadTimeout) {
      this.defaultReadTimeout = defaultReadTimeout;
      return this;
    }

    public HttpTransportProperties build() {
      return new HttpTransportProperties(this);
    }
  }
}

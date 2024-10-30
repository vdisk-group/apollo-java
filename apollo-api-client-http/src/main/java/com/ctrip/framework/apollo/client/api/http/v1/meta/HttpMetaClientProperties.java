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
package com.ctrip.framework.apollo.client.api.http.v1.meta;

import java.util.Objects;
import javax.annotation.Nonnull;

public class HttpMetaClientProperties {

  @Nonnull
  private final Integer getServicesConnectTimeout;

  @Nonnull
  private final Integer getServicesReadTimeout;

  HttpMetaClientProperties(Builder builder) {
    HttpMetaClientProperties.validateBuilder(builder);
    this.getServicesConnectTimeout = builder.getServicesConnectTimeout;
    this.getServicesReadTimeout = builder.getServicesReadTimeout;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.getServicesConnectTimeout, "getServicesConnectTimeout");
    Objects.requireNonNull(builder.getServicesReadTimeout, "getServicesReadTimeout");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.getServicesConnectTimeout = this.getServicesConnectTimeout;
    builder.getServicesReadTimeout = this.getServicesReadTimeout;
    return builder;
  }

  @Nonnull
  public Integer getGetServicesConnectTimeout() {
    return this.getServicesConnectTimeout;
  }

  @Nonnull
  public Integer getGetServicesReadTimeout() {
    return this.getServicesReadTimeout;
  }

  public static final class Builder {

    private Integer getServicesConnectTimeout;
    private Integer getServicesReadTimeout;

    Builder() {
    }

    public Builder getServicesConnectTimeout(Integer getServicesConnectTimeout) {
      this.getServicesConnectTimeout = getServicesConnectTimeout;
      return this;
    }

    public Builder getServicesReadTimeout(Integer getServicesReadTimeout) {
      this.getServicesReadTimeout = getServicesReadTimeout;
      return this;
    }

    public HttpMetaClientProperties build() {
      return new HttpMetaClientProperties(this);
    }
  }
}

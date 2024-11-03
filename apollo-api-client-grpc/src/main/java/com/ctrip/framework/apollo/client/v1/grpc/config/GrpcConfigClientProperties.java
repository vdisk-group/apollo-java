
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
package com.ctrip.framework.apollo.client.v1.grpc.config;

import java.util.Objects;
import javax.annotation.Nonnull;

public class GrpcConfigClientProperties {

  @Nonnull
  private final Integer watchNotificationConnectTimeout;

  @Nonnull
  private final Integer watchNotificationReadTimeout;

  @Nonnull
  private final Integer getConfigConnectTimeout;

  @Nonnull
  private final Integer getConfigReadTimeout;

  GrpcConfigClientProperties(Builder builder) {
    GrpcConfigClientProperties.validateBuilder(builder);
    this.watchNotificationConnectTimeout = builder.watchNotificationConnectTimeout;
    this.watchNotificationReadTimeout = builder.watchNotificationReadTimeout;
    this.getConfigConnectTimeout = builder.getConfigConnectTimeout;
    this.getConfigReadTimeout = builder.getConfigReadTimeout;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.watchNotificationConnectTimeout,
        "watchNotificationConnectTimeout");
    Objects.requireNonNull(builder.watchNotificationReadTimeout, "watchNotificationReadTimeout");
    Objects.requireNonNull(builder.getConfigConnectTimeout, "getConfigConnectTimeout");
    Objects.requireNonNull(builder.getConfigReadTimeout, "getConfigReadTimeout");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.watchNotificationConnectTimeout = this.watchNotificationConnectTimeout;
    builder.watchNotificationReadTimeout = this.watchNotificationReadTimeout;
    builder.getConfigConnectTimeout = this.getConfigConnectTimeout;
    builder.getConfigReadTimeout = this.getConfigReadTimeout;
    return builder;
  }

  @Nonnull
  public Integer getWatchNotificationConnectTimeout() {
    return this.watchNotificationConnectTimeout;
  }

  @Nonnull
  public Integer getWatchNotificationReadTimeout() {
    return this.watchNotificationReadTimeout;
  }

  @Nonnull
  public Integer getGetConfigConnectTimeout() {
    return this.getConfigConnectTimeout;
  }

  @Nonnull
  public Integer getGetConfigReadTimeout() {
    return this.getConfigReadTimeout;
  }

  public static final class Builder {

    private Integer watchNotificationConnectTimeout;
    private Integer watchNotificationReadTimeout;
    private Integer getConfigConnectTimeout;
    private Integer getConfigReadTimeout;

    Builder() {
    }

    public Builder watchNotificationConnectTimeout(Integer watchNotificationConnectTimeout) {
      this.watchNotificationConnectTimeout = watchNotificationConnectTimeout;
      return this;
    }

    public Builder watchNotificationReadTimeout(Integer watchNotificationReadTimeout) {
      this.watchNotificationReadTimeout = watchNotificationReadTimeout;
      return this;
    }

    public Builder getConfigConnectTimeout(Integer getConfigConnectTimeout) {
      this.getConfigConnectTimeout = getConfigConnectTimeout;
      return this;
    }

    public Builder getConfigReadTimeout(Integer getConfigReadTimeout) {
      this.getConfigReadTimeout = getConfigReadTimeout;
      return this;
    }

    public GrpcConfigClientProperties build() {
      return new GrpcConfigClientProperties(this);
    }
  }
}

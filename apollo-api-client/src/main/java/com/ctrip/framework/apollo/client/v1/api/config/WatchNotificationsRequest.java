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
package com.ctrip.framework.apollo.client.v1.api.config;

import java.util.Objects;

import com.ctrip.framework.apollo.client.v1.api.Endpoint;

public class WatchNotificationsRequest {

  /**
   * config server
   */
  private final Endpoint endpoint;

  /**
   * interested notifications
   */
  private final WatchNotificationsOptions options;

  WatchNotificationsRequest(Builder builder) {
    WatchNotificationsRequest.validateBuilder(builder);
    this.endpoint = builder.endpoint;
    this.options = builder.options;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.endpoint, "endpoint");
    Objects.requireNonNull(builder.options, "options");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.endpoint = this.endpoint;
    builder.options = this.options;
    return builder;
  }

  public Endpoint getEndpoint() {
    return this.endpoint;
  }

  public WatchNotificationsOptions getOptions() {
    return this.options;
  }

  public static final class Builder {

    private Endpoint endpoint;
    private WatchNotificationsOptions options;

    Builder() {
    }

    public Builder endpoint(Endpoint endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder options(WatchNotificationsOptions options) {
      this.options = options;
      return this;
    }

    public WatchNotificationsRequest build() {
      return new WatchNotificationsRequest(this);
    }
  }
}

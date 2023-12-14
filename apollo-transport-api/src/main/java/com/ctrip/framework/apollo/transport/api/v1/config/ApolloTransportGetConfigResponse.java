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
package com.ctrip.framework.apollo.transport.api.v1.config;

import java.util.StringJoiner;

public class ApolloTransportGetConfigResponse {

  private final ApolloTransportGetConfigStatus status;

  private final ApolloTransportGetConfigResult config;

  ApolloTransportGetConfigResponse(Builder builder) {
    this.status = builder.status;
    this.config = builder.config;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.status = this.status;
    builder.config = this.config;
    return builder;
  }

  public ApolloTransportGetConfigStatus getStatus() {
    return this.status;
  }

  public ApolloTransportGetConfigResult getConfig() {
    return this.config;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ApolloTransportGetConfigResponse.class.getSimpleName() + "[", "]")
        // fields
        .add("status=" + this.status)
        .add("config=" + this.config)
        .toString();
  }

  public static final class Builder {

    private ApolloTransportGetConfigStatus status;
    private ApolloTransportGetConfigResult config;

    Builder() {
    }

    public Builder status(ApolloTransportGetConfigStatus status) {
      this.status = status;
      return this;
    }

    public Builder config(ApolloTransportGetConfigResult config) {
      this.config = config;
      return this;
    }

    public ApolloTransportGetConfigResponse build() {
      return new ApolloTransportGetConfigResponse(this);
    }
  }
}

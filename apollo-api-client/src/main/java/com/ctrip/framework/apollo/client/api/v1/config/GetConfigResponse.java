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
package com.ctrip.framework.apollo.client.api.v1.config;

import java.util.StringJoiner;

public class GetConfigResponse {

  private final GetConfigStatus status;

  private final GetConfigResult config;

  GetConfigResponse(Builder builder) {
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

  public GetConfigStatus getStatus() {
    return this.status;
  }

  public GetConfigResult getConfig() {
    return this.config;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetConfigResponse.class.getSimpleName() + "[", "]")
        // fields
        .add("status=" + this.status)
        .add("config=" + this.config)
        .toString();
  }

  public static final class Builder {

    private GetConfigStatus status;
    private GetConfigResult config;

    Builder() {
    }

    public Builder status(GetConfigStatus status) {
      this.status = status;
      return this;
    }

    public Builder config(GetConfigResult config) {
      this.config = config;
      return this;
    }

    public GetConfigResponse build() {
      return new GetConfigResponse(this);
    }
  }
}

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
package com.ctrip.framework.apollo.client.api.v1;

public class Endpoint {

  /**
   * server address
   */
  private final String address;

  Endpoint(Builder builder) {
    this.address = builder.address;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.address = this.address;
    return builder;
  }

  public String getAddress() {
    return this.address;
  }

  public static final class Builder {

    private String address;

    Builder() {
    }

    public Builder address(String address) {
      this.address = address;
      return this;
    }

    public Endpoint build() {
      return new Endpoint(this);
    }
  }
}

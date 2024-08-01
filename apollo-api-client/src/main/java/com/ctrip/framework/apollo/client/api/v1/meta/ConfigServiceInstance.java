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
package com.ctrip.framework.apollo.client.api.v1.meta;

import java.util.StringJoiner;

public class ConfigServiceInstance {

  private final String serviceId;

  private final String instanceId;

  private final String address;

  ConfigServiceInstance(Builder builder) {
    this.serviceId = builder.serviceId;
    this.instanceId = builder.instanceId;
    this.address = builder.address;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.serviceId = this.serviceId;
    builder.instanceId = this.instanceId;
    builder.address = this.address;
    return builder;
  }

  public String getServiceId() {
    return this.serviceId;
  }

  public String getInstanceId() {
    return this.instanceId;
  }

  public String getAddress() {
    return this.address;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ConfigServiceInstance.class.getSimpleName() + "[", "]")
        // fields
        .add("serviceId='" + this.serviceId + "'")
        .add("instanceId='" + this.instanceId + "'")
        .add("address='" + this.address + "'")
        .toString();
  }

  public static final class Builder {

    private String serviceId;
    private String instanceId;
    private String address;

    Builder() {
    }

    public Builder serviceId(String serviceId) {
      this.serviceId = serviceId;
      return this;
    }

    public Builder instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
    }

    public Builder address(String address) {
      this.address = address;
      return this;
    }

    public ConfigServiceInstance build() {
      return new ConfigServiceInstance(this);
    }
  }
}

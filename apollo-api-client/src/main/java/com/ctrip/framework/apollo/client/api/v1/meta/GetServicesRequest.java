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

public class GetServicesRequest {

  private final String appId;

  private final String clientIp;

  GetServicesRequest(Builder builder) {
    this.appId = builder.appId;
    this.clientIp = builder.clientIp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.appId = this.appId;
    builder.clientIp = this.clientIp;
    return builder;
  }

  public String getAppId() {
    return this.appId;
  }

  public String getClientIp() {
    return this.clientIp;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetServicesRequest.class.getSimpleName() + "[", "]")
        // fields
        .add("appId='" + this.appId + "'")
        .add("clientIp='" + this.clientIp + "'")
        .toString();
  }

  public static final class Builder {

    private String appId;
    private String clientIp;

    Builder() {
    }

    public Builder appId(String appId) {
      this.appId = appId;
      return this;
    }

    public Builder clientIp(String clientIp) {
      this.clientIp = clientIp;
      return this;
    }

    public GetServicesRequest build() {
      return new GetServicesRequest(this);
    }
  }
}

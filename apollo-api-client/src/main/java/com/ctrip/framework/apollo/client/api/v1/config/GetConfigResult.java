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

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

public class GetConfigResult {

  private final String appId;

  private final String cluster;

  private final String namespaceName;

  private final String releaseKey;

  private final Map<String, String> configurations;

  GetConfigResult(Builder builder) {
    this.appId = builder.appId;
    this.cluster = builder.cluster;
    this.namespaceName = builder.namespaceName;
    this.releaseKey = builder.releaseKey;
    this.configurations = builder.configurations;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.appId = this.appId;
    builder.cluster = this.cluster;
    builder.namespaceName = this.namespaceName;
    builder.releaseKey = this.releaseKey;
    builder.configurations = this.configurations;
    return builder;
  }

  public String getAppId() {
    return this.appId;
  }

  public String getCluster() {
    return this.cluster;
  }

  public String getNamespaceName() {
    return this.namespaceName;
  }

  public String getReleaseKey() {
    return this.releaseKey;
  }

  public Map<String, String> getConfigurations() {
    return this.configurations;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetConfigResult.class.getSimpleName() + "[", "]")
        // fields
        .add("appId='" + this.appId + "'")
        .add("cluster='" + this.cluster + "'")
        .add("namespaceName='" + this.namespaceName + "'")
        .add("releaseKey='" + this.releaseKey + "'")
        .add("configurations=" + this.configurations)
        .toString();
  }

  public static final class Builder {

    private String appId;
    private String cluster;
    private String namespaceName;
    private String releaseKey;
    private Map<String, String> configurations;

    Builder() {
    }

    public Builder appId(String appId) {
      this.appId = appId;
      return this;
    }

    public Builder cluster(String cluster) {
      this.cluster = cluster;
      return this;
    }

    public Builder namespaceName(String namespaceName) {
      this.namespaceName = namespaceName;
      return this;
    }

    public Builder releaseKey(String releaseKey) {
      this.releaseKey = releaseKey;
      return this;
    }

    public Builder configurations(Map<String, String> configurations) {
      this.configurations = configurations == null ? null :
          // nonnull
          (configurations.isEmpty() ? Collections.emptyMap()
              : Collections.unmodifiableMap(configurations));
      return this;
    }

    public GetConfigResult build() {
      return new GetConfigResult(this);
    }
  }
}

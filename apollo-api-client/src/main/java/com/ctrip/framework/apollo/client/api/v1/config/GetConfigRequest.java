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

public class GetConfigRequest {

  private final String appId;

  private final String cluster;

  private final String namespace;

  private final String releaseKey;

  private final String dataCenter;

  private final String clientIp;

  private final String label;

  private final NotificationMessages messages;

  private final String accessKeySecret;

  GetConfigRequest(Builder builder) {
    this.appId = builder.appId;
    this.cluster = builder.cluster;
    this.namespace = builder.namespace;
    this.releaseKey = builder.releaseKey;
    this.dataCenter = builder.dataCenter;
    this.clientIp = builder.clientIp;
    this.label = builder.label;
    this.messages = builder.messages;
    this.accessKeySecret = builder.accessKeySecret;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.appId = this.appId;
    builder.cluster = this.cluster;
    builder.namespace = this.namespace;
    builder.releaseKey = this.releaseKey;
    builder.dataCenter = this.dataCenter;
    builder.clientIp = this.clientIp;
    builder.label = this.label;
    builder.messages = this.messages;
    builder.accessKeySecret = this.accessKeySecret;
    return builder;
  }

  public String getAppId() {
    return this.appId;
  }

  public String getCluster() {
    return this.cluster;
  }

  public String getNamespace() {
    return this.namespace;
  }

  public String getReleaseKey() {
    return this.releaseKey;
  }

  public String getDataCenter() {
    return this.dataCenter;
  }

  public String getClientIp() {
    return this.clientIp;
  }

  public String getLabel() {
    return this.label;
  }

  public NotificationMessages getMessages() {
    return this.messages;
  }

  public String getAccessKeySecret() {
    return this.accessKeySecret;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        GetConfigRequest.class.getSimpleName() + "[", "]")
        // fields
        .add("appId='" + this.appId + "'")
        .add("cluster='" + this.cluster + "'")
        .add("namespace='" + this.namespace + "'")
        .add("releaseKey='" + this.releaseKey + "'")
        .add("dataCenter='" + this.dataCenter + "'")
        .add("clientIp='" + this.clientIp + "'")
        .add("label='" + this.label + "'")
        .add("messages=" + this.messages)
        .add("accessKeySecret='" + this.accessKeySecret + "'")
        .toString();
  }

  public static final class Builder {

    private String appId;
    private String cluster;
    private String namespace;
    private String releaseKey;
    private String dataCenter;
    private String clientIp;
    private String label;
    private NotificationMessages messages;
    private String accessKeySecret;

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

    public Builder namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder releaseKey(String releaseKey) {
      this.releaseKey = releaseKey;
      return this;
    }

    public Builder dataCenter(String dataCenter) {
      this.dataCenter = dataCenter;
      return this;
    }

    public Builder clientIp(String clientIp) {
      this.clientIp = clientIp;
      return this;
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public Builder messages(NotificationMessages messages) {
      this.messages = messages;
      return this;
    }

    public Builder accessKeySecret(String accessKeySecret) {
      this.accessKeySecret = accessKeySecret;
      return this;
    }

    public GetConfigRequest build() {
      return new GetConfigRequest(this);
    }
  }
}

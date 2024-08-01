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
import java.util.List;
import java.util.StringJoiner;

public class WatchNotificationsRequest {

  private final String appId;

  private final String cluster;

  private final List<NotificationDefinition> notifications;

  private final String dataCenter;

  private final String clientIp;

  private final String label;

  private final String accessKeySecret;

  WatchNotificationsRequest(Builder builder) {
    this.appId = builder.appId;
    this.cluster = builder.cluster;
    this.notifications = builder.notifications;
    this.dataCenter = builder.dataCenter;
    this.clientIp = builder.clientIp;
    this.label = builder.label;
    this.accessKeySecret = builder.accessKeySecret;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.appId = this.appId;
    builder.cluster = this.cluster;
    builder.notifications = this.notifications;
    builder.dataCenter = this.dataCenter;
    builder.clientIp = this.clientIp;
    builder.label = this.label;
    builder.accessKeySecret = this.accessKeySecret;
    return builder;
  }

  public String getAppId() {
    return this.appId;
  }

  public String getCluster() {
    return this.cluster;
  }

  public List<NotificationDefinition> getNotifications() {
    return this.notifications;
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

  public String getAccessKeySecret() {
    return this.accessKeySecret;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        WatchNotificationsRequest.class.getSimpleName() + "[", "]")
        // fields
        .add("appId='" + this.appId + "'")
        .add("cluster='" + this.cluster + "'")
        .add("notifications=" + this.notifications)
        .add("dataCenter='" + this.dataCenter + "'")
        .add("clientIp='" + this.clientIp + "'")
        .add("label='" + this.label + "'")
        .add("accessKeySecret='" + this.accessKeySecret + "'")
        .toString();
  }

  public static final class Builder {

    private String appId;
    private String cluster;
    private List<NotificationDefinition> notifications;
    private String dataCenter;
    private String clientIp;
    private String label;
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

    public Builder notifications(List<NotificationDefinition> notifications) {
      this.notifications = notifications == null ? null :
          // nonnull
          (notifications.isEmpty() ? Collections.emptyList()
              : Collections.unmodifiableList(notifications));
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

    public Builder accessKeySecret(String accessKeySecret) {
      this.accessKeySecret = accessKeySecret;
      return this;
    }

    public WatchNotificationsRequest build() {
      return new WatchNotificationsRequest(this);
    }
  }
}

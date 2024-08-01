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

public class NotificationDefinition {

  private final String namespaceName;

  private final Long notificationId;

  NotificationDefinition(Builder builder) {
    this.namespaceName = builder.namespaceName;
    this.notificationId = builder.notificationId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.namespaceName = this.namespaceName;
    builder.notificationId = this.notificationId;
    return builder;
  }

  public String getNamespaceName() {
    return this.namespaceName;
  }

  public Long getNotificationId() {
    return this.notificationId;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", NotificationDefinition.class.getSimpleName() + "[",
        "]")
        // fields
        .add("namespaceName='" + this.namespaceName + "'")
        .add("notificationId=" + this.notificationId)
        .toString();
  }

  public static final class Builder {

    private String namespaceName;
    private Long notificationId;

    Builder() {
    }

    public Builder namespaceName(String namespaceName) {
      this.namespaceName = namespaceName;
      return this;
    }

    public Builder notificationId(Long notificationId) {
      this.notificationId = notificationId;
      return this;
    }

    public NotificationDefinition build() {
      return new NotificationDefinition(this);
    }
  }
}

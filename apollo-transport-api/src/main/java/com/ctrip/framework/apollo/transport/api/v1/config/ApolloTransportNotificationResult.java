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

public class ApolloTransportNotificationResult {

  private final String namespaceName;

  private final Long notificationId;

  private final ApolloTransportNotificationMessages messages;

  ApolloTransportNotificationResult(Builder builder) {
    this.namespaceName = builder.namespaceName;
    this.notificationId = builder.notificationId;
    this.messages = builder.messages;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.namespaceName = this.namespaceName;
    builder.notificationId = this.notificationId;
    builder.messages = this.messages;
    return builder;
  }

  public String getNamespaceName() {
    return this.namespaceName;
  }

  public Long getNotificationId() {
    return this.notificationId;
  }

  public ApolloTransportNotificationMessages getMessages() {
    return this.messages;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ApolloTransportNotificationResult.class.getSimpleName() + "[",
        "]")
        // fields
        .add("namespaceName='" + this.namespaceName + "'")
        .add("notificationId=" + this.notificationId)
        .add("messages=" + this.messages)
        .toString();
  }

  public static final class Builder {

    private String namespaceName;
    private Long notificationId;
    private ApolloTransportNotificationMessages messages;

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

    public Builder messages(ApolloTransportNotificationMessages messages) {
      this.messages = messages;
      return this;
    }

    public ApolloTransportNotificationResult build() {
      return new ApolloTransportNotificationResult(this);
    }
  }
}

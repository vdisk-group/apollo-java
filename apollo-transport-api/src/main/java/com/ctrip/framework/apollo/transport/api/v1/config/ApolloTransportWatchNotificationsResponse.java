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

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class ApolloTransportWatchNotificationsResponse {

  private final ApolloTransportWatchNotificationsStatus status;

  private final List<ApolloTransportNotificationResult> notifications;

  ApolloTransportWatchNotificationsResponse(Builder builder) {
    this.status = builder.status;
    this.notifications = builder.notifications;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.status = this.status;
    builder.notifications = this.notifications;
    return builder;
  }

  public ApolloTransportWatchNotificationsStatus getStatus() {
    return this.status;
  }

  public List<ApolloTransportNotificationResult> getNotifications() {
    return this.notifications;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        ApolloTransportWatchNotificationsResponse.class.getSimpleName() + "[", "]")
        // fields
        .add("status=" + this.status)
        .add("notifications=" + this.notifications)
        .toString();
  }

  public static final class Builder {

    private ApolloTransportWatchNotificationsStatus status;
    private List<ApolloTransportNotificationResult> notifications;

    Builder() {
    }

    public Builder status(ApolloTransportWatchNotificationsStatus status) {
      this.status = status;
      return this;
    }

    public Builder notifications(List<ApolloTransportNotificationResult> notifications) {
      this.notifications = notifications == null ? null :
          // nonnull
          (notifications.isEmpty() ? Collections.emptyList()
              : Collections.unmodifiableList(notifications));
      return this;
    }

    public ApolloTransportWatchNotificationsResponse build() {
      return new ApolloTransportWatchNotificationsResponse(this);
    }
  }
}

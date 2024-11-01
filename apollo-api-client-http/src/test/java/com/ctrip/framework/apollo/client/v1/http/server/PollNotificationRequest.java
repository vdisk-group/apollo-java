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
package com.ctrip.framework.apollo.client.v1.http.server;

import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import java.util.List;

public class PollNotificationRequest {

  private String appId;

  private String cluster;

  private List<ApolloConfigNotification> notifications;

  private String dataCenter;

  private String clientIp;

  public String getAppId() {
    return this.appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getCluster() {
    return this.cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public List<ApolloConfigNotification> getNotifications() {
    return this.notifications;
  }

  public void setNotifications(
      List<ApolloConfigNotification> notifications) {
    this.notifications = notifications;
  }

  public String getDataCenter() {
    return this.dataCenter;
  }

  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  public String getClientIp() {
    return this.clientIp;
  }

  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }
}

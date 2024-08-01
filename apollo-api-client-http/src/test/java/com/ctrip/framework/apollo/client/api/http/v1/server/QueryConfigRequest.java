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
package com.ctrip.framework.apollo.client.api.http.v1.server;

import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;

public class QueryConfigRequest {

  private String appId;

  private String cluster;

  private String namespace;

  private String releaseKey;

  private String dataCenter;

  private String clientIp;

  private String label;

  private ApolloNotificationMessages messages;

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

  public String getNamespace() {
    return this.namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getReleaseKey() {
    return this.releaseKey;
  }

  public void setReleaseKey(String releaseKey) {
    this.releaseKey = releaseKey;
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

  public String getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public ApolloNotificationMessages getMessages() {
    return this.messages;
  }

  public void setMessages(ApolloNotificationMessages messages) {
    this.messages = messages;
  }
}

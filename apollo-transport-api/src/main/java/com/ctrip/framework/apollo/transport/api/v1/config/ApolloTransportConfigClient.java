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

public interface ApolloTransportConfigClient {

  /**
   * watch notification changes
   *
   * @param request interested notifications
   * @return changed notifications
   * @throws ApolloTransportConfigException watch failed
   */
  ApolloTransportWatchNotificationsResponse watchNotifications(ApolloTransportEndpoint endpoint,
      ApolloTransportWatchNotificationsRequest request)
      throws ApolloTransportConfigException;

  /**
   * get latest config
   *
   * @param request config queries
   * @return latest config
   * @throws ApolloTransportConfigException         get failed
   * @throws ApolloTransportConfigNotFoundException config not found
   */
  ApolloTransportGetConfigResponse getConfig(ApolloTransportEndpoint endpoint,
      ApolloTransportGetConfigRequest request)
      throws ApolloTransportConfigException, ApolloTransportConfigNotFoundException;
}

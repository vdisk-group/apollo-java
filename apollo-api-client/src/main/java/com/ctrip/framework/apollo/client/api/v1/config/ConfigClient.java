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

import com.ctrip.framework.apollo.client.api.v1.Endpoint;

public interface ConfigClient {

  /**
   * watch notification changes
   *
   * @param request interested notifications
   * @return changed notifications
   * @throws ConfigException watch failed
   */
  WatchNotificationsResponse watch(Endpoint endpoint, WatchNotificationsRequest request)
      throws ConfigException;

  /**
   * get latest config
   *
   * @param request config queries
   * @return latest config
   * @throws ConfigException         get failed
   * @throws ConfigNotFoundException config not found
   */
  GetConfigResponse get(Endpoint endpoint, GetConfigRequest request)
      throws ConfigException, ConfigNotFoundException;
}

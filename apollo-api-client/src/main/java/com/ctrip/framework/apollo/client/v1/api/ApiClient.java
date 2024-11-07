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
package com.ctrip.framework.apollo.client.v1.api;

import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigException;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigNotFoundException;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigRequest;
import com.ctrip.framework.apollo.client.v1.api.config.GetConfigResponse;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsRequest;
import com.ctrip.framework.apollo.client.v1.api.config.WatchNotificationsResponse;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;

public interface ApiClient {

  /**
   * Get the MetaClient. MetaClient is used to discover config service instances.
   */
  MetaClient metaClient();

  /**
   * Get the ConfigClient. ConfigClient is used to watch config changes and get config from config
   * service.
   */
  ConfigClient configClient();
}

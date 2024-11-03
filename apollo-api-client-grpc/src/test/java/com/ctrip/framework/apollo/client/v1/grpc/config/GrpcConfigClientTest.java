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
package com.ctrip.framework.apollo.client.v1.grpc.config;

import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcConfigClientTest {

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void watch_ok() {
//    ConfigClient client = this.createClient();
  }

  private ConfigClient createClient() {
    return GrpcConfigClientFactory.createClient(null, null);
  }

  @Test
  void getConfig() {
  }
}
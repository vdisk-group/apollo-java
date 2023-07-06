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
package com.ctrip.framework.apollo.util.http;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.http.HttpPingClient;
import com.ctrip.framework.apollo.core.http.HttpPingClientProvider;
import javax.annotation.Nonnull;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class MockInjectorHttpPingClientProvider implements HttpPingClientProvider {

  public static final int ORDER = InjectorHttpPingClientProvider.ORDER - 100;

  @Nonnull
  @Override
  public HttpPingClient get() {
    return ApolloInjector.getInstance(HttpPingClient.class);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

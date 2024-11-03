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
package com.ctrip.framework.apollo.config.data.extension.webclient;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.http.HttpClient;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import com.ctrip.framework.apollo.util.http.HttpTransportWrapper;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@Deprecated
public class ApolloWebClientHttpClient implements HttpClient {

  private final HttpTransportWrapper wrapper;

  public ApolloWebClientHttpClient(WebClient webClient) {
    this(webClient, new Gson());
  }

  public ApolloWebClientHttpClient(WebClient webClient, Gson gson) {
    ApolloWebClientHttpTransport transport = new ApolloWebClientHttpTransport(webClient, gson);
    this.wrapper = new HttpTransportWrapper(transport);
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Class<T> responseType)
      throws ApolloConfigException {
    return this.wrapper.doGet(httpRequest, responseType);
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Type responseType)
      throws ApolloConfigException {
    return this.wrapper.doGet(httpRequest, responseType);
  }
}

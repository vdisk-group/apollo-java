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

import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import com.google.common.base.Supplier;
import java.lang.reflect.Type;
import java.util.Objects;

@Deprecated
public class HttpTransportWrapper {

  private final HttpTransport transport;

  public HttpTransportWrapper(HttpTransport transport) {
    Objects.requireNonNull(transport, "transport");
    this.transport = transport;
  }

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ApolloConfigException if any error happened or response code is neither 200 nor 304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Class<T> responseType) {
    HttpTransportRequest transportRequest = this.toTransportRequest(httpRequest);
    return this.doGetInternal(() -> this.transport.doGet(transportRequest, responseType));
  }

  private HttpTransportRequest toTransportRequest(HttpRequest httpRequest) {
    return HttpTransportRequest.builder()
        .url(httpRequest.getUrl())
        .headers(httpRequest.getHeaders())
        .connectTimeout(httpRequest.getConnectTimeout())
        .readTimeout(httpRequest.getReadTimeout())
        .build();
  }

  private <T> HttpResponse<T> doGetInternal(Supplier<HttpTransportResponse<T>> action) {
    HttpTransportResponse<T> transportResponse;
    try {
      transportResponse = action.get();
    } catch (HttpTransportStatusCodeException e) {
      throw new ApolloConfigStatusCodeException(e.getStatusCode(), e.getMessage(), e);
    } catch (Throwable e) {
      throw new ApolloConfigException("Could not complete get operation", e);
    }
    return new HttpResponse<>(transportResponse.getStatusCode(), transportResponse.getBody());
  }

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ApolloConfigException if any error happened or response code is neither 200 nor 304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Type responseType) {
    HttpTransportRequest transportRequest = this.toTransportRequest(httpRequest);
    return this.doGetInternal(() -> this.transport.doGet(transportRequest, responseType));
  }
}

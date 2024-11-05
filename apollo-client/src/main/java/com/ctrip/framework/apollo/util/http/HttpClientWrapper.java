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
import com.ctrip.framework.apollo.core.http.HttpTransportException;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.ctrip.framework.apollo.core.http.TypeReference;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * adapter from {@link HttpClient} to {@link HttpTransport}
 */
@Deprecated
public class HttpClientWrapper implements HttpTransport {

  private final HttpClient httpClient;

  public HttpClientWrapper(HttpClient httpClient) {
    Objects.requireNonNull(httpClient, "httpClient");
    this.httpClient = httpClient;
  }

  @Override
  public <T> HttpTransportResponse<T> doGet(HttpTransportRequest httpTransportRequest,
      TypeReference<T> responseType)
      throws HttpTransportException, HttpTransportStatusCodeException {
    HttpRequest httpRequest = this.toHttpRequest(httpTransportRequest);
    return this.doGetInternal(() -> this.httpClient.doGet(httpRequest, responseType.getType()));
  }

  private <T> HttpTransportResponse<T> doGetInternal(Supplier<HttpResponse<T>> action) {

    HttpResponse<T> httpResponse;
    try {
      httpResponse = action.get();
    } catch (ApolloConfigStatusCodeException e) {
      throw new HttpTransportStatusCodeException(e.getMessage(), e.getStatusCode(), e);
    } catch (ApolloConfigException e) {
      throw new HttpTransportException(e.getMessage(), e);
    } catch (Throwable e) {
      String errorMessage = MessageFormat.format("Could not complete get operation, {0}: {1}",
          e.getClass().getSimpleName(), e.getLocalizedMessage());
      throw new HttpTransportException(errorMessage, e);
    }

    return new HttpTransportResponse<>(httpResponse.getStatusCode(), httpResponse.getBody());
  }

  private HttpRequest toHttpRequest(HttpTransportRequest httpTransportRequest) {
    HttpRequest httpRequest = new HttpRequest(httpTransportRequest.getUrl());
    httpRequest.setHeaders(new LinkedHashMap<>(httpTransportRequest.getHeaders()));
    Integer connectTimeout = httpTransportRequest.getConnectTimeout();
    if (connectTimeout != null) {
      httpRequest.setConnectTimeout(connectTimeout);
    }
    Integer readTimeout = httpTransportRequest.getReadTimeout();
    if (readTimeout != null) {
      httpRequest.setReadTimeout(readTimeout);
    }
    return httpRequest;
  }

  @Override
  public HttpTransportResponse<Void> doGet(HttpTransportRequest httpTransportRequest)
      throws HttpTransportException, HttpTransportStatusCodeException {
    HttpRequest httpRequest = this.toHttpRequest(httpTransportRequest);
    HttpTransportResponse<Object> rawResponse = this.doGetInternal(
        () -> this.httpClient.doGet(httpRequest, Object.class));
    return new HttpTransportResponse<>(rawResponse.getStatusCode(), null);
  }
}

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

import com.ctrip.framework.apollo.core.http.ApolloCoreHttpClient;
import com.ctrip.framework.apollo.core.http.ApolloHttpException;
import com.ctrip.framework.apollo.core.http.ApolloHttpRequest;
import com.ctrip.framework.apollo.core.http.ApolloHttpResponse;
import com.ctrip.framework.apollo.core.http.ApolloHttpStatusCodeException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import java.lang.reflect.Type;

public class DefaultApolloCoreHttpClient implements ApolloCoreHttpClient {

  private final HttpClient httpClient;

  public DefaultApolloCoreHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public <T> ApolloHttpResponse<T> doGet(ApolloHttpRequest httpRequest, Class<T> responseType)
      throws ApolloHttpException, ApolloHttpStatusCodeException {
    try {
      HttpRequest delegateRequest = this.toDelegateRequest(httpRequest);

      HttpResponse<T> delegateResponse = this.httpClient.doGet(delegateRequest, responseType);

      return this.toApolloHttpResponse(delegateResponse);
    } catch (Throwable e) {
      throw this.convertError(e);
    }
  }

  private RuntimeException convertError(Throwable ex) {
    if (ex instanceof ApolloConfigStatusCodeException) {
      ApolloConfigStatusCodeException e = (ApolloConfigStatusCodeException) ex;
      return new ApolloHttpStatusCodeException(e.getLocalizedMessage(), e.getStatusCode(), e);
    } else if (ex instanceof ApolloConfigException) {
      ApolloConfigException e = (ApolloConfigException) ex;
      return new ApolloHttpException(e.getLocalizedMessage(), e);
    } else {
      return new ApolloHttpException("Could not complete get operation", ex);
    }
  }

  private HttpRequest toDelegateRequest(ApolloHttpRequest httpRequest) {
    HttpRequest delegateRequest = new HttpRequest(httpRequest.getUrl());
    delegateRequest.setHeaders(httpRequest.getHeaders());
    delegateRequest.setConnectTimeout(httpRequest.getConnectTimeout());
    delegateRequest.setReadTimeout(httpRequest.getReadTimeout());
    return delegateRequest;
  }

  private <T> ApolloHttpResponse<T> toApolloHttpResponse(HttpResponse<T> delegateResponse) {
    return new ApolloHttpResponse<>(delegateResponse.getStatusCode(), delegateResponse.getBody());
  }

  @Override
  public <T> ApolloHttpResponse<T> doGet(ApolloHttpRequest httpRequest, Type responseType)
      throws ApolloHttpException, ApolloHttpStatusCodeException {
    try {
      HttpRequest delegateRequest = this.toDelegateRequest(httpRequest);

      HttpResponse<T> delegateResponse = this.httpClient.doGet(delegateRequest, responseType);

      return this.toApolloHttpResponse(delegateResponse);
    } catch (Throwable e) {
      throw this.convertError(e);
    }
  }
}

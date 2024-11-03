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
package com.ctrip.framework.apollo.core.http;

public interface HttpTransport {

  /**
   * Do get operation for the http request.
   *
   * @param httpTransportRequest the request
   * @param responseType   the response type
   * @return the response
   * @throws HttpTransportException if any error happened or response code is neither 200 nor 304
   */
  <T> HttpTransportResponse<T> doGet(HttpTransportRequest httpTransportRequest,
      final TypeReference<T> responseType)
      throws HttpTransportException, HttpTransportStatusCodeException;

  /**
   * Do get operation for the http request. The response body is ignored.
   *
   * @param httpTransportRequest the request
   * @return the response without body
   * @throws HttpTransportException if any error happened or response code is neither 200 nor 304
   */
  HttpTransportResponse<Void> doGet(HttpTransportRequest httpTransportRequest)
      throws HttpTransportException, HttpTransportStatusCodeException;
}

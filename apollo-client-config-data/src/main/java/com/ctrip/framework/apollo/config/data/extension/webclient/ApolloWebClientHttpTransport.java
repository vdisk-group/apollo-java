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

import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportException;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.ctrip.framework.apollo.core.http.TypeReference;
import com.google.common.base.Function;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ApolloWebClientHttpTransport implements HttpTransport {

  private final WebClient webClient;

  private final Gson gson;

  public ApolloWebClientHttpTransport(WebClient webClient, Gson gson) {
    Objects.requireNonNull(webClient, "webClient");
    Objects.requireNonNull(gson, "gson");

    this.webClient = webClient;
    this.gson = gson;
  }

  @Override
  public <T> HttpTransportResponse<T> doGet(HttpTransportRequest httpTransportRequest,
      TypeReference<T> responseType)
      throws HttpTransportException, HttpTransportStatusCodeException {
    Objects.requireNonNull(httpTransportRequest, "httpTransportRequest");
    Objects.requireNonNull(responseType, "responseType");

    Function<String, T> convertResponse = input -> this.gson.fromJson(input,
        responseType.getType());

    return this.doGetWithSerializeFunction(httpTransportRequest, convertResponse);
  }

  @Override
  public HttpTransportResponse<Void> doGet(HttpTransportRequest httpTransportRequest)
      throws HttpTransportException, HttpTransportStatusCodeException {
    Objects.requireNonNull(httpTransportRequest, "httpTransportRequest");

    Function<String, Void> convertResponse = input -> null;

    return this.doGetWithSerializeFunction(httpTransportRequest, convertResponse);
  }

  private <T> HttpTransportResponse<T> doGetWithSerializeFunction(
      HttpTransportRequest httpTransportRequest,
      Function<String, T> serializeFunction) {
    try {
      return this.doGetWithSerializeFunctionInternal(httpTransportRequest, serializeFunction);
    } catch (HttpTransportStatusCodeException e) {
      throw e;
    } catch (Throwable e) {
      String errorMessage = MessageFormat.format("Could not complete get operation, {0}: {1}",
          e.getClass().getSimpleName(), e.getLocalizedMessage());
      throw new HttpTransportException(errorMessage, e);
    }
  }


  private <T> HttpTransportResponse<T> doGetWithSerializeFunctionInternal(
      HttpTransportRequest httpTransportRequest,
      Function<String, T> serializeFunction) {
    WebClient.RequestHeadersSpec<?> requestHeadersSpec = this.webClient.get()
        .uri(URI.create(httpTransportRequest.getUrl()));
    if (!CollectionUtils.isEmpty(httpTransportRequest.getHeaders())) {
      for (Map.Entry<String, String> entry : httpTransportRequest.getHeaders().entrySet()) {
        requestHeadersSpec.header(entry.getKey(), entry.getValue());
      }
    }
    return requestHeadersSpec.exchangeToMono(clientResponse -> {
      if (HttpStatus.OK.equals(clientResponse.statusCode())) {
        return clientResponse.bodyToMono(String.class)
            .map(body -> new HttpTransportResponse<>(HttpStatus.OK.value(),
                serializeFunction.apply(body)));
      }
      if (HttpStatus.NOT_MODIFIED.equals(clientResponse.statusCode())) {
        return Mono.just(new HttpTransportResponse<T>(HttpStatus.NOT_MODIFIED.value(), null));
      }
      return Mono.error(new HttpTransportStatusCodeException(
          String.format("Get operation failed for %s", httpTransportRequest.getUrl()),
          clientResponse.rawStatusCode()));
    }).block();
  }

}

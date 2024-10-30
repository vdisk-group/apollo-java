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

import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class DefaultHttpTransport implements HttpTransport {

  private static final Gson GSON = new Gson();

  private final int defaultConnectTimeout;

  private final int defaultReadTimeout;

  public DefaultHttpTransport(HttpTransportProperties properties) {
    Objects.requireNonNull(properties, "properties");
    this.defaultConnectTimeout = Objects.requireNonNull(properties.getDefaultConnectTimeout(),
        "defaultConnectTimeout");
    this.defaultReadTimeout = Objects.requireNonNull(properties.getDefaultReadTimeout(),
        "defaultReadTimeout");
  }

  @Override
  public <T> HttpTransportResponse<T> doGet(HttpTransportRequest httpTransportRequest,
      Class<T> responseType)
      throws HttpTransportException, HttpTransportStatusCodeException {
    Objects.requireNonNull(httpTransportRequest, "httpTransportRequest");
    Objects.requireNonNull(responseType, "responseType");

    Function<String, T> convertResponse = input -> GSON.fromJson(input, responseType);

    return this.doGetWithSerializeFunction(httpTransportRequest, convertResponse);
  }

  @Override
  public <T> HttpTransportResponse<T> doGet(HttpTransportRequest httpTransportRequest,
      Type responseType)
      throws HttpTransportException, HttpTransportStatusCodeException {
    Objects.requireNonNull(httpTransportRequest, "httpTransportRequest");
    Objects.requireNonNull(responseType, "responseType");

    Function<String, T> convertResponse = input -> GSON.fromJson(input, responseType);

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
    InputStreamReader isr = null;
    InputStreamReader esr = null;
    int statusCode;
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(
          httpTransportRequest.getUrl()).openConnection();

      conn.setRequestMethod("GET");

      @Nullable
      Map<String, String> headers = httpTransportRequest.getHeaders();
      if (headers != null && !headers.isEmpty()) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }

      conn.setUseCaches(false);

      Integer connectTimeout = httpTransportRequest.getConnectTimeout();
      conn.setConnectTimeout(this.toTimeoutValue(connectTimeout, this.defaultConnectTimeout));

      Integer readTimeout = httpTransportRequest.getReadTimeout();
      conn.setReadTimeout(this.toTimeoutValue(readTimeout, this.defaultReadTimeout));

      conn.connect();

      statusCode = conn.getResponseCode();
      String response;

      try {
        isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        response = CharStreams.toString(isr);
      } catch (IOException ex) {
        /**
         * according to https://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html,
         * we should clean up the connection by reading the response body so that the connection
         * could be reused.
         */
        InputStream errorStream = conn.getErrorStream();

        if (errorStream != null) {
          esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
          try {
            CharStreams.toString(esr);
          } catch (IOException ioe) {
            //ignore
          }
        }

        // 200 and 304 should not trigger IOException, thus we must throw the original exception out
        if (statusCode == 200 || statusCode == 304) {
          throw ex;
        }
        // for status codes like 404, IOException is expected when calling conn.getInputStream()
        throw new HttpTransportStatusCodeException("", statusCode, ex);
      }

      if (statusCode == 200) {
        return new HttpTransportResponse<>(statusCode, serializeFunction.apply(response));
      }

      if (statusCode == 304) {
        return new HttpTransportResponse<>(statusCode, null);
      }
    } catch (HttpTransportStatusCodeException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new HttpTransportException("Could not complete get operation", ex);
    } finally {
      if (isr != null) {
        try {
          isr.close();
        } catch (IOException ex) {
          // ignore
        }
      }

      if (esr != null) {
        try {
          esr.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }

    throw new HttpTransportStatusCodeException(
        String.format("Get operation failed for %s", httpTransportRequest.getUrl()),
        statusCode);
  }

  private int toTimeoutValue(Integer timeout, int defaultTimeout) {
    if (timeout == null) {
      return defaultTimeout;
    }
    if (timeout < 0) {
      return defaultTimeout;
    }
    return timeout;
  }

}

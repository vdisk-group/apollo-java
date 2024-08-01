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
package com.ctrip.framework.apollo.client.api.http.v1.transport;

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

public class DefaultHttpTransport implements HttpTransport {

  private static final Gson GSON = new Gson();

  private final int connectTimeout;

  private final int readTimeout;

  public DefaultHttpTransport(int connectTimeout, int readTimeout) {
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Class<T> responseType)
      throws HttpException, HttpStatusCodeException {
    Function<String, T> convertResponse = new Function<String, T>() {
      @Override
      public T apply(String input) {
        return GSON.fromJson(input, responseType);
      }
    };

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  @Override
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Type responseType)
      throws HttpException, HttpStatusCodeException {
    Function<String, T> convertResponse = new Function<String, T>() {
      @Override
      public T apply(String input) {
        return GSON.fromJson(input, responseType);
      }
    };

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  private <T> HttpResponse<T> doGetWithSerializeFunction(HttpRequest httpRequest,
      Function<String, T> serializeFunction) {
    InputStreamReader isr = null;
    InputStreamReader esr = null;
    int statusCode;
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(httpRequest.getUrl()).openConnection();

      conn.setRequestMethod("GET");

      Map<String, String> headers = httpRequest.getHeaders();
      if (headers != null && headers.size() > 0) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }

      conn.setConnectTimeout(this.connectTimeout);
      conn.setReadTimeout(this.readTimeout);

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
        throw new HttpStatusCodeException("", statusCode, ex);
      }

      if (statusCode == 200) {
        return new HttpResponse<>(statusCode, serializeFunction.apply(response));
      }

      if (statusCode == 304) {
        return new HttpResponse<>(statusCode, null);
      }
    } catch (HttpStatusCodeException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new HttpException("Could not complete get operation", ex);
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

    throw new HttpStatusCodeException(
        String.format("Get operation failed for %s", httpRequest.getUrl()),
        statusCode);
  }
}

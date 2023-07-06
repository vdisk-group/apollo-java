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

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class DefaultHttpPingClient implements HttpPingClient {

  private static final Logger log = LoggerFactory.getLogger(DefaultHttpPingClient.class);

  private static final int DEFAULT_TIMEOUT_IN_SECONDS = 5000;

  @Override
  public boolean pingUrl(String url) {
    try {
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod("GET");
      connection.setUseCaches(false);
      connection.setConnectTimeout(DEFAULT_TIMEOUT_IN_SECONDS);
      connection.setReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS);
      int statusCode = connection.getResponseCode();
      cleanUpConnection(connection);
      return (200 <= statusCode && statusCode <= 399);
    } catch (Throwable e) {
      if (log.isDebugEnabled()) {
        log.debug("http ping failed [url:{}][err:{}]", url,
            e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), e);
      }
    }
    return false;
  }

  /**
   * according to https://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html, we
   * should clean up the connection by reading the response body so that the connection could be
   * reused.
   */
  public static void cleanUpConnection(HttpURLConnection conn) {
    InputStreamReader isr = null;
    InputStreamReader esr = null;
    try {
      isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
      CharStreams.toString(isr);
    } catch (IOException e) {
      InputStream errorStream = conn.getErrorStream();

      if (errorStream != null) {
        esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
        try {
          CharStreams.toString(esr);
        } catch (IOException ioe) {
          //ignore
        }
      }
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
  }
}

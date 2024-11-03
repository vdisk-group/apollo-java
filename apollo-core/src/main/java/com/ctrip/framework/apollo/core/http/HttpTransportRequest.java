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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class HttpTransportRequest {

  private final String url;

  @Nullable
  private final Map<String, String> headers;

  @Nullable
  private final Integer connectTimeout;

  @Nullable
  private final Integer readTimeout;

  HttpTransportRequest(Builder builder) {
    HttpTransportRequest.validateBuilder(builder);
    this.url = builder.url;
    this.headers = builder.headers;
    this.connectTimeout = builder.connectTimeout;
    this.readTimeout = builder.readTimeout;
  }

  private static void validateBuilder(Builder builder) {
    Objects.requireNonNull(builder.url, "url");
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.url = this.url;
    builder.headers = this.headers;
    builder.connectTimeout = this.connectTimeout;
    builder.readTimeout = this.readTimeout;
    return builder;
  }

  public String getUrl() {
    return this.url;
  }

  @Nullable
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  @Nullable
  public Integer getConnectTimeout() {
    return this.connectTimeout;
  }

  @Nullable
  public Integer getReadTimeout() {
    return this.readTimeout;
  }

  public static final class Builder {

    private String url;
    private Map<String, String> headers;
    private Integer connectTimeout;
    private Integer readTimeout;

    Builder() {
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder headers(Map<String, String> headers) {
      this.headers = headers == null ? null :
          // nonnull
          (headers.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(headers));
      return this;
    }

    public Builder connectTimeout(Integer connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    public Builder readTimeout(Integer readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    public HttpTransportRequest build() {
      return new HttpTransportRequest(this);
    }
  }
}

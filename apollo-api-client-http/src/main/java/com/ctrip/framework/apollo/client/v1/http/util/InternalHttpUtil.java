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
package com.ctrip.framework.apollo.client.v1.http.util;

import com.ctrip.framework.apollo.client.v1.api.Endpoint;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.util.Map;

public class InternalHttpUtil {

  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");

  private static final Escaper PATH_ESCAPER = UrlEscapers.urlPathSegmentEscaper();

  private static final Escaper QUERY_PARAM_ESCAPER = UrlEscapers.urlFormParameterEscaper();

  public static String getActualAddress(Endpoint endpoint) {
    String address = endpoint.getAddress();
    if (address.endsWith("/")) {
      return address.substring(0, address.length() - 1);
    } else {
      return address;
    }
  }

  public static String toPathSegment(String text) {
    return PATH_ESCAPER.escape(text);
  }

  public static String toQueryString(Map<String, String> queryParams) {
    if (InternalCollectionUtil.isEmpty(queryParams)) {
      return "";
    }
    Map<String, String> escapedQueryParams = Maps.newLinkedHashMapWithExpectedSize(
        queryParams.size());
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      escapedQueryParams.put(key, QUERY_PARAM_ESCAPER.escape(value));
    }
    return "?" + MAP_JOINER.join(escapedQueryParams);
  }
}

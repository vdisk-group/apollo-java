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
package com.ctrip.framework.apollo.client.api.http.v1.meta;

import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpException;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpRequest;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpResponse;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpStatusCodeException;
import com.ctrip.framework.apollo.client.api.http.v1.transport.HttpTransport;
import com.ctrip.framework.apollo.client.api.http.v1.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.api.http.v1.util.InternalHttpUtil;
import com.ctrip.framework.apollo.client.api.v1.Endpoint;
import com.ctrip.framework.apollo.client.api.v1.meta.ConfigServiceInstance;
import com.ctrip.framework.apollo.client.api.v1.meta.GetServicesRequest;
import com.ctrip.framework.apollo.client.api.v1.meta.MetaClient;
import com.ctrip.framework.apollo.client.api.v1.meta.MetaException;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpMetaClient implements MetaClient {

  private static final Type GET_SERVICES_RESPONSE_TYPE = new TypeToken<List<ServiceDTO>>() {
  }.getType();

  private final HttpTransport httpTransport;

  public HttpMetaClient(HttpTransport httpTransport) {
    this.httpTransport = Objects.requireNonNull(httpTransport, "httpTransport");
  }

  @Override
  public List<ConfigServiceInstance> getServices(Endpoint endpoint,
      GetServicesRequest request) {
    HttpRequest httpRequest = this.toGetServicesHttpRequest(endpoint, request);
    HttpResponse<List<ServiceDTO>> httpResponse = this.doGet("Get config services", httpRequest,
        GET_SERVICES_RESPONSE_TYPE);
    List<ServiceDTO> serviceDTOList = httpResponse.getBody();
    if (InternalCollectionUtil.isEmpty(serviceDTOList)) {
      return Collections.emptyList();
    }
    List<ConfigServiceInstance> configServiceInstanceList = new ArrayList<>(serviceDTOList.size());
    for (ServiceDTO serviceDTO : serviceDTOList) {
      ConfigServiceInstance configServiceInstance = ConfigServiceInstance.builder()
          .serviceId(serviceDTO.getAppName())
          .instanceId(serviceDTO.getInstanceId())
          .address(serviceDTO.getHomepageUrl())
          .build();
      configServiceInstanceList.add(configServiceInstance);
    }
    return Collections.unmodifiableList(configServiceInstanceList);
  }

  private HttpRequest toGetServicesHttpRequest(Endpoint endpoint, GetServicesRequest request) {
    Map<String, String> queryParams = Maps.newHashMap();
    queryParams.put("appId", request.getAppId());
    String clientIp = request.getClientIp();
    if (!Strings.isNullOrEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }

    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);

    String query = InternalHttpUtil.toQueryString(queryParams);

    String uri = MessageFormat.format("{0}/services/config{1}", actualAddress, query);
    HttpRequest httpRequest = new HttpRequest(uri);
    return httpRequest;
  }

  private <T> HttpResponse<T> doGet(String scene,
      HttpRequest httpRequest, Type responseType) {
    HttpResponse<T> httpResponse;
    try {
      httpResponse = this.httpTransport.doGet(
          httpRequest, responseType);
    } catch (HttpStatusCodeException e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Http status code: {1}",
              scene, e.getStatusCode()),
          e);
    } catch (HttpException e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Http error message: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Error message: {1}",
              scene, e.getLocalizedMessage()), e);
    }
    return httpResponse;
  }
}

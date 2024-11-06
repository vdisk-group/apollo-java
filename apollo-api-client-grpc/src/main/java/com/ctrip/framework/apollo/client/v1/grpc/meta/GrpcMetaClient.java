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
package com.ctrip.framework.apollo.client.v1.grpc.meta;

import com.ctrip.framework.apollo.api.v1.grpc.meta.GrpcDiscoveryRequest;
import com.ctrip.framework.apollo.api.v1.grpc.meta.GrpcDiscoveryResponse;
import com.ctrip.framework.apollo.api.v1.grpc.meta.GrpcServiceInstance;
import com.ctrip.framework.apollo.api.v1.grpc.meta.MetaServiceGrpc;
import com.ctrip.framework.apollo.api.v1.grpc.meta.MetaServiceGrpc.MetaServiceBlockingStub;
import com.ctrip.framework.apollo.client.v1.api.Endpoint;
import com.ctrip.framework.apollo.client.v1.api.meta.ConfigServiceInstance;
import com.ctrip.framework.apollo.client.v1.api.meta.DiscoveryOptions;
import com.ctrip.framework.apollo.client.v1.api.meta.DiscoveryRequest;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaException;
import com.ctrip.framework.apollo.client.v1.grpc.GrpcChannelManager;
import com.ctrip.framework.apollo.client.v1.grpc.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.v1.grpc.util.InternalStringUtil;
import io.grpc.Context;
import io.grpc.Context.CancellableContext;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class GrpcMetaClient implements MetaClient {

  private final GrpcChannelManager channelManager;

  private final GrpcMetaClientProperties properties;

  public GrpcMetaClient(GrpcChannelManager channelManager, GrpcMetaClientProperties properties) {
    Objects.requireNonNull(channelManager, "channelManager");
    Objects.requireNonNull(properties, "properties");
    this.channelManager = channelManager;
    this.properties = properties;
  }

  @Override
  public String traceGetServices(DiscoveryRequest request) {
    return this.toGetServicesUri(request);
  }

  private String toGetServicesUri(DiscoveryRequest request) {
    return "";
  }

  @Override
  public List<ConfigServiceInstance> getServices(DiscoveryRequest request) {
    Endpoint endpoint = request.getEndpoint();

    ManagedChannel channel = this.channelManager.getChannel(endpoint);
    MetaServiceBlockingStub blockingStub = MetaServiceGrpc.newBlockingStub(channel);
    GrpcDiscoveryRequest grpcRequest = this.toGetServicesGrpcRequest(request);

    GrpcDiscoveryResponse grpcResponse = this.doCallInternal(
        "Get config services",
        () -> blockingStub.getServices(grpcRequest));

    List<GrpcServiceInstance> instancesList = grpcResponse.getInstancesList();
    if (InternalCollectionUtil.isEmpty(instancesList)) {
      return Collections.emptyList();
    }

    List<ConfigServiceInstance> configServiceInstanceList = new ArrayList<>(instancesList.size());
    for (GrpcServiceInstance grpcInstance : instancesList) {
      ConfigServiceInstance instance = ConfigServiceInstance.builder()
          .serviceId(grpcInstance.getServiceId())
          .instanceId(grpcInstance.getInstanceId())
          .address(grpcInstance.getAddress())
          .build();
      configServiceInstanceList.add(instance);
    }
    return Collections.unmodifiableList(configServiceInstanceList);

  }

  private GrpcDiscoveryRequest toGetServicesGrpcRequest(DiscoveryRequest request) {
    DiscoveryOptions options = request.getOptions();

    GrpcDiscoveryRequest.Builder builder = GrpcDiscoveryRequest.newBuilder();

    String appId = options.getAppId();
    if (!InternalStringUtil.isEmpty(appId)) {
      builder.setAppId(appId);
    }

    String clientIp = options.getClientIp();
    if (!InternalStringUtil.isEmpty(clientIp)) {
      builder.setClientIp(clientIp);
    }

    return builder.build();
  }

  private <T> T doCallInternal(String scene, Callable<Iterator<T>> action) {
    try (CancellableContext cancellableContext = Context.current().withCancellation()) {
      Iterator<T> responseIterator = cancellableContext.call(action);
      if (responseIterator.hasNext()) {
        return responseIterator.next();
      }
    } catch (StatusRuntimeException e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Grpc Error: {1}",
              scene, e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Error: {1}",
              scene, e.getLocalizedMessage()), e);
    }
    throw new MetaException(
        MessageFormat.format("{0} failed. No response", scene));
  }
}

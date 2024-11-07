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
package com.ctrip.framework.apollo.internals;

import static com.ctrip.framework.apollo.monitor.internal.ApolloClientMonitorConstant.*;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.client.v1.api.meta.ConfigServiceInstance;
import com.ctrip.framework.apollo.client.v1.api.meta.DiscoveryOptions;
import com.ctrip.framework.apollo.client.v1.api.meta.DiscoveryRequest;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;
import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.DeferredLoggerFactory;
import com.ctrip.framework.apollo.core.utils.DeprecatedPropertyNotifyUtil;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.spi.MetaClientHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.util.ArrayList;

public class ConfigServiceLocator {
  private static final Logger logger = DeferredLoggerFactory.getLogger(ConfigServiceLocator.class);
  private final MetaClientHolder m_metaClientHolder;
  private ConfigUtil m_configUtil;
  private AtomicReference<List<ServiceDTO>> m_configServices;
  private ScheduledExecutorService m_executorService;
  /**
   * forbid submit multiple task to {@link #m_executorService}，
   * <p>
   * so use this AtomicBoolean as a signal
   */
  protected AtomicBoolean discoveryTaskQueueMark;
  private RateLimiter m_discoveryRateLimiter;
  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

  /**
   * Create a config service locator.
   */
  public ConfigServiceLocator() {
    List<ServiceDTO> initial = Lists.newArrayList();
    m_configServices = new AtomicReference<>(initial);
    m_metaClientHolder = ApolloInjector.getInstance(MetaClientHolder.class);
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    this.m_executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("ConfigServiceLocator", true));
    this.discoveryTaskQueueMark = new AtomicBoolean(false);
    this.m_discoveryRateLimiter = RateLimiter.create(m_configUtil.getDiscoveryQPS());
    initConfigServices();
  }

  private void initConfigServices() {
    // get from run time configurations
    List<ServiceDTO> customizedConfigServices = getCustomizedConfigService();

    if (customizedConfigServices != null) {
      setConfigServices(customizedConfigServices);
      return;
    }

    // update from meta service
    this.tryUpdateConfigServices();
    this.schedulePeriodicRefresh();
  }

  private List<ServiceDTO> getCustomizedConfigService() {
    // 1. Get from System Property
    String configServices = System.getProperty(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE);
    if (Strings.isNullOrEmpty(configServices)) {
      // 2. Get from OS environment variable
      configServices = System.getenv(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES);
    }
    if (Strings.isNullOrEmpty(configServices)) {
      // 3. Get from server.properties
      configServices = Foundation.server().getProperty(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE, null);
    }
    if (Strings.isNullOrEmpty(configServices)) {
      // 4. Get from deprecated config
      configServices = getDeprecatedCustomizedConfigService();
    }

    if (Strings.isNullOrEmpty(configServices)) {
      return null;
    }

    logger.info("Located config services from apollo.config-service configuration: {}, will not refresh config services from remote meta service!", configServices);

    // mock service dto list
    String[] configServiceUrls = configServices.split(",");
    List<ServiceDTO> serviceDTOS = Lists.newArrayList();

    for (String configServiceUrl : configServiceUrls) {
      configServiceUrl = configServiceUrl.trim();
      ServiceDTO serviceDTO = new ServiceDTO();
      serviceDTO.setHomepageUrl(configServiceUrl);
      serviceDTO.setAppName(ServiceNameConsts.APOLLO_CONFIGSERVICE);
      serviceDTO.setInstanceId(configServiceUrl);
      serviceDTOS.add(serviceDTO);
    }

    return serviceDTOS;
  }

  @SuppressWarnings("deprecation")
  private String getDeprecatedCustomizedConfigService() {
    // 1. Get from System Property
    String configServices = System.getProperty(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE);
    if (!Strings.isNullOrEmpty(configServices)) {
      DeprecatedPropertyNotifyUtil.warn(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE,
          ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE);
    }
    if (Strings.isNullOrEmpty(configServices)) {
      // 2. Get from OS environment variable
      configServices = System.getenv(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES);
      if (!Strings.isNullOrEmpty(configServices)) {
        DeprecatedPropertyNotifyUtil.warn(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES,
            ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE_ENVIRONMENT_VARIABLES);
      }
    }
    if (Strings.isNullOrEmpty(configServices)) {
      // 3. Get from server.properties
      configServices = Foundation.server().getProperty(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE, null);
      if (!Strings.isNullOrEmpty(configServices)) {
        DeprecatedPropertyNotifyUtil.warn(ApolloClientSystemConsts.DEPRECATED_APOLLO_CONFIG_SERVICE,
            ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE);
      }
    }
    return configServices;
  }

  void doSubmitUpdateTask() {
    m_executorService.submit(() -> {
      boolean needUpdate = this.discoveryTaskQueueMark.getAndSet(false);
      if (needUpdate) {
        this.tryUpdateConfigServices();
      }
    });
  }

  void trySubmitUpdateTask() {
    // forbid multiple submit in a short period
    boolean needForceAlready = this.discoveryTaskQueueMark.getAndSet(true);
    if (needForceAlready) {
      // do not submit because submit already, task running
    } else {
      doSubmitUpdateTask();
    }
  }

  /**
   * Get the config service info from remote meta server.
   *
   * @return the services dto
   */
  public List<ServiceDTO> getConfigServices() {
    if (m_configServices.get().isEmpty()) {
      trySubmitUpdateTask();
      // quick fail
      throw new ApolloConfigException(
          "No available config service, "
              + "server side maybe crash or network cannot connect to server from this ip, "
              + "one of meta service url is " + assembleMetaServiceUrl()
      );
    }

    return m_configServices.get();
  }

  private boolean tryUpdateConfigServices() {
    try {
      updateConfigServices();
      return true;
    } catch (Throwable ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("Update config services failed {}", ExceptionUtil.getDetailMessage(ex), ex);
      }
    }
    return false;
  }

  private void schedulePeriodicRefresh() {
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            logger.debug("refresh config services");
            Tracer.logEvent(APOLLO_META_SERVICE, "periodicRefresh");
            tryUpdateConfigServices();
          }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
        m_configUtil.getRefreshIntervalTimeUnit());
  }

  synchronized boolean tryAcquireForUpdate() {
    return this.m_discoveryRateLimiter.tryAcquire();
  }

  private synchronized void updateConfigServices() {
    if (!tryAcquireForUpdate()) {
      // quick return without wait
      return;
    }

    String domainName = m_configUtil.getMetaServerDomainName();
    DiscoveryOptions discoveryOptions = assembleMetaServiceOptions();
    DiscoveryRequest discoveryRequest = DiscoveryRequest.builder()
        .endpoint(domainName)
        .options(discoveryOptions)
        .build();

    MetaClient metaClient = m_metaClientHolder.getMetaClient();
    String url = metaClient.traceGetServices(discoveryRequest);

    int maxRetries = 2;
    Throwable exception = null;

    for (int i = 0; i < maxRetries; i++) {
      Transaction transaction = Tracer.newTransaction("Apollo.MetaService", "getConfigService");
      transaction.addData("Url", url);
      try {
        List<ConfigServiceInstance> serviceInstances = metaClient.getServices(discoveryRequest);
        transaction.setStatus(Transaction.SUCCESS);
        List<ServiceDTO> services = this.toServiceDTOList(serviceInstances);
        if (services == null || services.isEmpty()) {
          logConfigService("Empty response!");
          continue;
        }
        setConfigServices(services);
        return;
      } catch (Throwable ex) {
        if (logger.isDebugEnabled()) {
          logger.debug("getConfigService failed [i:{}] {}", i, ExceptionUtil.getDetailMessage(ex),
              ex);
        }
        Tracer.logEvent(APOLLO_CONFIG_EXCEPTION, ExceptionUtil.getDetailMessage(ex));
        transaction.setStatus(ex);
        exception = ex;
      } finally {
        transaction.complete();
      }

      try {
        m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(m_configUtil.getOnErrorRetryInterval());
      } catch (InterruptedException ex) {
        //ignore
      }
    }

    throw new ApolloConfigException(
        String.format("Get config services failed from %s", url), exception);
  }

  private void setConfigServices(List<ServiceDTO> services) {
    m_configServices.set(services);
    logConfigServices(services);
  }

  DiscoveryOptions assembleMetaServiceOptions() {
    String appId = m_configUtil.getAppId();
    String localIp = m_configUtil.getLocalIp();
    return DiscoveryOptions.builder()
        .appId(appId)
        .clientIp(localIp)
        .build();
  }

  private List<ServiceDTO> toServiceDTOList(List<ConfigServiceInstance> serviceInstances) {
    if (serviceInstances == null || serviceInstances.isEmpty()) {
      return null;
    }
    List<ServiceDTO> services = new ArrayList<>(serviceInstances.size());
    for (ConfigServiceInstance serviceInstance : serviceInstances) {
      ServiceDTO serviceDTO = new ServiceDTO();
      serviceDTO.setAppName(serviceInstance.getServiceId());
      serviceDTO.setInstanceId(serviceInstance.getInstanceId());
      serviceDTO.setHomepageUrl(serviceInstance.getAddress());
      services.add(serviceDTO);
    }
    return services;
  }

  String assembleMetaServiceUrl() {
    String domainName = m_configUtil.getMetaServerDomainName();
    String appId = m_configUtil.getAppId();
    String localIp = m_configUtil.getLocalIp();

    Map<String, String> queryParams = Maps.newLinkedHashMap();
    queryParams.put("appId", queryParamEscaper.escape(appId));
    if (!Strings.isNullOrEmpty(localIp)) {
      queryParams.put("ip", queryParamEscaper.escape(localIp));
    }

    return domainName + "/services/config?" + MAP_JOINER.join(queryParams);
  }

  private void logConfigServices(List<ServiceDTO> serviceDtos) {
    for (ServiceDTO serviceDto : serviceDtos) {
      logConfigService(serviceDto.getHomepageUrl());
    }
  }

  private void logConfigService(String serviceUrl) {
    Tracer.logEvent(APOLLO_CONFIG_SERVICES, serviceUrl);
  }
}

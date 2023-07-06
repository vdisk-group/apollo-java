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

import com.ctrip.framework.apollo.config.data.extension.properties.ApolloClientProperties;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class WebClientCustomizerArgs {

  /**
   * apollo client binded properties
   */
  private final ApolloClientProperties apolloClientProperties;

  /**
   * properties binder
   */
  private final Binder binder;


  /**
   * properties binder Handler
   */
  private final BindHandler bindHandler;

  /**
   * deferred log factory
   */
  private final DeferredLogFactory logFactory;

  /**
   * deferred log
   *
   * @deprecated use {@link #logFactory} instead
   */
  @Deprecated
  private final Log log;

  /**
   * bootstrapContext
   */
  private final ConfigurableBootstrapContext bootstrapContext;

  WebClientCustomizerArgs(ApolloClientProperties apolloClientProperties, Binder binder,
      BindHandler bindHandler, DeferredLogFactory logFactory, Log log,
      ConfigurableBootstrapContext bootstrapContext) {
    this.apolloClientProperties = apolloClientProperties;
    this.binder = binder;
    this.bindHandler = bindHandler;
    this.logFactory = logFactory;
    this.log = log;
    this.bootstrapContext = bootstrapContext;
  }

  public ApolloClientProperties getApolloClientProperties() {
    return this.apolloClientProperties;
  }

  public Binder getBinder() {
    return this.binder;
  }

  public BindHandler getBindHandler() {
    return this.bindHandler;
  }

  public DeferredLogFactory getLogFactory() {
    return this.logFactory;
  }

  @Deprecated
  public Log getLog() {
    return this.log;
  }

  public ConfigurableBootstrapContext getBootstrapContext() {
    return this.bootstrapContext;
  }
}

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
package com.ctrip.framework.apollo.util.http.tls;

import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class HttpTlsConfig {

  private static final Logger log = LoggerFactory.getLogger(HttpTlsConfig.class);

  /**
   * TLS enabled
   */
  private final boolean enabled;

  /**
   * TLS protocol
   */
  private final String protocol;

  /**
   * mTLS client key store location
   */
  private final String keyStore;

  /**
   * mTLS client key password
   */
  private final String keyPassword;

  /**
   * mTLS client key store password
   */
  private final String keyStorePassword;

  /**
   * mTLS client key store type
   */
  private final String keyStoreType;

  /**
   * mTLS client key store provider
   */
  private final String keyStoreProvider;

  /**
   * TLS trust store location
   */
  private final String trustStore;

  /**
   * TLS trust store password
   */
  private final String trustStorePassword;

  /**
   * TLS trust store type
   */
  private final String trustStoreType;

  /**
   * TLS trust store provider
   */
  private final String trustStoreProvider;

  /**
   * TLS trust manager type
   */
  private final String trustManagerType;

  /**
   * TLS hostname verifier type
   */
  private final String hostnameVerifierType;

  /**
   * TLS hostname verifier exact server names
   */
  private final List<String> hostnameVerifierExactServerNames;

  public HttpTlsConfig() {
    this(system());
  }

  HttpTlsConfig(Builder builder) {
    this.enabled = builder.enabled;
    this.protocol = builder.protocol;
    this.keyStore = builder.keyStore;
    this.keyPassword = builder.keyPassword;
    this.keyStorePassword = builder.keyStorePassword;
    this.keyStoreType = builder.keyStoreType;
    this.keyStoreProvider = builder.keyStoreProvider;
    this.trustStore = builder.trustStore;
    this.trustStorePassword = builder.trustStorePassword;
    this.trustStoreType = builder.trustStoreType;
    this.trustStoreProvider = builder.trustStoreProvider;
    this.trustManagerType = builder.trustManagerType;
    this.hostnameVerifierType = builder.hostnameVerifierType;
    this.hostnameVerifierExactServerNames = builder.hostnameVerifierExactServerNames;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder system() {
    return HttpTlsConfig.builder()
        .enabled(initEnabled())
        .protocol(initProtocol())
        .keyStore(initKeyStore())
        .keyPassword(initKeyPassword())
        .keyStorePassword(initKeyStorePassword())
        .keyStoreType(initKeyStoreType())
        .keyStoreProvider(initKeyStoreProvider())
        .trustStore(initTrustStore())
        .trustStorePassword(initTrustStorePassword())
        .trustStoreType(initTrustStoreType())
        .trustStoreProvider(initTrustStoreProvider())
        .trustManagerType(initTrustManagerType())
        .hostnameVerifierType(initHostnameVerifierType())
        .hostnameVerifierExactServerNames(initHostnameVerifierExactServerNames());
  }

  private static boolean initEnabled() {
    return getPropertyBoolean(
        ApolloClientHttpTlsSystemConstants.ENABLED_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.ENABLED_ENVIRONMENT_VARIABLES,
        false);
  }

  private static String initProtocol() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.PROTOCOL_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.PROTOCOL_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initKeyStore() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.KEY_STORE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initKeyPassword() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.KEY_PASSWORD_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.KEY_PASSWORD_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initKeyStorePassword() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PASSWORD_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PASSWORD_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initKeyStoreType() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_TYPE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.KEY_STORE_TYPE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initKeyStoreProvider() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PROVIDER_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PROVIDER_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initTrustStore() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initTrustStorePassword() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PASSWORD_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PASSWORD_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initTrustStoreType() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_TYPE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_TYPE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initTrustStoreProvider() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PROVIDER_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PROVIDER_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initTrustManagerType() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.TRUST_MANAGER_TYPE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.TRUST_MANAGER_TYPE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static String initHostnameVerifierType() {
    return getPropertyString(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_TYPE_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_TYPE_ENVIRONMENT_VARIABLES,
        null);
  }

  private static List<String> initHostnameVerifierExactServerNames() {
    String exactServerNames = getPropertyString(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_SYSTEM_PROPERTIES,
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_ENVIRONMENT_VARIABLES,
        null);
    if (Strings.isNullOrEmpty(exactServerNames)) {
      return Collections.emptyList();
    }
    Splitter splitter = Splitter.on(',')
        .omitEmptyStrings()
        .trimResults();
    return splitter.splitToList(exactServerNames);
  }

  private static String getPropertyString(String propertyName,
      String envName, String defaultValue) {
    String enablePropertyNamesCache = System.getProperty(propertyName);
    if (Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      enablePropertyNamesCache = System.getenv(envName);
    }
    if (Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      enablePropertyNamesCache = Foundation.app().getProperty(propertyName, null);
    }
    if (!Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      return enablePropertyNamesCache;
    }
    return defaultValue;
  }

  private static boolean getPropertyBoolean(String propertyName, String envName,
      boolean defaultValue) {
    String enablePropertyNamesCache = getPropertyString(propertyName, envName, null);
    if (!Strings.isNullOrEmpty(enablePropertyNamesCache)) {
      if ("true".equalsIgnoreCase(enablePropertyNamesCache)) {
        return true;
      } else if ("false".equalsIgnoreCase(enablePropertyNamesCache)) {
        return false;
      } else {
        log.warn("Config for {} is invalid: {}, set default value: {}",
            propertyName, enablePropertyNamesCache, defaultValue);
      }
    }
    return defaultValue;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public String getKeyStore() {
    return this.keyStore;
  }

  public String getKeyPassword() {
    return this.keyPassword;
  }

  public String getKeyStorePassword() {
    return this.keyStorePassword;
  }

  public String getKeyStoreType() {
    return this.keyStoreType;
  }

  public String getKeyStoreProvider() {
    return this.keyStoreProvider;
  }

  public String getTrustStore() {
    return this.trustStore;
  }

  public String getTrustStorePassword() {
    return this.trustStorePassword;
  }

  public String getTrustStoreType() {
    return this.trustStoreType;
  }

  public String getTrustStoreProvider() {
    return this.trustStoreProvider;
  }

  public String getTrustManagerType() {
    return this.trustManagerType;
  }

  public String getHostnameVerifierType() {
    return this.hostnameVerifierType;
  }

  public List<String> getHostnameVerifierExactServerNames() {
    return this.hostnameVerifierExactServerNames;
  }

  public static final class Builder {

    private boolean enabled;
    private String protocol;
    private String keyStore;
    private String keyPassword;
    private String keyStorePassword;
    private String keyStoreType;
    private String keyStoreProvider;
    private String trustStore;
    private String trustStorePassword;
    private String trustStoreType;
    private String trustStoreProvider;
    private String trustManagerType;
    private String hostnameVerifierType;
    private List<String> hostnameVerifierExactServerNames;

    Builder() {
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder protocol(String protocol) {
      this.protocol = protocol;
      return this;
    }

    public Builder keyStore(String keyStore) {
      this.keyStore = keyStore;
      return this;
    }

    public Builder keyPassword(String keyPassword) {
      this.keyPassword = keyPassword;
      return this;
    }

    public Builder keyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
      return this;
    }

    public Builder keyStoreType(String keyStoreType) {
      this.keyStoreType = keyStoreType;
      return this;
    }

    public Builder keyStoreProvider(String keyStoreProvider) {
      this.keyStoreProvider = keyStoreProvider;
      return this;
    }

    public Builder trustStore(String trustStore) {
      this.trustStore = trustStore;
      return this;
    }

    public Builder trustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
      return this;
    }

    public Builder trustStoreType(String trustStoreType) {
      this.trustStoreType = trustStoreType;
      return this;
    }

    public Builder trustStoreProvider(String trustStoreProvider) {
      this.trustStoreProvider = trustStoreProvider;
      return this;
    }

    public Builder trustManagerType(String trustManagerType) {
      this.trustManagerType = trustManagerType;
      return this;
    }

    public Builder hostnameVerifierType(String hostnameVerifierType) {
      this.hostnameVerifierType = hostnameVerifierType;
      return this;
    }

    public Builder hostnameVerifierExactServerNames(List<String> hostnameVerifierExactServerNames) {
      this.hostnameVerifierExactServerNames = hostnameVerifierExactServerNames;
      return this;
    }

    public HttpTlsConfig build() {
      return new HttpTlsConfig(this);
    }
  }
}

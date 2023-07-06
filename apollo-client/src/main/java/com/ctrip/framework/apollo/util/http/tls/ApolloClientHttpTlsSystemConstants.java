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

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientHttpTlsSystemConstants {

  /**
   * apollo http TLS enabled
   */
  public static final String ENABLED_SYSTEM_PROPERTIES = "apollo.http.tls.enabled";

  /**
   * apollo http TLS enabled environment variables
   */
  public static final String ENABLED_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_ENABLED";

  /**
   * apollo http TLS protocol
   */
  public static final String PROTOCOL_SYSTEM_PROPERTIES = "apollo.http.tls.protocol";

  /**
   * apollo http TLS protocol environment variables
   */
  public static final String PROTOCOL_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_PROTOCOL";

  /**
   * apollo http mTLS client key store location
   */
  public static final String KEY_STORE_SYSTEM_PROPERTIES = "apollo.http.tls.key-store";

  /**
   * apollo http mTLS client key store location environment variables
   */
  public static final String KEY_STORE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_KEY_STORE";

  /**
   * apollo http mTLS client key password
   */
  public static final String KEY_PASSWORD_SYSTEM_PROPERTIES = "apollo.http.tls.key-password";

  /**
   * apollo http mTLS client key password environment variables
   */
  public static final String KEY_PASSWORD_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_KEY_PASSWORD";

  /**
   * apollo http mTLS client key store password
   */
  public static final String KEY_STORE_PASSWORD_SYSTEM_PROPERTIES = "apollo.http.tls.key-store-password";

  /**
   * apollo http mTLS client key store password environment variables
   */
  public static final String KEY_STORE_PASSWORD_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_KEY_STORE_PASSWORD";

  /**
   * apollo http mTLS client key store type
   */
  public static final String KEY_STORE_TYPE_SYSTEM_PROPERTIES = "apollo.http.tls.key-store-type";

  /**
   * apollo http mTLS client key store type environment variables
   */
  public static final String KEY_STORE_TYPE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_KEY_STORE_TYPE";

  /**
   * apollo http mTLS client key store provider
   */
  public static final String KEY_STORE_PROVIDER_SYSTEM_PROPERTIES = "apollo.http.tls.key-store-provider";

  /**
   * apollo http mTLS client key store provider environment variables
   */
  public static final String KEY_STORE_PROVIDER_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_KEY_STORE_PROVIDER";

  /**
   * apollo http TLS trust store location
   */
  public static final String TRUST_STORE_SYSTEM_PROPERTIES = "apollo.http.tls.trust-store";

  /**
   * apollo http TLS trust store location environment variables
   */
  public static final String TRUST_STORE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_TRUST_STORE";

  /**
   * apollo http TLS trust store password
   */
  public static final String TRUST_STORE_PASSWORD_SYSTEM_PROPERTIES = "apollo.http.tls.trust-store-password";

  /**
   * apollo http TLS trust store password environment variables
   */
  public static final String TRUST_STORE_PASSWORD_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_TRUST_STORE_PASSWORD";

  /**
   * apollo http TLS trust store type
   */
  public static final String TRUST_STORE_TYPE_SYSTEM_PROPERTIES = "apollo.http.tls.trust-store-type";

  /**
   * apollo http TLS trust store type environment variables
   */
  public static final String TRUST_STORE_TYPE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_TRUST_STORE_TYPE";

  /**
   * apollo http TLS trust store provider
   */
  public static final String TRUST_STORE_PROVIDER_SYSTEM_PROPERTIES = "apollo.http.tls.trust-store-provider";

  /**
   * apollo http TLS trust store provider environment variables
   */
  public static final String TRUST_STORE_PROVIDER_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_TRUST_STORE_PROVIDER";

  /**
   * apollo http TLS trust manager type
   */
  public static final String TRUST_MANAGER_TYPE_SYSTEM_PROPERTIES = "apollo.http.tls.trust-manager-type";

  /**
   * apollo http TLS trust manager type environment variables
   */
  public static final String TRUST_MANAGER_TYPE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_TRUST_MANAGER_TYPE";

  /**
   * apollo http TLS hostname verifier type
   */
  public static final String HOSTNAME_VERIFIER_TYPE_SYSTEM_PROPERTIES = "apollo.http.tls.hostname-verifier-type";

  /**
   * apollo http TLS hostname verifier type environment variables
   */
  public static final String HOSTNAME_VERIFIER_TYPE_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_HOSTNAME_VERIFIER_TYPE";

  /**
   * apollo http TLS hostname verifier exact server names
   */
  public static final String HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_SYSTEM_PROPERTIES = "apollo.http.tls.hostname-verifier-exact-server-names";

  /**
   * apollo http TLS hostname verifier exact server names environment variables
   */
  public static final String HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_ENVIRONMENT_VARIABLES = "APOLLO_HTTP_TLS_HOSTNAME_VERIFIER_EXACT_SERVER_NAMES";
}

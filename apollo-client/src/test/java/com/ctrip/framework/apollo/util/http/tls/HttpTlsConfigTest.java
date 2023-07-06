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

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
class HttpTlsConfigTest {

  @BeforeEach
  void setUp() {
    this.clearProperty();
  }

  @AfterEach
  void tearDown() {
    this.clearProperty();
  }

  private void clearProperty() {
    System.clearProperty(ApolloClientHttpTlsSystemConstants.ENABLED_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.PROTOCOL_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.KEY_PASSWORD_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PASSWORD_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_TYPE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PROVIDER_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PASSWORD_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_TYPE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PROVIDER_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_MANAGER_TYPE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_TYPE_SYSTEM_PROPERTIES);
    System.clearProperty(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_SYSTEM_PROPERTIES);
  }

  @Test
  void testDefault() {
    HttpTlsConfig httpTlsConfig = HttpTlsConfig.system().build();
    Assertions.assertFalse(httpTlsConfig.isEnabled());
    Assertions.assertNull(httpTlsConfig.getProtocol());
    Assertions.assertNull(httpTlsConfig.getKeyStore());
    Assertions.assertNull(httpTlsConfig.getKeyPassword());
    Assertions.assertNull(httpTlsConfig.getKeyStorePassword());
    Assertions.assertNull(httpTlsConfig.getKeyStoreType());
    Assertions.assertNull(httpTlsConfig.getKeyStoreProvider());
    Assertions.assertNull(httpTlsConfig.getTrustStore());
    Assertions.assertNull(httpTlsConfig.getTrustStorePassword());
    Assertions.assertNull(httpTlsConfig.getTrustStoreType());
    Assertions.assertNull(httpTlsConfig.getTrustStoreProvider());
    Assertions.assertNull(httpTlsConfig.getTrustManagerType());
    Assertions.assertNull(httpTlsConfig.getHostnameVerifierType());
    Assertions.assertTrue(
        CollectionUtils.isEmpty(httpTlsConfig.getHostnameVerifierExactServerNames()));
  }

  @Test
  void testSystemProperties() {
    System.setProperty(ApolloClientHttpTlsSystemConstants.ENABLED_SYSTEM_PROPERTIES,
        "true");
    System.setProperty(ApolloClientHttpTlsSystemConstants.PROTOCOL_SYSTEM_PROPERTIES,
        "TLSv1.2");
    System.setProperty(ApolloClientHttpTlsSystemConstants.KEY_STORE_SYSTEM_PROPERTIES,
        "file:/tmp/keystore.jks");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.KEY_PASSWORD_SYSTEM_PROPERTIES,
        "test-password-1");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PASSWORD_SYSTEM_PROPERTIES,
        "test-password-2");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_TYPE_SYSTEM_PROPERTIES, "PKCS12");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.KEY_STORE_PROVIDER_SYSTEM_PROPERTIES,
        "provider-1");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_SYSTEM_PROPERTIES,
        "file:/tmp/truststore.jks");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PASSWORD_SYSTEM_PROPERTIES,
        "test-password-3");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_TYPE_SYSTEM_PROPERTIES, "JKS");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_STORE_PROVIDER_SYSTEM_PROPERTIES,
        "provider-2");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.TRUST_MANAGER_TYPE_SYSTEM_PROPERTIES,
        "SELF_SIGNED_CA");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_TYPE_SYSTEM_PROPERTIES,
        "EXACT_SERVER_NAME");
    System.setProperty(
        ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_SYSTEM_PROPERTIES,
        "test-server-name-1,,test-server-name-2");

    HttpTlsConfig httpTlsConfig = HttpTlsConfig.system().build();
    Assertions.assertTrue(httpTlsConfig.isEnabled());
    Assertions.assertEquals("TLSv1.2", httpTlsConfig.getProtocol());
    Assertions.assertEquals("file:/tmp/keystore.jks", httpTlsConfig.getKeyStore());
    Assertions.assertEquals("test-password-1", httpTlsConfig.getKeyPassword());
    Assertions.assertEquals("test-password-2", httpTlsConfig.getKeyStorePassword());
    Assertions.assertEquals("PKCS12", httpTlsConfig.getKeyStoreType());
    Assertions.assertEquals("provider-1", httpTlsConfig.getKeyStoreProvider());
    Assertions.assertEquals("file:/tmp/truststore.jks", httpTlsConfig.getTrustStore());
    Assertions.assertEquals("test-password-3", httpTlsConfig.getTrustStorePassword());
    Assertions.assertEquals("JKS", httpTlsConfig.getTrustStoreType());
    Assertions.assertEquals("provider-2", httpTlsConfig.getTrustStoreProvider());
    Assertions.assertEquals("SELF_SIGNED_CA", httpTlsConfig.getTrustManagerType());
    Assertions.assertEquals("EXACT_SERVER_NAME", httpTlsConfig.getHostnameVerifierType());
    Assertions.assertEquals(Arrays.asList("test-server-name-1", "test-server-name-2"),
        httpTlsConfig.getHostnameVerifierExactServerNames());
  }

  @Test
  void testEnvironmentVariables() throws Exception {
    SystemLambda.withEnvironmentVariable(
            ApolloClientHttpTlsSystemConstants.ENABLED_ENVIRONMENT_VARIABLES, "true")
        .and(ApolloClientHttpTlsSystemConstants.PROTOCOL_ENVIRONMENT_VARIABLES,
            "TLSv1.2")
        .and(ApolloClientHttpTlsSystemConstants.KEY_STORE_ENVIRONMENT_VARIABLES,
            "file:/tmp/keystore.jks")
        .and(ApolloClientHttpTlsSystemConstants.KEY_PASSWORD_ENVIRONMENT_VARIABLES,
            "test-password-1")
        .and(
            ApolloClientHttpTlsSystemConstants.KEY_STORE_PASSWORD_ENVIRONMENT_VARIABLES,
            "test-password-2")
        .and(ApolloClientHttpTlsSystemConstants.KEY_STORE_TYPE_ENVIRONMENT_VARIABLES,
            "PKCS12")
        .and(
            ApolloClientHttpTlsSystemConstants.KEY_STORE_PROVIDER_ENVIRONMENT_VARIABLES,
            "provider-1")
        .and(ApolloClientHttpTlsSystemConstants.TRUST_STORE_ENVIRONMENT_VARIABLES,
            "file:/tmp/truststore.jks")
        .and(
            ApolloClientHttpTlsSystemConstants.TRUST_STORE_PASSWORD_ENVIRONMENT_VARIABLES,
            "test-password-3")
        .and(ApolloClientHttpTlsSystemConstants.TRUST_STORE_TYPE_ENVIRONMENT_VARIABLES,
            "JKS")
        .and(
            ApolloClientHttpTlsSystemConstants.TRUST_STORE_PROVIDER_ENVIRONMENT_VARIABLES,
            "provider-2")
        .and(
            ApolloClientHttpTlsSystemConstants.TRUST_MANAGER_TYPE_ENVIRONMENT_VARIABLES,
            "SELF_SIGNED_CA")
        .and(
            ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_TYPE_ENVIRONMENT_VARIABLES,
            "EXACT_SERVER_NAME")
        .and(
            ApolloClientHttpTlsSystemConstants.HOSTNAME_VERIFIER_EXACT_SERVER_NAMES_ENVIRONMENT_VARIABLES,
            "test-server-name-1, ,test-server-name-2 ,")
        .execute(() -> {
          HttpTlsConfig httpTlsConfig = HttpTlsConfig.system().build();
          Assertions.assertTrue(httpTlsConfig.isEnabled());
          Assertions.assertEquals("TLSv1.2", httpTlsConfig.getProtocol());
          Assertions.assertEquals("file:/tmp/keystore.jks", httpTlsConfig.getKeyStore());
          Assertions.assertEquals("test-password-1", httpTlsConfig.getKeyPassword());
          Assertions.assertEquals("test-password-2", httpTlsConfig.getKeyStorePassword());
          Assertions.assertEquals("PKCS12", httpTlsConfig.getKeyStoreType());
          Assertions.assertEquals("provider-1", httpTlsConfig.getKeyStoreProvider());
          Assertions.assertEquals("file:/tmp/truststore.jks", httpTlsConfig.getTrustStore());
          Assertions.assertEquals("test-password-3", httpTlsConfig.getTrustStorePassword());
          Assertions.assertEquals("JKS", httpTlsConfig.getTrustStoreType());
          Assertions.assertEquals("provider-2", httpTlsConfig.getTrustStoreProvider());
          Assertions.assertEquals("SELF_SIGNED_CA", httpTlsConfig.getTrustManagerType());
          Assertions.assertEquals("EXACT_SERVER_NAME", httpTlsConfig.getHostnameVerifierType());
          Assertions.assertEquals(Arrays.asList("test-server-name-1", "test-server-name-2"),
              httpTlsConfig.getHostnameVerifierExactServerNames());
        });
  }
}
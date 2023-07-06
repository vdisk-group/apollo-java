/*
 * Copyright 2022 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use HttpTlsUtil file except in compliance with the License.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class HttpTlsFactoryUtil {

  private static final String CLASSPATH_URL_PREFIX = "classpath:";

  private HttpTlsFactoryUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * load key store
   *
   * @param resource resource location
   * @param type     key store type
   * @param provider key store provider
   * @param password key password or key store password
   * @return key store
   * @throws Exception load key store failed
   */
  public static KeyStore loadStore(@Nullable String resource, String type, String provider,
      String password)
      throws Exception {
    if (resource == null) {
      return null;
    }
    String actualType = (type != null) ? type : "JKS";
    KeyStore store = HttpTlsFactoryUtil.newKeyStore(actualType, provider);
    URL url = HttpTlsFactoryUtil.toUrl(resource);
    try (InputStream inputStream = url.openStream()) {
      store.load(inputStream, (password != null) ? password.toCharArray() : null);
    } catch (Throwable e) {
      throw new Exception(
          "Could not load key store [" + resource + "] " + e.getClass().getSimpleName() + ": "
              + e.getLocalizedMessage(), e);
    }
    return store;
  }

  private static KeyStore newKeyStore(String type, String provider)
      throws KeyStoreException, NoSuchProviderException {
    if (provider != null) {
      return KeyStore.getInstance(type, provider);
    }
    return KeyStore.getInstance(type);
  }

  /**
   * resource location to URL
   *
   * @param resource resource location
   * @return URL
   * @throws FileNotFoundException resource not found
   */
  public static URL toUrl(String resource) throws FileNotFoundException {
    Objects.requireNonNull(resource, "resource");
    if (resource.startsWith(CLASSPATH_URL_PREFIX)) {
      String path = resource.substring(CLASSPATH_URL_PREFIX.length());
      ClassLoader cl = HttpTlsFactoryUtil.getClassLoader();
      URL url = cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path);
      if (url == null) {
        String description = "class path resource [" + path + "]";
        throw new FileNotFoundException(description +
            " cannot be resolved to URL because it does not exist");
      }
      return url;
    }
    try {
      return new URL(resource);
    } catch (MalformedURLException ex) {
      try {
        return new File(resource).toURI().toURL();
      } catch (MalformedURLException ex2) {
        throw new FileNotFoundException("Resource location [" + resource +
            "] is neither a URL not a well-formed file path");
      }
    }
  }

  private static ClassLoader getClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      return contextClassLoader;
    }
    return HttpTlsFactoryUtil.class.getClassLoader();
  }
}

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

import java.security.cert.X509Certificate;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface X509HostnameVerifier {

  /**
   * verify the host name is match with the certificate chain
   *
   * @param host  the host
   * @param chain the certificate chain
   * @return false if the verification process fails. otherwise, true.
   */
  boolean verify(String host, X509Certificate[] chain);
}

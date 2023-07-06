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

import java.util.Collections;
import java.util.Map;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@RestController
@SpringBootApplication
public class DefaultHttpTlsFactoryTestApplication {

  @GetMapping("/test")
  public Map<String, Object> test() {
    return Collections.singletonMap("aaa", "bbb");
  }

  @GetMapping("/ping")
  public Map<String, Object> ping() {
    return Collections.singletonMap("aaa", "bbb");
  }
}

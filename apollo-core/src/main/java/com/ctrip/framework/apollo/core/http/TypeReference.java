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
package com.ctrip.framework.apollo.core.http;

import java.lang.reflect.Type;
import java.util.Objects;

public class TypeReference<T> {

  /**
   * The actual type with parameter arguments. {@link java.lang.reflect.ParameterizedType} or
   * {@link java.lang.Class}
   */
  private final Type type;

  TypeReference(Type type) {
    Objects.requireNonNull(type, "type");
    this.type = type;
  }

  public Type getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    return this.type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TypeReference)) {
      return false;
    }
    TypeReference<?> that = (TypeReference<?>) obj;
    return this.type.equals(that.type);
  }
}

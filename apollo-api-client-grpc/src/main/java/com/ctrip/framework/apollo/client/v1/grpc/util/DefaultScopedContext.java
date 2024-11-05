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
package com.ctrip.framework.apollo.client.v1.grpc.util;

import com.google.common.collect.Lists;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DefaultScopedContext implements ScopedContext {

  private final List<ScopedCleaner> cleaners = new ArrayList<>();

  DefaultScopedContext() {
  }

  @Override
  public void addCleaner(ScopedCleaner cleaner) {
    this.addCleanerInternal(cleaner);
  }

  private void addCleanerInternal(ScopedCleaner cleaner) {
    this.cleaners.add(cleaner);
  }

  @Override
  public <T extends AutoCloseable> T addCloseable(T closeable) {
    this.addCleanerInternal(new ScopedCloseableCleaner(closeable));
    return closeable;
  }

  @Override
  public void close() throws ScopedCloseException {
    List<ScopedCleaner> cleaners = this.cleaners;

    // reverse the cleaners to ensure the close order
    List<ScopedCleaner> reversedCleaners = Lists.reverse(cleaners);

    ScopedCloseException ex = null;
    for (ScopedCleaner cleaner : reversedCleaners) {
      try {
        cleaner.clean();
      } catch (Throwable e) {
        if (ex == null) {
          String errorMessage = MessageFormat.format("close error, {0}: {1}",
              e.getClass().getSimpleName(), e.getLocalizedMessage());
          ex = new ScopedCloseException(errorMessage, e);
        } else {
          ex.addSuppressed(e);
        }
      }
    }

    if (ex != null) {
      throw ex;
    }
  }
}

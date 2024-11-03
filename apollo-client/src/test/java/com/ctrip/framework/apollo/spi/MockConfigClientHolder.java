package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;

public class MockConfigClientHolder implements ConfigClientHolder {

  @Override
  public ConfigClient getConfigClient() {
    return DefaultConfigClientHolder.initClient();
  }
}
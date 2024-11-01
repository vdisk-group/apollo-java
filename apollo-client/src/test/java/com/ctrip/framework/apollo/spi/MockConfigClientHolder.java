package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.client.api.v1.config.ConfigClient;

public class MockConfigClientHolder implements ConfigClientHolder {

  @Override
  public ConfigClient getConfigClient() {
    return DefaultConfigClientHolder.initClient();
  }
}
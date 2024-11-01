package com.ctrip.framework.apollo.client.v1.grpc.config;

import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author vdisk
 * @version 1.0
 * @since 2024-11-01 22:40
 */

/**
 * @author yelq
 * @version V3.0
 * @since 2024-11-01 22:40
 */
class GrpcConfigClientTest {

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void watch_ok() {
    ConfigClient client = this.createClient();
  }

  private ConfigClient createClient() {
    return GrpcConfigClientFactory.createClient(null, null);
  }

  @Test
  void getConfig() {
  }
}
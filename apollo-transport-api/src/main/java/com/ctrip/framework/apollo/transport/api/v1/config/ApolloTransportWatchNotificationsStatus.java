package com.ctrip.framework.apollo.transport.api.v1.config;

public enum ApolloTransportWatchNotificationsStatus {

  OK(200),
  NOT_MODIFIED(304),
  ;

  private final int code;

  ApolloTransportWatchNotificationsStatus(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}

package com.ctrip.framework.apollo.transport.api.v1.config;

public enum ApolloTransportGetConfigStatus {

  OK(200),
  NOT_MODIFIED(304),
  ;

  private final int code;

  ApolloTransportGetConfigStatus(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}

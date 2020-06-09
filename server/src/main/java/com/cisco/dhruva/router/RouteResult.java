package com.cisco.dhruva.router;

@FunctionalInterface
public interface RouteResult<T> {
  T getDestination();
}

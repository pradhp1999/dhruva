package com.cisco.dhruva.DsLibs.DsSecurity.DsCert;

import java.util.concurrent.ConcurrentHashMap;

public interface DsCRLGenerator {
  void buildCRL();

  ConcurrentHashMap<String, DsCRL> getCrlHashMap();
}

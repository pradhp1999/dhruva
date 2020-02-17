package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import java.util.concurrent.ConcurrentHashMap;

public interface DsCRLGenerator {
  void buildCRL();

  ConcurrentHashMap<String, DsCRL> getCrlHashMap();
}

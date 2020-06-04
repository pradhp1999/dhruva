package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

public class DsSipServerLocatorFactory {
  private static DsSipServerLocatorFactory dsSipServerLocatorFactory =
      new DsSipServerLocatorFactory();

  public static DsSipServerLocatorFactory getInstance() {
    return dsSipServerLocatorFactory;
  }

  public DsSipServerLocator createNewSIPServerLocator() {
    return new DsSipServerLocator();
  }
}

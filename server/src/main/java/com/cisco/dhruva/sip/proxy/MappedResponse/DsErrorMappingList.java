package com.cisco.dhruva.sip.proxy.MappedResponse;

import com.cisco.dhruva.sip.proxy.Errors.DsProxyErrorCode;
import java.util.List;

public class DsErrorMappingList {

  private String networkName;
  private List<DsErrorMapping> errorMappingList;

  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  public String getNetworkName() {
    return networkName;
  }

  public void setErrorMappingList(List<DsErrorMapping> errorMappingList) {
    this.errorMappingList = errorMappingList;
  }

  public int size() {
    return errorMappingList.size();
  }

  public DsErrorMapping getResponseMappingInfo(int statusCode) {
    for (int i = 0; i < errorMappingList.size(); i++) {
      if (errorMappingList.get(i).isMatching(statusCode)) {
        return errorMappingList.get(i);
      }
    }

    return null;
  }

  public DsErrorMapping getResponseMappingInfo(DsProxyErrorCode errorCode) {

    for (int i = 0; i < errorMappingList.size(); i++) {
      if (errorMappingList.get(i).isMatching(errorCode)) {
        return errorMappingList.get(i);
      }
    }

    return null;
  }
}

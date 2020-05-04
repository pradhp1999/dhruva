package com.cisco.dhruva.sip.proxy.MappedResponse;


import com.cisco.dhruva.sip.proxy.Errors.DsProxyErrorCode;

import java.util.List;

public class DsErrorMappingTable {

    //try to put in hashmap<networkName, List<DsErrorMaping>>
    private List<DsErrorMappingList> errorMappingLists;

    public void setErrorMappingLists(List<DsErrorMappingList> errorMappingLists) {
        this.errorMappingLists = errorMappingLists;
    }

    public boolean isMappingAvailableForNetwork(String networkName) {
        DsErrorMappingList errorMappingList = getErrorMappingListForNetwork(networkName);
        return (errorMappingList != null && errorMappingList.size() > 0);
    }

    public DsErrorMapping getResponseMappingInfo(String networkName, int statusCode) {
    	DsErrorMappingList dsErrorMappingList = getErrorMappingListForNetwork(networkName);
    	if(dsErrorMappingList!=null)
    		return dsErrorMappingList.getResponseMappingInfo(statusCode);
    	else
    		return null;
    }

    public DsErrorMapping getResponseMappingInfo(String networkName, DsProxyErrorCode errorCode) {
    	
    	DsErrorMappingList dsErrorMappingList = getErrorMappingListForNetwork(networkName);
    	if(dsErrorMappingList!=null)
    		return dsErrorMappingList.getResponseMappingInfo(errorCode);
    	else
    		return null;
    }

    private DsErrorMappingList getErrorMappingListForNetwork(String networkName) {
        DsErrorMappingList errorMappingList = null;
        for (int i = 0; i < errorMappingLists.size(); i++) {
            if(networkName.equals(errorMappingLists.get(i).getNetworkName())) {
                errorMappingList = errorMappingLists.get(i);
                break;
            }
        }

        return errorMappingList;
    }
}

package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public class DsProxyServerGroupDownError extends DsProxyError {

    private String serverGroupName = null;

    public DsProxyServerGroupDownError(String serverGroupName) {
        this.serverGroupName = serverGroupName;
        this.errorCode = DsProxyErrorCode.ERROR_SERVER_GROUP_DOWN;
    }

    public String getServerGroupName() {
        return serverGroupName;
    }
    
    @Override
    public String getDescription() {
        return new StringBuilder().append("{errorCode:").append(errorCode)
                .append(",serverGroupName:").append(serverGroupName)
                .append(",errorType:").append(errorCode.getDescription()).append("}").toString();
    }

    @Override
    public DsSipResponse getResponse() {
        return null;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public DsBindingInfo getBindingInfo() {
        return null;
    }
}

package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public class DsProxyFailureResponse extends DsProxyError {

    private DsSipResponse sipResponse;

    public DsProxyFailureResponse(DsSipResponse sipResponse) {
        this.sipResponse = sipResponse;
        this.errorCode = DsProxyErrorCode.ERROR_SIP_FAILURE_RESPONSE;
    }

    public DsSipResponse getSipResponse() {
        return sipResponse;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("{errorCode:").append(errorCode)
            .append(",statusCode:").append(sipResponse.getStatusCode())
            .append(",reasonPharse:").append(sipResponse.getReasonPhrase());
            DsBindingInfo bindingInfo = sipResponse.getBindingInfo();
            if(bindingInfo != null ) {
                if(bindingInfo.getRemoteAddress() != null) {
                    builder.append(",remoteIP:").append(bindingInfo.getRemoteAddress().getHostAddress())
                    .append(",remotePort:").append(bindingInfo.getRemotePort());
                }
                if(bindingInfo.getLocalAddress() != null) {
                    builder.append(",localIP:").append(bindingInfo.getLocalAddress().getHostAddress())
                    .append(",localPort:").append(bindingInfo.getLocalPort());    
                } 
            }
        builder.append(",errorType:").append(errorCode.getDescription()).append("}").toString();
        return builder.toString();
    }

    @Override
    public DsSipResponse getResponse() {
        return sipResponse;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public DsBindingInfo getBindingInfo() {
        return sipResponse.getBindingInfo();
    }
}

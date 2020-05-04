package com.cisco.dhruva.sip.proxy.Errors;


import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public class DsProxyUnidentifiedError extends DsProxyError {

    private Throwable throwable;
    private DsBindingInfo bindingInfo;

    public DsProxyUnidentifiedError(Throwable throwable, DsBindingInfo bindingInfo) {
        this.throwable = throwable;
        this.bindingInfo = bindingInfo;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("{errorCode:").append(errorCode)
            .append(",exceptionClass:").append(throwable.getClass())
            .append(",exceptionMessage:").append(throwable.getLocalizedMessage());
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
                
        builder.append(",errorType:").append(errorCode.getDescription()).append("}");
        return builder.toString();
    }

    @Override
    public DsSipResponse getResponse() {
        return null;
    }

    @Override
    public Throwable getException() {
        return throwable;
    }

    @Override
    public DsBindingInfo getBindingInfo() {
        return bindingInfo;
    }
}

package com.cisco.dhruva.sip.proxy.Errors;

public interface DsProxyErrorListener {

    void notifyError(DsProxyErrorDetail errorDetail);
}

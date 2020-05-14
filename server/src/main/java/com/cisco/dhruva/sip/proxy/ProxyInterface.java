package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;


//MEETPASS
//ProxyResponseInterface not required since proxy adaptor manages the response
public interface ProxyInterface {

    void proxyTo(Location location, DsSipRequest request, ProxyResponseInterface callbackIf);

    void proxyTo(Location location, DsSipRequest request, ProxyResponseInterface callbackIf, long timeout);

    void cancel(Location location, boolean timedOut);

    void respond(DsSipResponse response);
}

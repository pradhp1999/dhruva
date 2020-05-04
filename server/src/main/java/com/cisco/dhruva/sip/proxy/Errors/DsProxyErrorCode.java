package com.cisco.dhruva.sip.proxy.Errors;

public enum  DsProxyErrorCode {

    ERROR_SIP_FAILURE_RESPONSE (700, "SIP Failure Response"),
    ERROR_SIP_REQUEST_TIMEDOUT (701, "SIP Request Timed Out"),
    
    ERROR_TLS_HANDSHAKE_NO_TRUSTED_CERT (711, "TLS Handshake Failed, No Trusted Certificate."),
    ERROR_TLS_HANDSHAKE_CERT_EXPIRED (712, "TLS Hankshake Failed, Certificate Expired"),
    ERROR_TLS_HANDSHAKE_ALERT (713, "TLS Hankshake Failed, Received Fatal Alert"),
    ERROR_TLS_HANDSHAKE_MISC1 (714, "TLS Hankshake Failed, Remote Host Closed Connection During Handshake"),
    ERROR_TLS_HANDSHAKE_CERT_EXT_EE (715, "TLS Hankshake Failed, End Entity Certificate Extension Check Failed"),
    ERROR_TLS_HANDSHAKE_CERT_EXT_CA (716, "TLS Hankshake Failed, CA Certificate Extension Check Failed"),
    ERROR_TLS_HANDSHAKE_CERT_SIGN (717, "TLS Hankshake Failed, Certificate Signature Validation Failed"),
    ERROR_TLS_HANDSHAKE_CERT_CHAIN (718, "TLS Hankshake Failed, Certificate Chaining Error"),
    ERROR_TLS_HANDSHAKE_CERT_SIGN_ALG (719, "TLS Hankshake Failed, Certificate Signature Algorithm Disabled"),
    ERROR_TLS_HANDSHAKE_CERT_HASH (720, "TLS Hankshake Failed, Untrusted Certificate"),
    ERROR_TLS_HANDSHAKE_OTHER (721, "TLS Hankshake Failed, Due To Any Other Reason"),
    ERROR_TLS_HANDSHAKE_TIMEDOUT (722, "TLS Hankshake Failed, Timed Out"),
   
    ERROR_TCP_CONNECTION_REFUSED (731, "TCP Connection Failed, Refused By Remote Host"),
    ERROR_TCP_CONNECTION_OTHER (732, "TCP Connection Failed, Due To Any Other Reason"),
    ERROR_TCP_CONNECTION_TIMEDOUT (733, "TCP Connection Failed, Timed Out"),
    
    ERROR_DNS_A_QUERY_TIMEDOUT (741, "DNS-A-Query Failed, Timed Out."),
    ERROR_DNS_A_NO_RECORDS (742, "DNS-A-Query Failed, No Records Found."),
    ERROR_DNS_A_OTHER (743, "DNS-A-Querry Failed, Due To Any Other Reason"),
    ERROR_DNS_SRV_QUERY_TIMEDOUT (744, "DNS-SRV-Query Failed, Timed Out"),
    ERROR_DNS_SRV_NO_RECORDS (745, "DNS-SRV-Query Failed, No Records Found"),
    ERROR_DNS_SRV_OTHER (746, "DNS-SRV-Query Failed, Due To Any Other Reason"),
    
    ERROR_SERVER_GROUP_DOWN (751, "Server Group Down"),
    ERROR_XCL_ROUTING_FAIlURE (752, "XCL Routing Failed, To Find An Active Route"),
    ERROR_UNIDENTIFIED (760, "Unknown Error"),
    ERROR_CLIENT_UNREACHABLE (761, "Client Unreachable");
    
    private final int value;
    private final String description;
    
    private DsProxyErrorCode(int value, String description)
    {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
}


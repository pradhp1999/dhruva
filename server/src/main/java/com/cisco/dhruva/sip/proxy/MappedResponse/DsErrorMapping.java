package com.cisco.dhruva.sip.proxy.MappedResponse;


import com.cisco.dhruva.sip.proxy.Errors.DsProxyErrorCode;
import com.cisco.dhruva.util.log.Trace;

import java.util.ArrayList;
import java.util.List;



public class DsErrorMapping {
    private DsProxyErrorCode[] errorCode;
    private String source;
    private String remoteStatusCode;
    private int xCiscoReasonCode;
    private String xCiscoReasonText;
    private int mappedStatusCode;
    private String mappedReasonPhrase;

    private static final String XCISCO_REASON_TEXT_DEFAULT = "sip_error";
    private static final String ERROR_SOURCE_REMOTE_DEFAULT = "external";
    private static final String ERROR_SOURCE_LOCAL_DEFAULT = "internal";

    private static Trace Log = Trace.getTrace(DsErrorMapping.class.getName());

    private List<RemoteStatusCodeRange> remoteStatusCodeRangeList;

    public boolean isMatching(int code) {

        if(this.remoteStatusCode == null) {
            return false;
        }

        //parse only once
        if(remoteStatusCodeRangeList == null) {
            this.remoteStatusCodeRangeList = new ArrayList<>();
            String[] statusCodes = this.remoteStatusCode.trim().split("\\s*,\\s*");
            for (String statusCode:statusCodes) {
                String[] statusCodeRange = statusCode.trim().split("\\s*-\\s*");
                try {
                    if(statusCodeRange.length == 1) {
                        int configuredStatusCode = Integer.parseInt(statusCodeRange[0]);
                        this.remoteStatusCodeRangeList.add(new RemoteStatusCodeRange(configuredStatusCode, configuredStatusCode));

                    } else if(statusCodeRange.length == 2) {
                        int configuredStatusCodeMin = Integer.parseInt(statusCodeRange[0]);
                        int configuredStatusCodeMax = Integer.parseInt(statusCodeRange[1]);
                        this.remoteStatusCodeRangeList.add(new RemoteStatusCodeRange(configuredStatusCodeMin, configuredStatusCodeMax));
                    }
                } catch (Exception e) {
                    Log.info("Ignoring the exception [" + e.getLocalizedMessage() + "], while parsing configured remoteStatusCode");
                }
            }
        }

        for (RemoteStatusCodeRange remoteStatusCodeRange : remoteStatusCodeRangeList) {
            if(remoteStatusCodeRange.contains(code)) {
                return true;
            }
        }

        return false;
    }

    public boolean isMatching(DsProxyErrorCode dsProxyErrorCode) {
        if(this.errorCode != null) {
            for (DsProxyErrorCode proxyErrorCode : errorCode) {
                if(proxyErrorCode == dsProxyErrorCode) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setErrorCode(DsProxyErrorCode[] errorCode) {
        this.errorCode = errorCode;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setRemoteStatusCode(String remoteStatusCode) {
        this.remoteStatusCode = remoteStatusCode;
    }

    public void setxCiscoReasonCode(int xCiscoReasonCode) {
        this.xCiscoReasonCode = xCiscoReasonCode;
    }

    public void setxCiscoReasonText(String xCiscoReasonText) {
        this.xCiscoReasonText = xCiscoReasonText;
    }

    public void setMappedStatusCode(int mappedStatusCode) throws Exception {

        if(mappedStatusCode >= MIN_RESPONSE_CODE && mappedStatusCode <= MAX_RESPONSE_CODE) {
            this.mappedStatusCode = mappedStatusCode;
        } else {
            throw new Exception("mappedStatusCode should lie between " + MIN_RESPONSE_CODE + " and " + MAX_RESPONSE_CODE);
        }
    }

    public void setMappedReasonPhrase(String mappedReasonPhrase) {
        this.mappedReasonPhrase = mappedReasonPhrase;
    }

    public DsProxyErrorCode[] getErrorCode() {
        return errorCode;
    }

    public String getSource() {

        if(source != null) {
            return source;
        }

        if(remoteStatusCode != null) {
            return ERROR_SOURCE_REMOTE_DEFAULT;
        } else {
            return ERROR_SOURCE_LOCAL_DEFAULT;
        }
    }

    public String getRemoteStatusCode() {
        return remoteStatusCode;
    }

    public int getxCiscoReasonCode() {
        return xCiscoReasonCode;
    }

    public String getxCiscoReasonText() {
        String reasonText = xCiscoReasonText;
        if(reasonText == null) {
            if(remoteStatusCode != null) {
                reasonText = XCISCO_REASON_TEXT_DEFAULT;
            }
        }

        return reasonText;
    }

    public int getMappedStatusCode() {
        return mappedStatusCode;
    }

    public String getMappedReasonPhrase() {
        return mappedReasonPhrase;
    }

    class RemoteStatusCodeRange {

        private int min;
        private int max;

        public RemoteStatusCodeRange(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public boolean contains(int value) {
            return (value >= min && value <= max);
        }
    }
}


package com.cisco.dhruva.sip.proxy.MappedResponse;

import com.cisco.dhruva.sip.proxy.Errors.DsProxyError;
import com.cisco.dhruva.sip.proxy.Errors.DsProxyErrorCode;
import com.cisco.dhruva.sip.proxy.Errors.DsProxyFailureResponse;
import com.cisco.dhruva.sip.proxy.Errors.DsXclRoutingFailureException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipUnknownHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class DsMappedResponseCreator {

  private static String configFile =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_ERROR_MAPPED_RESPONSE_CONFIG_FILE,
          DsConfigManager.PROP_ERROR_MAPPED_RESPONSE_CONFIG_FILE_PATH_DEFAULT);

  private static DsByteString X_CISCO_INTERNAL_REASON_HEADER =
      new DsByteString("X-Cisco-Internal-Reason");

  private static Logger Log = DhruvaLoggerFactory.getLogger(DsMappedResponseCreator.class);

  private static DsMappedResponseCreator instance;

  private DsErrorMappingTable errorMappingTable;

  public static void initialize() throws Exception {
    instance = new DsMappedResponseCreator();
  }

  public static void initialize(List<DsErrorMappingList> errorMappingLists) throws Exception {
    instance = new DsMappedResponseCreator(errorMappingLists);
  }

  public static DsMappedResponseCreator getInstance() {
    return instance;
  }

  public DsSipResponse createresponse(
      String networkName, List<DsProxyError> proxyErrorList, DsSipResponse response) {

    try {

      if (proxyErrorList == null || proxyErrorList.isEmpty() || response.getStatusCode() < 300) {

        if (proxyErrorList == null || proxyErrorList.isEmpty()) {
          Log.info("skipping error mapped response as error aggregator contains no error");
        } else {
          Log.info(
              "skipping error mapped response as remote status code is "
                  + response.getStatusCode());
        }

        return response;
      }

      if (!errorMappingTable.isMappingAvailableForNetwork(networkName)) {

        Log.info(
            "skipping error mapped response as there is no error mapping for the network "
                + networkName);

        return response;
      }

      // Find an error mapping
      List<DsProxyError> proxyErrorListTemp = new LinkedList<>(proxyErrorList);

      DsErrorMapping errorMapping;
      DsProxyError proxyError;
      do {
        proxyError = proxyErrorListTemp.get(0);
        for (int i = 1; i < proxyErrorListTemp.size(); i++) {
          if (getProxyErrorPriority(proxyErrorListTemp.get(i))
              < getProxyErrorPriority(proxyError)) {
            proxyError = proxyErrorListTemp.get(i);
          }
        }

        if (proxyError.getErrorCode() == DsProxyErrorCode.ERROR_SIP_FAILURE_RESPONSE) {
          errorMapping =
              errorMappingTable.getResponseMappingInfo(
                  networkName,
                  ((DsProxyFailureResponse) proxyError).getSipResponse().getStatusCode());
        } else {
          errorMapping =
              errorMappingTable.getResponseMappingInfo(networkName, proxyError.getErrorCode());
        }

        // debug logging
        if (errorMapping == null) {
          String log = "No error mapping for [" + proxyError.getErrorCode() + "]";
          if (proxyError.getErrorCode() == DsProxyErrorCode.ERROR_SIP_FAILURE_RESPONSE) {
            log +=
                "[remote response status: "
                    + ((DsProxyFailureResponse) proxyError).getSipResponse().getStatusCode()
                    + "]";
          }

          Log.info(log);
        }

        // remove this error from the list as it has no mapping configured. Try to find a mapping
        // for the other errors in the list.
        proxyErrorListTemp.remove(proxyError);
      } while (errorMapping == null && !proxyErrorListTemp.isEmpty());

      // apply the mapping if present
      if (errorMapping != null) {
        applyErrorMapping(response, errorMapping, proxyError);
      } else {
        Log.info(
            "skipping error mapped response as no mapping configured for the error(s) encountered");
      }
    } catch (Throwable t) {
      Log.error("exception while creating mapped response", t);
    }

    return response;
  }

  private void applyErrorMapping(
      DsSipResponse response, DsErrorMapping errorMapping, DsProxyError proxyError)
      throws Exception {

    int mappedStatusCode = errorMapping.getMappedStatusCode();
    String mappedReasonPhrase = errorMapping.getMappedReasonPhrase();
    int xCiscoInternalHeaderCause = errorMapping.getxCiscoReasonCode();
    String xCiscoInternalHeaderReason = errorMapping.getxCiscoReasonText();
    String xCiscoInternalHeaderSource = errorMapping.getSource();
    StringBuffer logMappedError = new StringBuffer("errorCode: ").append(proxyError.getErrorCode());

    // check if the response, cp decided to sent already has X_CISCO_INTERNAL_REASON_HEADER
    DsSipUnknownHeader xCiscoInternalHeader =
        (DsSipUnknownHeader) response.getHeaderValidate(X_CISCO_INTERNAL_REASON_HEADER);
    boolean addXCiscoInternalReasonHeader = (xCiscoInternalHeader == null);

    if (proxyError.getErrorCode() == DsProxyErrorCode.ERROR_SIP_FAILURE_RESPONSE) {

      DsSipResponse remoteResponse = ((DsProxyFailureResponse) proxyError).getSipResponse();

      // passthrough the status code, if mappedStatusCode is not configured.
      if (mappedStatusCode == 0) {
        mappedStatusCode = remoteResponse.getStatusCode();
      }

      // passthrough the reason phrase, if mappedReasonPhrase is not configured.
      if (mappedReasonPhrase == null) {
        mappedReasonPhrase = remoteResponse.getReasonPhrase().toString();
      }

      // If CP decide to sent a different response to UAC than the one it received from UAS
      if (addXCiscoInternalReasonHeader) {

        // set remote status as X-Cisco-Internal-Reason cause.
        xCiscoInternalHeaderCause = remoteResponse.getStatusCode();

        // passthrough the X-Cisco-Internal-Reason header, if present in remote response
        xCiscoInternalHeader =
            (DsSipUnknownHeader) remoteResponse.getHeaderValidate(X_CISCO_INTERNAL_REASON_HEADER);
      }

      // info logging
      logMappedError
          .append(", remote response status: ")
          .append(remoteResponse.getStatusCode())
          .append(", remote response reason phrase: ")
          .append(remoteResponse.getReasonPhrase().toString());
      logMappedError
          .append(", remote response xCiscoInternalHeader: ")
          .append(
              ((xCiscoInternalHeader != null) ? xCiscoInternalHeader.toString() : "NOT PRESENT"));
    } else if (proxyError.getErrorCode() == DsProxyErrorCode.ERROR_XCL_ROUTING_FAIlURE) {

      DsSipResponse locallyGenratedResponse =
          ((DsXclRoutingFailureException) proxyError).getSipResponse();

      // use the internally generated response status code as mappedStatusCode, if mappedStatusCode
      // is not configured
      if (mappedStatusCode == 0) {
        mappedStatusCode = locallyGenratedResponse.getStatusCode();
      }

      // use the internally generated response status code as mappedReasonPhrase, if
      // mappedReasonPhrase is not configured
      if (mappedReasonPhrase == null) {
        mappedReasonPhrase = locallyGenratedResponse.getReasonPhrase().toString();
      }

      if (xCiscoInternalHeaderCause == 0) {
        xCiscoInternalHeaderCause = locallyGenratedResponse.getStatusCode();
      }

      if (xCiscoInternalHeaderReason == null) {
        xCiscoInternalHeaderReason = "config_failure";
      }

      // info logging
      logMappedError
          .append(", response status: ")
          .append(locallyGenratedResponse.getStatusCode())
          .append(", response reason prase: ")
          .append(locallyGenratedResponse.getReasonPhrase().toString());
    }

    response.setStatusCode(mappedStatusCode);
    response.setReasonPhrase(new DsByteString(mappedReasonPhrase));

    // If CP decide to sent a different response to UAC than the one it received from UAS
    if (addXCiscoInternalReasonHeader) {

      if (xCiscoInternalHeader == null) {
        xCiscoInternalHeader =
            createReasonHeader(
                xCiscoInternalHeaderCause, xCiscoInternalHeaderReason, xCiscoInternalHeaderSource);
      }

      response.addHeader(xCiscoInternalHeader);
    }

    Log.info(
        "created mapped response["
            + response.maskAndWrapSIPMessageToSingleLineOutput()
            + "] for error["
            + logMappedError
            + "]");
  }

  private int getProxyErrorPriority(DsProxyError proxyError) {

    if (proxyError.getErrorCode() == DsProxyErrorCode.ERROR_SIP_FAILURE_RESPONSE) {
      return ((DsProxyFailureResponse) proxyError).getSipResponse().getStatusCode();
    } else {
      return proxyError.getErrorCode().getValue();
    }
  }

  private DsSipUnknownHeader createReasonHeader(
      int xCiscoInternalCause, String xCiscoInternalReason, String xCiscoInternalSource) {

    String value =
        "cause="
            + xCiscoInternalCause
            + ";reason="
            + xCiscoInternalReason
            + ";source="
            + xCiscoInternalSource;
    return new DsSipUnknownHeader(X_CISCO_INTERNAL_REASON_HEADER, new DsByteString(value));
  }

  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"})
  private DsMappedResponseCreator() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    this.errorMappingTable = mapper.readValue(new File(configFile), DsErrorMappingTable.class);
  }

  private DsMappedResponseCreator(List<DsErrorMappingList> errorMappingLists) throws Exception {
    this.errorMappingTable = new DsErrorMappingTable();
    this.errorMappingTable.setErrorMappingLists(errorMappingLists);
  }
}

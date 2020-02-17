package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.log.Trace;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

public class DsCRLServiceApiRequest {

  private static final String AEGIS_HOST_NAME =
      DsConfigManager.getProperty(
          DsConfigManager.AEGIS_HOST_NAME, DsConfigManager.AEGIS_HOST_NAME_DEFAULT);
  private static int AEGIS_PORT = 8444;
  private static final String GETCRL =
      "https://" + AEGIS_HOST_NAME + ":" + AEGIS_PORT + "/aegis/aegis/v1/config/ca";
  private static final String GETLAST_REFRESH_TIME =
      "https://" + AEGIS_HOST_NAME + ":" + AEGIS_PORT + "/aegis/aegis/v1/runtime/refresh-time";
  private static Logger Log = Trace.getLogger(DsCRLServiceApiRequest.class.getName());
  private static SSLConnectionSocketFactory sslSocketFactory;
  private static final String CONTENT_TYPE = "application/json";
  private static final int HTTP_TIME_OUT = 2000;

  static {
    DsLog4j.setLoggerLevel("httpclient.wire.header", Level.WARN);
    DsLog4j.setLoggerLevel("httpclient.wire.content", Level.WARN);
    System.setProperty(
        "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
  }

  private static void disableHostnameVerify() {

    if (sslSocketFactory == null) {
      SSLContext sslContext = SSLContexts.createSystemDefault();
      HostnameVerifier allowAll =
          new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
          };
      sslSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAll);
    }
  }

  static void sendCAPatchRequest(JSONArray patchRequestCollection) {
    disableHostnameVerify();
    RequestConfig configParams =
        RequestConfig.custom()
            .setConnectTimeout(HTTP_TIME_OUT)
            .setSocketTimeout(HTTP_TIME_OUT)
            .build();
    HttpResponse patchResponse = null;
    HttpClient patchClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
    HttpPatch httpPatch = new HttpPatch(GETCRL);
    httpPatch.setConfig(configParams);
    int responseCode = 0;
    try {
      StringEntity params = new StringEntity(patchRequestCollection.toString());
      params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE));
      httpPatch.setEntity(params);
      patchResponse = patchClient.execute(httpPatch);
      if (Log.isEnabled(Level.INFO))
        Log.info(
            "Patch Request sent to the centralized service with response code "
                + patchResponse.getStatusLine().getStatusCode());
      responseCode = patchResponse.getStatusLine().getStatusCode();
      if (responseCode == 200) {
        ConnectionSAEventBuilder.logSuccessfulAegisRequest(
            "Success", "CRLPatch", "Successful Patch request of certificate chain", GETCRL);
      } else {
        ConnectionSAEventBuilder.logFailureAegisRequest(
            "Failure",
            "CRLPatch",
            "Failed Patch request of certificate chain",
            responseCode + "",
            GETCRL);
      }
    } catch (IOException e) {
      ConnectionSAEventBuilder.logFailureAegisRequest(
          "Failure", "CRLPatch", e.getMessage(), responseCode + "", GETCRL);
      Log.error("Exception while sending Patch request to the centralized service", e);
    }
  }

  static BufferedReader getCRL() {
    disableHostnameVerify();
    RequestConfig configParams =
        RequestConfig.custom()
            .setConnectTimeout(HTTP_TIME_OUT)
            .setSocketTimeout(HTTP_TIME_OUT)
            .build();
    HttpClient getCRLClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
    HttpGet httpCRLGet = new HttpGet(GETCRL);
    httpCRLGet.setConfig(configParams);
    int responseCode = 0;
    BufferedReader getCRLContent = null;
    try {
      HttpResponse getCRLResponse = getCRLClient.execute(httpCRLGet);
      responseCode = getCRLResponse.getStatusLine().getStatusCode();
      if (responseCode == 200) {
        ConnectionSAEventBuilder.logSuccessfulAegisRequest(
            "Success", "GetCRLMap", "Successful Get CRL API Request", GETCRL);
        getCRLContent =
            new BufferedReader(new InputStreamReader(getCRLResponse.getEntity().getContent()));
        return getCRLContent;
      } else {
        ConnectionSAEventBuilder.logFailureAegisRequest(
            "Failure", "GetCRLMap", "Failed Get CRL API Request", responseCode + "", GETCRL);
      }
    } catch (IOException e) {
      ConnectionSAEventBuilder.logFailureAegisRequest(
          "Failed", "GetCRLMap", e.getMessage(), responseCode + "", GETCRL);
      Log.error("Exception while sending Get CRLMap request to the centralized service", e);
    }
    return getCRLContent;
  }

  static BufferedReader getLastRefreshTime() {
    disableHostnameVerify();
    RequestConfig configParams =
        RequestConfig.custom()
            .setConnectTimeout(HTTP_TIME_OUT)
            .setSocketTimeout(HTTP_TIME_OUT)
            .build();
    HttpClient getLastRefTimeClient =
        HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
    HttpGet httpGet = new HttpGet(GETLAST_REFRESH_TIME);
    httpGet.setConfig(configParams);
    BufferedReader getLastRefTimeContent = null;
    HttpResponse getLastRefTimeResponse = null;
    int responseCode = 0;
    try {
      getLastRefTimeResponse = getLastRefTimeClient.execute(httpGet);
      responseCode = getLastRefTimeResponse.getStatusLine().getStatusCode();
      if (responseCode == 200) {
        ConnectionSAEventBuilder.logSuccessfulAegisRequest(
            "Success",
            "GetLastRefresh",
            "Successful Get Last Refresh Time Request",
            GETLAST_REFRESH_TIME);
        getLastRefTimeContent =
            new BufferedReader(
                new InputStreamReader(getLastRefTimeResponse.getEntity().getContent()));
      } else {
        ConnectionSAEventBuilder.logFailureAegisRequest(
            "Failure",
            "GetLastRefresh",
            "Failed Get Last Refresh Time Request",
            responseCode + "",
            GETLAST_REFRESH_TIME);
      }
    } catch (IOException e) {
      ConnectionSAEventBuilder.logFailureAegisRequest(
          "Failed", "GetLastRefresh", e.getMessage(), responseCode + "", GETLAST_REFRESH_TIME);
      Log.error("Exception while sending GetLastRefreshTime request to the centralized service", e);
    }
    return getLastRefTimeContent;
  }
}

package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.Trace;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DsCRLExecutorService {

  private static Logger LOG = Trace.getLogger(DsCRLExecutorService.class.getName());
  private static final int AEGIS_SCHEDULED_TIME_AFTER_PATCH =
      DsConfigManager.getProperty(
          DsConfigManager.AEGIS_SCHEDULED_TIME_AFTER_PATCH,
          DsConfigManager.AEGIS_SCHEDULED_TIME_AFTER_PATCH_DEFAULT);
  private static ScheduledExecutorService crlFetchExecutorService =
      Executors.newSingleThreadScheduledExecutor();
  private static ScheduledExecutorService crlFetchPeriodicExecutorService =
      Executors.newSingleThreadScheduledExecutor();
  private static Executor missingCRLExecutor = Executors.newSingleThreadExecutor();

  // REFACTOR
  public static void scheduleGetCRLAfterPatch(int seconds) {
    //    Runnable runnable =
    //        new Runnable() {
    //          public void run() {
    //            DsRouteEngine.crlGenerator.buildCRL();
    //          }
    //        };
    //    crlFetchExecutorService.schedule(runnable, seconds, TimeUnit.SECONDS);
    return;
  }

  public static void scheduleGetCRLRequest(int seconds) {
    return;
    // REFACTOR
    //    Runnable runnable =
    //        new Runnable() {
    //          public void run() {
    //            DsRouteEngine.crlGenerator.buildCRL();
    //          }
    //        };
    //    crlFetchPeriodicExecutorService.scheduleAtFixedRate(runnable, 15, seconds,
    // TimeUnit.SECONDS);
  }

  static void getMissingCRL(X509Certificate[] x509Certs) {
    missingCRLExecutor.execute(
        new Runnable() {
          public void run() {
            byte[] x509Encoded = null;
            byte[] x509Base64Encoded = null;
            JSONArray jsonX509Base64EncodedCollection = new JSONArray();
            try {
              for (int i = 0; i < x509Certs.length; i++) {
                x509Encoded = x509Certs[i].getEncoded();
                String base64Encoded = new String(Base64.getEncoder().encode(x509Encoded));
                JSONObject jsonX509Base64Encoded = new JSONObject();
                jsonX509Base64Encoded.put("encodedCert", base64Encoded);
                jsonX509Base64EncodedCollection.put(jsonX509Base64Encoded);
              }
            } catch (CertificateEncodingException e) {
              LOG.error("Exception while encoding the certificate", e);
            } catch (JSONException e) {
              LOG.error(
                  "Exception while handling JSONObject to send Patch request to the centralised service",
                  e);
            }
            DsCRLServiceApiRequest.sendCAPatchRequest(jsonX509Base64EncodedCollection);
            scheduleGetCRLAfterPatch(AEGIS_SCHEDULED_TIME_AFTER_PATCH);
          }
        });
  }
}

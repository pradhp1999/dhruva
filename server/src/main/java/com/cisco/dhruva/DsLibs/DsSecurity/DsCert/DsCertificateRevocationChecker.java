package com.cisco.dhruva.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.Trace;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import org.apache.logging.log4j.Logger;

public class DsCertificateRevocationChecker {
  private static final String CRL_SERVICE =
      DsConfigManager.getProperty(DsConfigManager.CRL_SERVICE, DsConfigManager.CRL_SERVICE_DEFAULT);
  private static final String CRL_OPTION = "centralized";
  private static Logger Log = Trace.getLogger(DsCertificateRevocationChecker.class.getName());
  private static final boolean SOFTFAIL_TLSCONNECTIONS =
      DsConfigManager.getProperty(
          DsConfigManager.SOFTFAIL_TLSCONNECTIONS, DsConfigManager.SOFTFAIL_TLSCONNECTIONS_DEFAULT);

  // REFACTOR
  public static void applyRevocationCheck(Certificate[] certs) throws CertificateException {
    //        ConcurrentHashMap<String, DsCRL> crlHashMap = DsRouteEngine.crlGenerator
    //                .getCrlHashMap();
    //        if(certs == null)
    //        	throw new CertificateException("No certificate passed, its null");
    //        X509Certificate[] x509certs = new X509Certificate[certs.length];
    //        boolean sendPatch = false;
    //        for (int i = 0; i < certs.length; i++) {
    //            x509certs[i] = DsCertificateHelper.getX509Certificate(certs[i]);
    //            if (x509certs[i] == null) {
    //                return;
    //            }
    //            String certSerialNumber = DsCertificateHelper
    //                    .getSerialNumber(x509certs[i]);
    //            String certIssuerPrincipal = DsCertificateHelper
    //                    .getIssuerX500Principal(x509certs[i]);
    //            String issuerCommonName = DsCertificateHelper
    //                    .getCommonName(certIssuerPrincipal);
    //            if (issuerCommonName == null) {
    //                continue;
    //            }
    //            String certDetails = x509certs[i].getSubjectDN()
    //                    + " Serial Number=" + certSerialNumber;
    //            if (crlHashMap == null || crlHashMap.isEmpty()) {
    //                if (CRL_OPTION.equalsIgnoreCase(CRL_SERVICE)) {
    //                    sendPatch = true;
    //                }
    //                if (SOFTFAIL_TLSCONNECTIONS) {
    //                    if (Log.isEnabled(Level.INFO)) {
    //                        Log.info("CRL Map is empty; soft fail is enabled "
    //                                + certDetails);
    //                    }
    //                } else {
    //                    if (Log.isEnabled(Level.ERROR)) {
    //                        Log.error("CRL Map is empty; soft fail is disabled "
    //                                + certDetails);
    //                        throw new CertificateException(certDetails);
    //                    }
    //                }
    //            }
    //            if (crlHashMap.get(issuerCommonName) == null) {
    //                if (CRL_OPTION.equalsIgnoreCase(CRL_SERVICE)) {
    //                    sendPatch = true;
    //                }
    //                if (SOFTFAIL_TLSCONNECTIONS) {
    //                    if (Log.isEnabled(Level.INFO)) {
    //                        Log.info("CA Info not present in CRL Map; soft fail is enabled "
    //                                + certDetails);
    //                    }
    //                } else {
    //                    if (Log.isEnabled(Level.ERROR)) {
    //                        Log.error("CA Info not present in CRL Map; soft fail is disabled "
    //                                + certDetails);
    //                        throw new CertificateException(certDetails);
    //                    }
    //                }
    //            } else {
    //                if (isRevokedCert(certSerialNumber,
    //                        crlHashMap.get(issuerCommonName))) {
    //                    throw new CertificateException(certDetails);
    //                }
    //            }
    //        }
    //        if (sendPatch) {
    //            Log.info("Patch request to be sent to Aegis Service");
    //            DsCRLExecutorService.getMissingCRL(x509certs);
    //        }
  }

  public static boolean isRevokedCert(String certSerialNumber, DsCRL cert_crlDetails) {
    if (cert_crlDetails == null || cert_crlDetails.getRevokedSerialNumbers() == null) {
      return false;
    }
    return cert_crlDetails.getRevokedSerialNumbers().contains(certSerialNumber);
  }
}

package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.HashSet;
import java.util.Set;

public class DsCRLHelper {
  private static HashSet<String> revokedSerialNumbers;

  static HashSet<String> updateRevokedSerialNumbers(X509CRL crl) {
    revokedSerialNumbers = new HashSet<String>();
    Set<? extends X509CRLEntry> revokedCerts = crl.getRevokedCertificates();
    for (X509CRLEntry revokedCert : revokedCerts) {
      revokedSerialNumbers.add(revokedCert.getSerialNumber().toString());
    }
    return revokedSerialNumbers;
  }
}

package com.cisco.dhruva.sip.servergroups.util.testhelper;

import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CertHelper {

  public static Certificate generateCert(String fileName)
      throws CertificateException, FileNotFoundException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate cert =
        cf.generateCertificate(CertHelper.class.getClassLoader().getResourceAsStream(fileName));
    return cert;
  }
}

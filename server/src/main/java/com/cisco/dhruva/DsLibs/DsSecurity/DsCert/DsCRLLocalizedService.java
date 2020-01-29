package com.cisco.dhruva.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.Trace;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class DsCRLLocalizedService implements DsCRLGenerator {
  private static ConcurrentHashMap<String, DsCRL> crlHashMap =
      new ConcurrentHashMap<String, DsCRL>();
  private static final Logger LOG = Trace.getLogger(DsCRLLocalizedService.class.getName());

  /*
   * builds hash map for CRLs with CN as key {CDP Points, Revoked Serial
   * Number} as value
   */
  public void buildCRL() {
    String crlFolderPath =
        DsConfigManager.getProperty(
            DsConfigManager.CRL_FILES_LOCATION, DsConfigManager.CRL_FILES_LOCATION_DEFAULT);
    File[] crlFiles = getCRLFilesFromFolder(crlFolderPath);
    if (crlFiles == null) {
      return;
    }
    for (File crlFile : crlFiles) {
      X509CRL crl = getCRLFromFile(crlFile);
      if (crl != null) addCRLEntry(crl);
    }
  }

  public static File[] getCRLFilesFromFolder(String crlFolderPath) {
    File crlFolder = new File(crlFolderPath);
    if (!crlFolder.exists()) {
      if (LOG.isEnabled(Level.ERROR)) LOG.error("CRL folder not present in " + crlFolderPath);
      return null;
    }
    if (!crlFolder.canRead()) {
      if (LOG.isEnabled(Level.ERROR)) LOG.error("No Read permission " + crlFolderPath);
      return null;
    }
    try {
      return crlFolder.listFiles(
          new FilenameFilter() {
            public boolean accept(File crlFolder, String filename) {
              return filename.endsWith(".der");
            }
          });
    } catch (SecurityException se) {
      if (LOG.isEnabled(Level.ERROR))
        LOG.error("Security Exception while listing files " + crlFolderPath, se);
    }
    return null;
  }

  public static X509CRL getCRLFromFile(File crlFile) {
    X509CRL x509crl = null;
    try {
      x509crl =
          (X509CRL)
              CertificateFactory.getInstance("X.509")
                  .generateCRL(Files.newInputStream(crlFile.toPath()));
    } catch (CRLException e) {
      if (LOG.isEnabled(Level.ERROR))
        LOG.error("CRL file not loaded properly " + crlFile.getAbsolutePath(), e);
    } catch (CertificateException e) {
      if (LOG.isEnabled(Level.ERROR))
        LOG.error("Certificate exception while generating CRL " + crlFile.getAbsolutePath(), e);
    } catch (IOException e) {
      if (LOG.isEnabled(Level.ERROR))
        LOG.error("File not found while generating CRL " + crlFile.getAbsolutePath(), e);
    }
    return x509crl;
  }

  public static void setCRLHashMap(Collection<X509CRL> crls) {
    for (X509CRL crl : crls) {
      addCRLEntry(crl);
    }
  }

  public static void addCRLEntry(X509CRL crl) {
    String crlIssuerPrincipal = crl.getIssuerX500Principal().getName();
    String keyCN = DsCertificateHelper.getCommonName(crlIssuerPrincipal);
    if (keyCN == null || keyCN.isEmpty() || crlHashMap.get(keyCN) != null) {
      return;
    }
    DsCRL crlValue = new DsCRL();
    crlValue.setRevokedSerialNumbers(DsCRLHelper.updateRevokedSerialNumbers(crl));
    crlHashMap.put(keyCN, crlValue);
  }

  public ConcurrentHashMap<String, DsCRL> getCrlHashMap() {
    return crlHashMap;
  }
}

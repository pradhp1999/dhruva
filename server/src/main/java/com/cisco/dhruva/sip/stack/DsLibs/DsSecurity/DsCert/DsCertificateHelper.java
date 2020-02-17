package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.util.log.Trace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;

public class DsCertificateHelper {

  static Logger Log = Trace.getLogger(DsCertificateHelper.class.getName());

  public static X509Certificate getX509Certificate(Certificate cert) {
    ByteArrayInputStream encodedCert = null;
    X509Certificate x509Cert = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      encodedCert = new ByteArrayInputStream(cert.getEncoded());
      x509Cert = (X509Certificate) cf.generateCertificate(encodedCert);
    } catch (CertificateException e) {
      if (Log.isEnabled(Level.WARN))
        Log.warn(
            "Certificate Exception while generating X509Certificate from Certificate " + cert, e);
    } finally {
      try {
        if (encodedCert != null) encodedCert.close();
      } catch (IOException ioe) {
        if (Log.isEnabled(Level.WARN)) Log.warn("IOException while closing the socket ", ioe);
      }
    }
    return x509Cert;
  }

  public static String getSerialNumber(X509Certificate x509cert) {
    return x509cert.getSerialNumber().toString();
  }

  public static String getIssuerX500Principal(X509Certificate x509cert) {
    return x509cert.getIssuerX500Principal().toString();
  }

  public static String getCommonName(String dn) {
    String cn = null;
    try {
      LdapName ldapDN = new LdapName(dn);
      for (Rdn rdn : ldapDN.getRdns()) {
        if (rdn.getType().equals("CN")) {
          cn = rdn.getValue().toString();
          break;
        }
      }
    } catch (InvalidNameException e) {
      if (Log.isEnabled(Level.WARN)) Log.warn("CN name is not valid, making it null " + dn, e);
    }
    return cn;
  }

  public static ArrayList<ArrayList<String>> getCertsInfo(Certificate[] certs) {
    ArrayList<ArrayList<String>> certParsedInfo = new ArrayList<ArrayList<String>>();
    if (certs == null) {
      return certParsedInfo;
    }
    for (Certificate cert : certs) {
      X509Certificate cc = (X509Certificate) cert;
      String crlUrl = getCRLURL(cc);
      String ocspUrl = getOCSPURL(cc);
      ArrayList<String> certInfo = new ArrayList<String>();
      certInfo.add(getCommonName(cc.getSubjectDN().toString()));
      certInfo.add(getCommonName(cc.getIssuerDN().toString()));
      certInfo.add(crlUrl);
      certInfo.add(ocspUrl);
      certInfo.add(cc.getNotAfter().toString());
      certParsedInfo.add(certInfo);
    }
    return certParsedInfo;
  }

  public static String getOCSPURL(X509Certificate x509certificate) {
    ASN1Primitive obj;
    String ID_OCSP = "1.3.6.1.5.5.7.48.1";
    List<String> ocspUrlCollection = new ArrayList<String>();
    String ocspUrl = "NA";
    try {
      // Using oid of AuthorityInfoAcess getting the values
      obj = getExtensionValue(x509certificate, Extension.authorityInfoAccess.getId());
      if (obj == null) {
        return ocspUrl;
      }
      ASN1Sequence AccessDescriptions = (ASN1Sequence) obj;
      int AccessDescriptionSeqenceCount = 2;
      for (int i = 0; i < AccessDescriptions.size(); i++) {
        ASN1Sequence AccessDescription = (ASN1Sequence) AccessDescriptions.getObjectAt(i);
        if (AccessDescription.size() != AccessDescriptionSeqenceCount) {
          continue;
        } else if (AccessDescription.getObjectAt(0) instanceof ASN1ObjectIdentifier) {
          ASN1ObjectIdentifier id = (ASN1ObjectIdentifier) AccessDescription.getObjectAt(0);
          if (ID_OCSP.equals(id.getId())) {
            ASN1Primitive description = (ASN1Primitive) AccessDescription.getObjectAt(1);
            try {
              String AccessLocation = getStringFromGeneralName(description);
              if (AccessLocation != null) {
                ocspUrlCollection.add(AccessLocation);
              }
            } catch (IOException e) {
              if (Log.isEnabled(Level.ERROR)) Log.error("Fetching ocspURL ", e);
            }
          }
        }
      }
    } catch (IOException e) {
      if (Log.isEnabled(Level.ERROR)) Log.error("Fetching ocspURL ", e);
      return ocspUrl;
    }

    return ocspUrlCollection.toString();
  }

  public static String getCRLURL(X509Certificate x509certificate) {

    ASN1Primitive obj;
    String crlUrl = "NA";
    List<String> crlUrlCollection = new ArrayList<String>();
    try {
      obj = getExtensionValue(x509certificate, Extension.cRLDistributionPoints.getId());

    } catch (IOException e) {
      if (Log.isEnabled(Level.ERROR)) Log.error("Fetching URL ", e);
      return crlUrl;
    }
    if (obj == null) {
      return crlUrl;
    }
    CRLDistPoint dist = CRLDistPoint.getInstance(obj);
    DistributionPoint[] dists = dist.getDistributionPoints();
    for (DistributionPoint p : dists) {
      DistributionPointName distributionPointName = p.getDistributionPoint();
      if (DistributionPointName.FULL_NAME != distributionPointName.getType()) {
        continue;
      }
      GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
      GeneralName[] names = generalNames.getNames();
      for (GeneralName name : names) {
        if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
          continue;
        }
        DERIA5String derStr =
            DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
        crlUrlCollection.add(derStr.toString());
      }
    }

    if (crlUrlCollection != null) {
      crlUrl = crlUrlCollection.toString();
    }
    return crlUrl;
  }

  private static String getStringFromGeneralName(ASN1Primitive names) throws IOException {
    ASN1TaggedObject taggedObject = (ASN1TaggedObject) names;
    return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets(), "ISO-8859-1");
  }

  private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid)
      throws IOException {
    byte[] bytes = certificate.getExtensionValue(oid);
    if (bytes == null) {
      return null;
    }
    ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
    ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
    aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
    return aIn.readObject();
  }

  public static List<SubjectAltName> getSubjectAltName(X509Certificate certificate) {
    Collection<List<?>> subjectAltNameFromCert;
    List<SubjectAltName> subjectAltName = null;
    try {
      subjectAltNameFromCert = certificate.getSubjectAlternativeNames();
      subjectAltName = getSANAltNameFromSANList(subjectAltNameFromCert);

    } catch (Exception e) {
      subjectAltName = null;
      Log.error("Error in parsing SAN from certificate ", e);
    }
    return subjectAltName;
  }

  private static List<SubjectAltName> getSANAltNameFromSANList(Collection subjectAltNameFromCert) {
    List<SubjectAltName> subjectAltName = null;
    if (subjectAltNameFromCert != null) {
      subjectAltName = new LinkedList<>();
      Iterator iter = subjectAltNameFromCert.iterator();
      while (iter.hasNext()) {
        List next = (List) iter.next();
        int oid = ((Integer) next.get(0)).intValue();
        switch (oid) {
          case GeneralName.registeredID:
          case GeneralName.otherName:
          case GeneralName.rfc822Name:
          case GeneralName.dNSName:
          case GeneralName.uniformResourceIdentifier:
          case GeneralName.iPAddress:
            SubjectAltName san = new SubjectAltName(oid, next.get(1).toString());
            subjectAltName.add(san);
            break;
          default:
            Log.warn("Unhandled SANType found, SANType = " + oid);
            break;
        }
      }
    }
    return subjectAltName;
  }

  public static String getCommonName(X509Certificate certificate) {
    String cn = null;
    try {
      cn = getCommonName(certificate.getSubjectDN().toString());
    } catch (Exception e) {
      Log.error("Error in parsing CN from certificate ", e);
    }
    return cn;
  }
}

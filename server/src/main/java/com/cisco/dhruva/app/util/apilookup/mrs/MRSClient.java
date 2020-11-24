package com.cisco.dhruva.app.util.apilookup.mrs;

import com.cisco.dhruva.util.log.Trace;
import com.cisco.wx2.client.ClientException;
import com.cisco.wx2.meetingregistry.client.MeetingRegistryClient;
import com.cisco.wx2.meetingregistry.client.MeetingRegistryClientFactory;
import com.cisco.wx2.mrs.common.CcpDomain;
import com.cisco.wx2.mrs.common.QueryCcpDomainRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;

public class MRSClient {
  public static final String MRS_L2SIP_ENDPOINT = "l2sipSipEndpoint";
  public static final String MRS_SITE_FULL_URL = "siteFullUrl";
  public static final String MRS_CMR_VERSION = "cmrVersion";
  private MeetingRegistryClient meetingRegistryClient;
  private QueryCcpDomainRequest queryCcpDomainRequest;
  private static Cache<String, CcpDomain> ccpDomainCache;
  private static Cache<String, ClientException> ccpDomainFailCache;
  private static Object loadLock = new Object();
  private volatile CcpDomain ccpDomain;
  private String ccpDomainCacheKey;
  private static Logger log = Trace.getInstance(MRSClient.class.getName());
  private String mrsDomainUri;
  private String meetingUri;
  private String lookupBody;

  @Inject private MeetingRegistryClientFactory meetingRegistryClientFactory;

  private boolean mrsResponseValidation = true;

  private void setMRSResponseValidation(boolean mrsResponseValidation) {
    this.mrsResponseValidation = mrsResponseValidation;
  }

  public boolean isMRSResponseValid() {
    return mrsResponseValidation;
  }

  public MRSClient(String mrsDomainURI, String meetingUri, String lookupBody) {
    this.mrsDomainUri = mrsDomainURI;
    this.meetingUri = meetingUri;
    this.lookupBody = lookupBody;
    synchronized (loadLock) {
      if (ccpDomainCache == null) {
        ccpDomainCache =
            CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(100, TimeUnit.MINUTES)
                .build();
      }
      if (ccpDomainFailCache == null) {
        ccpDomainFailCache =
            CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(100, TimeUnit.SECONDS)
                .build();
      }
    }

    meetingRegistryClient = meetingRegistryClientFactory.newMeetingRegistryClient(mrsDomainURI);
    queryCcpDomainRequest = new QueryCcpDomainRequest();
    this.ccpDomainCacheKey = meetingUri;
    queryCcpDomainRequest.setMeetingNumber(meetingUri);
  }

  public static void unsetAllCache() {
    if (ccpDomainCache != null) {
      ccpDomainCache.invalidateAll();
    }
    if (ccpDomainFailCache != null) {
      ccpDomainFailCache.invalidateAll();
    }
  }

  public static void initMRSCache(long cacheSize, long cacheExpiry, TimeUnit timeunit) {
    ccpDomainCache =
        CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(cacheExpiry, timeunit)
            .build();
  }

  public static void initMRSFailCache(long cacheSize, long cacheExpiry, TimeUnit timeunit) {
    ccpDomainFailCache =
        CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(cacheExpiry, timeunit)
            .build();
  }

  private void executeQueryCCPDomain() throws ClientException {
    if (meetingRegistryClient != null && queryCcpDomainRequest != null) {
      ccpDomain = meetingRegistryClient.queryCcpDomain(queryCcpDomainRequest);
    } else {
      log.error(
          "Meeting registry client or queryCcpDomainRequest object is null , MRSClient might not have been initialized , meetingRegistryClient = "
              + meetingRegistryClient
              + " queryCcpDomainRequest = "
              + queryCcpDomainRequest);
    }
  }

  private void doMRSLookup() throws ClientException {
    boolean retry = false;
    ClientException exception = null;

    // first try
    try {
      executeQueryCCPDomain();
    } catch (ClientException ce) {
      Throwable cause = ce.getCause();
      if (cause != null && cause instanceof IOException) {
        retry = true;
      } else {
        exception = ce;
      }
    }

    // retry
    if (retry) {
      try {
        executeQueryCCPDomain();
      } catch (ClientException ce) {
        exception = ce;
      }
    }

    if (exception != null) {
      Throwable cause = exception.getCause();
      if (exception.isClientError()
          || exception.isServerError()
          || (cause != null && cause instanceof IOException)) {
        ccpDomainFailCache.put(ccpDomainCacheKey, exception);
        throw exception;
      }
      throw exception;
    }
  }

  public boolean isRedirectResponse() {
    CcpDomain redirectCcpDomain = queryCCPDomain();
    return redirectCcpDomain.isNeedRedirect();
  }

  public String redirectEnv() {
    if (ccpDomain != null) {
      return ccpDomain.getEnv();
    }
    isRedirectResponse();
    return ccpDomain.getEnv();
  }

  private CcpDomain queryCCPDomain() {
    log.info("querying CCP domain " + ccpDomainCacheKey);
    if (ccpDomain != null) {
      return ccpDomain;
    }

    ClientException clientException = ccpDomainFailCache.getIfPresent(ccpDomainCacheKey);

    if (clientException != null) {
      log.error("Request is in failed cache , returning Exception from cache");
      throw clientException;
    }

    ccpDomain = ccpDomainCache.getIfPresent(ccpDomainCacheKey);

    if (ccpDomain != null) {
      log.info("Getting CCPDomain from cache , ccpDomain = " + ccpDomain);
      return ccpDomain;
    }

    try {
      doMRSLookup();
    } finally {

    }

    if (ccpDomain != null) {
      ccpDomainCache.put(ccpDomainCacheKey, ccpDomain);
      log.info("Query ccp domain returned " + ccpDomain);
      log.info(
          ccpDomain.getDomainName()
              + " "
              + ccpDomain.getLocusEndpoint()
              + " "
              + ccpDomain.getDescription());
    }

    return ccpDomain;
  }

  public String getMrsDomainUri() {
    return mrsDomainUri;
  }

  public String getMeetingUri() {
    return meetingUri;
  }

  public String getLookupBody() {
    return lookupBody;
  }

  public String getAttribute(String attribute) {

    CcpDomain ccpDomain = queryCCPDomain();
    String mrsAttribute = null;

    if (ccpDomain != null) {
      switch (attribute) {
        case MRS_L2SIP_ENDPOINT:
          mrsAttribute = ccpDomain.getL2sipSipEndpoint();
          break;
        case MRS_SITE_FULL_URL:
          mrsAttribute = ccpDomain.getSiteFullUrl();
          break;
        case MRS_CMR_VERSION:
          mrsAttribute = ccpDomain.getCmrVersion();
          break;
        default:
          log.error("Key " + attribute + " does not exist in MRSClient");
      }
    } else {
      log.error("ccpDomain is null , MRSLookup returned null");
    }
    return mrsAttribute;
  }
}

package com.cisco.dhruva.DsLibs.DsSecurity.DsCert;

import com.cisco.dhruva.util.log.Trace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class DsCRLCentralizedService implements DsCRLGenerator {

  private static Logger Log = Trace.getLogger(DsCRLCentralizedService.class.getName());
  private static ConcurrentHashMap<String, DsCRL> crlHashMap =
      new ConcurrentHashMap<String, DsCRL>();
  static Date lastRefreshTime = null;

  @Override
  public synchronized void buildCRL() {
    ConcurrentHashMap<String, DsCRL> tmpHashMap;
    ObjectMapper mapper = new ObjectMapper();
    Date lastRefreshServiceTime = getServiceLastRefreshTime();
    if (lastRefreshServiceTime == null) {
      Log.error("Last Refresh Service Time is null ");
    }
    if (lastRefreshTime == null
        || lastRefreshServiceTime == null
        || lastRefreshTime.before(lastRefreshServiceTime)) {
      try (BufferedReader crlContent = DsCRLServiceApiRequest.getCRL()) {
        TypeFactory typeFactory = mapper.getTypeFactory();
        MapType mapType =
            typeFactory.constructMapType(ConcurrentHashMap.class, String.class, DsCRL.class);
        tmpHashMap = mapper.readValue(crlContent, mapType);
        if (tmpHashMap != null && !tmpHashMap.isEmpty()) {
          crlHashMap = tmpHashMap;
        }
        lastRefreshTime = lastRefreshServiceTime;
      } catch (IOException e) {
        Log.error("Exception while building CRL HashMap from Centralized Service ", e);
      }
    }
  }

  private static Date getServiceLastRefreshTime() {

    String inputLine;
    try (BufferedReader lastRefreshTimeContent = DsCRLServiceApiRequest.getLastRefreshTime()) {

      StringBuffer sb = new StringBuffer();
      if (lastRefreshTimeContent == null) {
        return null;
      }
      while ((inputLine = lastRefreshTimeContent.readLine()) != null) {
        sb.append(inputLine);
      }
      JSONObject lastRefreshTimeJson = new JSONObject(sb.toString());
      SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
      return sdf.parse(lastRefreshTimeJson.getString("lastRefresh"));
    } catch (JSONException | ParseException | IOException e) {
      Log.error("Exception while getting LastRefreshTime from Centralized Service ", e);
    }
    return null;
  }

  public ConcurrentHashMap<String, DsCRL> getCrlHashMap() {
    return crlHashMap;
  }
}

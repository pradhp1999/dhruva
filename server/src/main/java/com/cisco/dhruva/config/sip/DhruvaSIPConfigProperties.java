package com.cisco.dhruva.config.sip;

import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.JsonUtilFactory;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DhruvaSIPConfigProperties {

  public static final String SIP_LISTEN_POINTS = "sipListenPoints";

  public static final String PROXY = "proxy";

  public static final Transport DEFAULT_TRANSPORT = Transport.UDP;

  // MeetPass TODO
  // Env is not read properly, hence setting it here to true
  public static final boolean DEFAULT_RECORD_ROUTE_ENABLED = true;

  private Logger logger = DhruvaLoggerFactory.getLogger(DhruvaSIPConfigProperties.class);

  public static int DEFAULT_PORT_UDP = 5060;

  @Autowired private Environment env;

  public List<SIPListenPoint> getListeningPoints() {

    String configuredListeningPoints = env.getProperty(SIP_LISTEN_POINTS);

    List<SIPListenPoint> listenPoints;

    if (configuredListeningPoints != null) {
      try {
        listenPoints =
            Arrays.asList(
                JsonUtilFactory.getInstance(JsonUtilFactory.JsonUtilType.LOCAL)
                    .toObject(configuredListeningPoints, SIPListenPoint[].class));
      } catch (Exception e) {
        // TODO shoould we generate an Alarm
        logger.error(
            "Error converting JSON ListenPoint configuration provided in the environment , default listenpoint will be choosen ",
            e);
        listenPoints = getDefaultListenPoints();
      }

    } else {
      listenPoints = getDefaultListenPoints();
    }

    logger.info("Listen points from the {} configuration {}", SIP_LISTEN_POINTS, listenPoints);

    return listenPoints;
  }

  private List<SIPListenPoint> getDefaultListenPoints() {

    List<SIPListenPoint> listenPoints = new ArrayList<SIPListenPoint>();

    SIPListenPoint udpListenPoint = new SIPListenPoint.SIPListenPointBuilder().build();

    listenPoints.add(udpListenPoint);

    return listenPoints;
  }
}

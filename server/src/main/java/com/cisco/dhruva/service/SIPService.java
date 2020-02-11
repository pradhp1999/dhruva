/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.service;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.transport.DhruvaTransportLayer;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.TransportLayerFactory;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SIPService {

  Logger logger = DhruvaLoggerFactory.getLogger(SIPService.class);

  @Autowired
  DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  @Autowired
  private NetworkConfig networkConfig;

  private MessageForwarder messageForwarder;

  @PostConstruct
  public void init() throws Exception {

    List<SIPListenPoint> sipListenPoints = dhruvaSIPConfigProperties.getListeningPoints();

    //dummy implementation of MessageForwarder

    messageForwarder = ((messageBytes, bindingInfo) -> {

      String receivedMessage = new String(messageBytes);
      logger.info("Received Message from {} ,in MessageForwarder ,message is {} ", bindingInfo,
          receivedMessage);

    });

    initTransportLayer(sipListenPoints);

  }

  private void initTransportLayer(
      List<SIPListenPoint> sipListenPoints) throws Exception {

    logger.info("Starting Dhruva Transport Layer");
    DhruvaTransportLayer dhruvaTransportLayer = (DhruvaTransportLayer) TransportLayerFactory
        .getInstance().getTransportLayer();

    ArrayList<CompletableFuture> listenPointFutures = new ArrayList<CompletableFuture>();

    for (SIPListenPoint sipListenPoint : sipListenPoints) {

      logger.info("Starting ListenPoint {} ", sipListenPoint);
      CompletableFuture listenPointFuture = dhruvaTransportLayer
          .startListening(sipListenPoint.getTransport(), networkConfig,
              InetAddress.getByName(sipListenPoint.getHostIPAddress()), sipListenPoint.getPort(),
              messageForwarder);
      listenPointFutures.add(listenPointFuture);
    }

    listenPointFutures.forEach(CompletableFuture::join);
  }

}

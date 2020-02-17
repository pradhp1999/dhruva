/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.service;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.SipPacketProcessor;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.SipTransactionManager;
import com.cisco.dhruva.transport.DhruvaTransportLayer;
import com.cisco.dhruva.transport.TransportLayerFactory;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SIPService {

  Logger logger = DhruvaLoggerFactory.getLogger(SIPService.class);

  @Autowired DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  @Autowired private NetworkConfig networkConfig;

  private SipPacketProcessor sipPacketProcessor;

  private SipTransactionManager sipTransactionManager;

  private ExecutorService executorService;

  @PostConstruct
  public void init() throws Exception {

    List<SIPListenPoint> sipListenPoints = dhruvaSIPConfigProperties.getListeningPoints();

    executorService = new ExecutorService("DhruvaSipServer");
    executorService.startExecutorService(ExecutorType.SIP_TRANSACTION_PROCESSOR, 10);
    sipPacketProcessor = new SipPacketProcessor(executorService);
    sipTransactionManager = new SipTransactionManager();


    initTransportLayer(sipListenPoints);
  }

  private void initTransportLayer(List<SIPListenPoint> sipListenPoints) throws Exception {

    logger.info("Starting Dhruva Transport Layer");
    DhruvaTransportLayer dhruvaTransportLayer =
        (DhruvaTransportLayer) TransportLayerFactory.getInstance().getTransportLayer(sipPacketProcessor);


    ArrayList<CompletableFuture> listenPointFutures = new ArrayList<CompletableFuture>();

    for (SIPListenPoint sipListenPoint : sipListenPoints) {

      logger.info("Starting ListenPoint {} ", sipListenPoint);
      CompletableFuture listenPointFuture =
          dhruvaTransportLayer.startListening(
              sipListenPoint.getTransport(),
              networkConfig,
              InetAddress.getByName(sipListenPoint.getHostIPAddress()),
              sipListenPoint.getPort(),
              sipPacketProcessor);

      listenPointFutures.add(listenPointFuture);
    }

    listenPointFutures.forEach(CompletableFuture::join);
  }
}

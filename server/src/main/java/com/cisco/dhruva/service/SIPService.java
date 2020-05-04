/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.service;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.re.controllers.DsREControllerFactory;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsSipProxyManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransportLayer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.SipPacketProcessor;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.DhruvaTransportLayer;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.TransportLayerFactory;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SIPService {

  Logger logger = DhruvaLoggerFactory.getLogger(SIPService.class);

  @Autowired DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  private DsNetwork networkConfig;

  private SipPacketProcessor sipPacketProcessor;

  private DsSipTransactionManager sipTransactionManager;

  private ExecutorService executorService;

  @Autowired private Environment env;

  private DsSipTransportLayer sipTransportLayer;

  private DhruvaTransportLayer dhruvaTransportLayer;

  @PostConstruct
  public void init() throws Exception {

    List<SIPListenPoint> sipListenPoints = dhruvaSIPConfigProperties.getListeningPoints();
    executorService = new ExecutorService("DhruvaSipServer");
    executorService.startExecutorService(ExecutorType.SIP_TRANSACTION_PROCESSOR, 10);
    sipPacketProcessor = new SipPacketProcessor(executorService);
    initTransportLayer(sipListenPoints);
    sipTransportLayer = new DsSipTransportLayer(null, sipPacketProcessor, dhruvaTransportLayer);
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager proxyManager = new DsSipProxyManager(sipTransportLayer, controllerFactory);
    proxyManager.setRouteFixInterface(controllerFactory);
  }

  private void initTransportLayer(List<SIPListenPoint> sipListenPoints) throws Exception {

    logger.info("Starting Dhruva Transport Layer");
    dhruvaTransportLayer =
        (DhruvaTransportLayer)
            TransportLayerFactory.getInstance()
                .getTransportLayer(sipPacketProcessor, executorService);

    ArrayList<CompletableFuture> listenPointFutures = new ArrayList<CompletableFuture>();

    InetAddress listenAddress;
    int listenPort;
    Transport listenTransport;

    for (SIPListenPoint sipListenPoint : sipListenPoints) {

      logger.info("Starting ListenPoint {} ", sipListenPoint);

      networkConfig = DsNetwork.getNetwork(sipListenPoint.getName());
      networkConfig.setenv(env);

      CompletableFuture listenPointFuture =
          dhruvaTransportLayer.startListening(
              sipListenPoint.getTransport(),
              networkConfig,
              InetAddress.getByName(sipListenPoint.getHostIPAddress()),
              sipListenPoint.getPort(),
              sipPacketProcessor);

      listenPointFuture.whenComplete(
          (channel, throwable) -> {
            if (throwable == null) {
              try {
                DsControllerConfig.addListenInterface(
                    networkConfig,
                    InetAddress.getByName(sipListenPoint.getHostIPAddress()),
                    sipListenPoint.getPort(),
                    sipListenPoint.getTransport(),
                    InetAddress.getByName(sipListenPoint.getHostIPAddress()));

                if (sipListenPoint.isRecordRoute()) {
                  DsControllerConfig.addRecordRouteInterface(
                      InetAddress.getByName(sipListenPoint.getHostIPAddress()),
                      sipListenPoint.getPort(),
                      sipListenPoint.getTransport(),
                      networkConfig);
                }
              } catch (Exception e) {
                logger.error(
                    "Exception in ListenPointFuture.whenComplete while configuring DsController Config ",
                    e);
              }
            }
          });

      listenPointFutures.add(listenPointFuture);
    }

    listenPointFutures.forEach(CompletableFuture::join);
  }
}

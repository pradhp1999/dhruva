/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.service;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.sip.controller.DsREControllerFactory;
import com.cisco.dhruva.sip.proxy.DsSipProxyManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTimer;
import com.cisco.dhruva.transport.DhruvaTransportLayer;
import com.cisco.dhruva.transport.TransportLayerFactory;
import com.cisco.dhruva.util.LMAUtil;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SIPService {

  Logger logger = DhruvaLoggerFactory.getLogger(SIPService.class);

  @Autowired DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  private SipPacketProcessor sipPacketProcessor;

  private DsSipTransactionManager sipTransactionManager;

  @Autowired private ExecutorService executorService;

  @Autowired private Environment env;

  private DsSipTransportLayer sipTransportLayer;

  private DhruvaTransportLayer dhruvaTransportLayer;

  @Autowired public MetricService metricsService;

  @Autowired LMAUtil lmaUtil;

  @Autowired SipServerLocatorService resolver;

  @PostConstruct
  public void init() throws Exception {

    List<SIPListenPoint> sipListenPoints = dhruvaSIPConfigProperties.getListeningPoints();
    executorService.startExecutorService(ExecutorType.SIP_TRANSACTION_PROCESSOR, 10);
    DsTimer.startTimers(executorService);
    sipPacketProcessor = new SipPacketProcessor(executorService);
    initTransportLayer(sipListenPoints);

    DsSipServerTransactionImpl.configureExecutor(executorService);
    DsSipClientTransactionImpl.configureExecutor(executorService);

    sipTransportLayer = new DsSipTransportLayer(null, sipPacketProcessor, dhruvaTransportLayer);
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipTransactionFactory transactionFactory = new DsSipDefaultTransactionFactory();
    // TODO DNS pass resolver here
    DsSipProxyManager proxyManager =
        new DsSipProxyManager(sipTransportLayer, controllerFactory, transactionFactory, resolver);
    proxyManager.setRouteFixInterface(controllerFactory);
  }

  private void initTransportLayer(List<SIPListenPoint> sipListenPoints) throws Exception {

    logger.info("Starting Dhruva Transport Layer");
    dhruvaTransportLayer =
        (DhruvaTransportLayer)
            TransportLayerFactory.getInstance()
                .getTransportLayer(sipPacketProcessor, executorService, metricsService);

    ArrayList<CompletableFuture> listenPointFutures = new ArrayList<CompletableFuture>();

    for (SIPListenPoint sipListenPoint : sipListenPoints) {

      logger.info("Trying to start server socket on {} ", sipListenPoint);

      DsNetwork networkConfig = DsNetwork.getNetwork(sipListenPoint.getName());
      networkConfig.setDhruvaConfigProperties(dhruvaSIPConfigProperties);
      networkConfig.setTlsAuthenticationType(sipListenPoint.getTlsAuthType());

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
                logger.info("Server socket created for {}", channel);
                DsControllerConfig.addListenInterface(
                    networkConfig,
                    InetAddress.getByName(sipListenPoint.getHostIPAddress()),
                    sipListenPoint.getPort(),
                    sipListenPoint.getTransport(),
                    InetAddress.getByName(sipListenPoint.getHostIPAddress()),
                    sipListenPoint.shouldAttachExternalIP());

                if (sipListenPoint.isRecordRoute()) {
                  DsControllerConfig.addRecordRouteInterface(
                      InetAddress.getByName(sipListenPoint.getHostIPAddress()),
                      sipListenPoint.getPort(),
                      sipListenPoint.getTransport(),
                      networkConfig);
                }
              } catch (Exception e) {
                logger.error(
                    "Configuring Listenpoint in DsControllerConfig failed for ListenPoint  "
                        + channel,
                    e);
              }
            } else {
              // TODO: should Dhruva exit ? or generate an Alarm
              logger.error(
                  "Server socket creation failed for {} , error is {} ", channel, throwable);
            }
          });

      listenPointFutures.add(listenPointFuture);
    }

    listenPointFutures.forEach(CompletableFuture::join);
  }

  @PreDestroy
  private void releaseServiceResources() {
    logger.info(
        "Releasing Resources as part of App shutdown, Shutting down executors and Transport layer");
    executorService.shutdown();
    dhruvaTransportLayer.shutdown();
    logger.info("Executor service and Transport layer shutdown complete as part of App shutdown");
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }
}

/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.mockito.Mock;;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DhruvaTransportLayerTest {

  @Mock
  Environment env = new MockEnvironment();
  
  @BeforeTest
  void init() {


  }

  @Test
   public void test()
      throws UnknownHostException, InterruptedException, UnknownHostException {

    NetworkConfig networkConfig=mock(NetworkConfig.class);
    when(networkConfig.UDPEventPoolThreadCount()).thenReturn(1);
    TransportLayer transportLayer = TransportLayerFactory.getInstance().getTransportLayer();
    MessageForwarder handler = new MessageForwarder() {
      @Override
      public void processMessage(byte[] messageBytes, DsBindingInfo bindingInfo) {
        System.out.println("Received bytes " + new String(messageBytes));
        System.out.println("Binding info "+bindingInfo.getRemoteAddressStr()+" "+bindingInfo.getLocalAddress());
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    CompletableFuture future = transportLayer
        .startListening(Transport.UDP, networkConfig, InetAddress.getByName("0.0.0.0"), 5070, handler);

    CompletableFuture future2=transportLayer.startListening(Transport.UDP,networkConfig, InetAddress.getByName("0.0.0.0"), 5071, handler);

    System.out.println("waiting for future to complete "+future.isDone());

    future.whenComplete((o, o2) -> {

        System.out.println("Future is complete "+future.isDone());
        System.out.println("first argument "+o);
        System.out.println("Second Argument "+ o2);
        try {
          System.out.println("Future get"+future.get());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }

    });

     future2.whenComplete((o, o2) -> {

       System.out.println("Future is complete "+future.isDone());
       System.out.println("first argument "+o);
       System.out.println("Second Argument "+ o2);
       try {
         System.out.println("Future get"+future2.get());
       } catch (InterruptedException e) {
         e.printStackTrace();
       } catch (ExecutionException e) {
         e.printStackTrace();
       }

     });

    Thread.sleep(100000000);


  }
}
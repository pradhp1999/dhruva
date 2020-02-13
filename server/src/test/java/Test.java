/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.DhruvaTransportLayer;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.TransportLayerFactory;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class Test {


  public static void main(String[] args)
  {

    try {

      DhruvaTransportLayer transportLayer = (DhruvaTransportLayer) TransportLayerFactory.getInstance().getTransportLayer((a,b)->{
            System.out.println("MessageForwarder "+new String(a) +" ");
          }

      );
      NetworkConfig networkConfig=null;
      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("10.78.98.21"),
              5060);
      Connection udpConnection = connectionFuture.get();

      CompletableFuture writeFuture=udpConnection.send(SIPMessageGenerator.getInviteMessage("graivitt").getBytes());

      System.out.println(writeFuture);
      System.out.println(writeFuture.isDone());
      System.out.println(writeFuture.get());
      System.out.println(writeFuture.isDone());



    } catch (Exception e) {
     e.printStackTrace();
    }
  }
  }

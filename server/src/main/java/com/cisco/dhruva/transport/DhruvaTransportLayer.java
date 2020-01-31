/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.transport.config.NetworkConfig;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DhruvaTransportLayer implements TransportLayer {

    @Override
    public CompletableFuture startListening(Transport transportType, NetworkConfig transportConfig, InetAddress address, int port, MessageHandler handler) {

        CompletableFuture serverStartFuture = new CompletableFuture();


        if(transportType == null) {
            serverStartFuture.completeExceptionally(new NullPointerException("TransportType passed to NettyTransportLayer.startListening is null"));
            return serverStartFuture;
        }

        switch (transportType) {
            case UDP:
                ServerFactory.getInstance(transportType).startListening(transportConfig,address, port,serverStartFuture);
                break;
            case TCP:
                break;
            case TLS:
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + transportType);
        }

    }

    @Override
    public CompletableFuture<Connection> getConnection(NetworkConfig networkConfig, Transport transportType, InetAddress localAddress, int localPort, InetAddress remoteAddress, int remotePort) {
        return null;
    }

    @Override
    public HashMap<Transport, Integer> getConnectionSummary() {
        return null;
    }
}

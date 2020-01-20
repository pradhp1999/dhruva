/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport;

public interface MessageHandler {

  public void processMessage(byte[] messageBytes);
}

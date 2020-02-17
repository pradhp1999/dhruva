// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipFrameStream;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSocket;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * Provides the functionality to read and process incoming SIP messages. This class runs in its own
 * thread. Keep waiting for messages to come on the socket input stream, frame and construct the SIP
 * message and put the constructed message in its work processor queue to be processed.
 */
public class DsSipMessageReader extends DsMessageReader {

  /**
   * Constructs message reader object with the specified parameters.
   *
   * @param str the input stream to read messages from
   * @param queue the work queue where to put messages to be processed
   * @param sock the socket associated with input stream
   * @param binfo the binding info of the incoming message
   */
  public DsSipMessageReader(InputStream str, Executor queue, DsSocket sock, DsBindingInfo binfo) {
    super(createFrameStream(str, binfo), queue, sock, binfo);
  }

  protected byte[] frameMsg(InputStream str) throws EOFException, IOException {
    return ((DsSipFrameStream) str).readMsg();
  }

  protected DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo) {
    return new DsSipMessageBytes(bytes, binfo);
  }

  private static DsSipFrameStream createFrameStream(InputStream str, DsBindingInfo binfo) {
    DsSipFrameStream fs = new DsSipFrameStream(str);
    fs.mark(binfo.getNetwork().getMaxTcpMsgSize());

    return fs;
  }
}

/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransportLayer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ByteBuffer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.security.*;
import javax.crypto.*;
import org.apache.logging.log4j.Level;

/**
 * An auxilary singleton class used to do Via header encryption/decsryption and loop detection Note
 * that init() MUST be called before getInstance()
 */
public class DsViaHandler {

  // This is (almost) a singleton. Note that init() MUST be called
  // before getInstance()
  private static DsViaHandler viaHandler;

  private KeyGenerator keyGen;
  private Cipher encoder, decoder;
  private SecretKey desKey;
  private MyBase64Encoder base64Encoder;
  private MyBase64Decoder base64Decoder;
  private DsSipTransportLayer transportLayer;
  private DsLoopCheckInterface loopDetector;

  private static final Trace Log = Trace.getTrace(DsViaHandler.class.getName());

  private DsViaHandler(DsSipTransportLayer transportLayer)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

    if (transportLayer == null) throw new NullPointerException("TransportLayer must not be null");

    keyGen = DsProxyUtils.getAESKeyGenerator();

    desKey = keyGen.generateKey();
    encoder = Cipher.getInstance("AES/ECB/PKCS5Padding");
    decoder = Cipher.getInstance("AES/ECB/PKCS5Padding");
    encoder.init(Cipher.ENCRYPT_MODE, desKey);
    decoder.init(Cipher.DECRYPT_MODE, desKey);
    base64Encoder = new MyBase64Encoder();
    base64Decoder = new MyBase64Decoder();
    this.transportLayer = transportLayer;
    this.loopDetector = new DsMaxForwardsLoopDetector(this);
  }

  public void setLoopDetector(DsLoopCheckInterface loopDetector) {
    this.loopDetector = loopDetector;
  }

  protected DsSipTransportLayer getTransportLayer() {
    return transportLayer;
  }

  protected MyBase64Encoder getBase64Encoder() {
    return base64Encoder;
  }

  /**
   * Takes an unencrypted Via header and encrypts it using certain encryption algorithm The
   * encrypted <b>via</b> will contain hidden parameter
   *
   * @param via Via header to encrypt. After the method returns, the host-port is encrypted and the
   *     header contains hidden parameter
   */
  protected void encryptVia(DsSipViaHeader via) {
    try (ByteBuffer buf = new ByteBuffer(64)) {
      buf.write(via.getHost());
      if (via.isPortPresent()) {
        buf.write(':');
        buf.write(DsIntStrCache.intToBytes(via.getPort()));
      }

      DsByteString received = via.getReceived();
      if (received != null) { // encrypt received attribute as well
        buf.write('|');
        buf.write(received);
        via.removeReceived();
      }

      byte[] hostPort = buf.toByteArray();

      // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //	  via.setConcealedHost(encoder.doFinal(hostPort));
      via.setConcealedHost(new DsByteString(base64Encoder.encode(encoder.doFinal(hostPort))));
      //	  via.hasPort(false);

      via.setHidden(true);
    } catch (IllegalBlockSizeException e) {
      if (Log.on && Log.isEnabled(Level.WARN)) Log.warn("Exception encrypting Via", e);
    } catch (BadPaddingException e) {
      if (Log.on && Log.isEnabled(Level.WARN)) Log.warn("Exception encrypting Via", e);
    } catch (IOException ie) {
      if (Log.on && Log.isEnabled(Level.WARN)) Log.warn("Exception encrypting Via ", ie);
    }
  }

  /**
   * Takes an encrypted Via header and decrypts it hidden parameter is stripped off
   *
   * @param via encrypted Via header; after the methods is returned, if the encryption was done by
   *     this proxy, the host-port part will be decrypted and hidden parameter is removed;
   *     otherwise, the via will stay unchanged
   * @return <b>true</b> if descyption was successful or not encrypted, <b>false</b> otherwise
   */
  protected boolean decryptVia(DsSipViaHeader via) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering decryptVia(" + via + ")");

    if (via.isHiddenPresent()) {
      if (Log.on && Log.isInfoEnabled()) Log.info("decrypting hidden Via");
      try {
        byte[] concealed = base64Decoder.decodeBuffer(DsByteString.toString(via.getHost()));
        DsByteString hostPort = new DsByteString(decoder.doFinal(concealed));
        int receivedIndex = hostPort.indexOf('|');
        int colonIndex = hostPort.indexOf(':');

        if (receivedIndex >= 0) {
          DsByteString received = hostPort.substring(receivedIndex + 1);
          via.setReceived(received);
          hostPort = hostPort.substring(0, receivedIndex);
        }

        if (colonIndex >= 0) {
          try {
            via.setPort(
                Integer.parseInt(DsByteString.toString(hostPort.substring(colonIndex + 1))));
          } catch (Exception e) {
            if (Log.on && Log.isEnabled(Level.ERROR))
              Log.error("Error parsing decoded Via port: " + via, e);
          }
          via.setHost(hostPort.substring(0, colonIndex));
        } else via.setHost(hostPort);

        via.setHidden(false);
        return true; // decoded successfully
      } catch (Exception e) {
        if (Log.on && Log.isEnabled(Level.ERROR)) Log.error("Exception decrypting hidden Via", e);
        return false;
      }
    } else return true;
  }

  protected boolean isLooped(DsSipRequest request) {
    return loopDetector.isLooped(request);
  }

  public DsByteString getBranchID(int n_branch, DsSipRequest request) {
    return loopDetector.getBranchID(n_branch, request);
  }

  protected boolean isLocalInterface(DsByteString host, int port, Transport transport) {
    return transportLayer.isListenInterface(host.toString(), port, transport);
  }

  public static void init(DsSipTransportLayer transport) throws DsCryptoInitException {
    try {
      viaHandler = new DsViaHandler(transport);
    } catch (Throwable e) {
      if (Log.on && Log.isEnabled(Level.ERROR)) Log.error("Exception creating ViaHandler", e);
      throw new DsCryptoInitException(e.getMessage());
    }
  }

  public static DsViaHandler getInstance() {
    if (viaHandler == null) {
      throw new NullPointerException("DsViaHandler is not initialized!");
    }
    return viaHandler;
  }

  class MyBase64Encoder {

    MyBase64Encoder() {}

    String encode(byte[] array) {
      return new String(Base64.encode(array));
    }
  }

  class MyBase64Decoder {

    MyBase64Decoder() {}

    byte[] decodeBuffer(String str) throws IOException {
      // We should probably threadlocal the char array if this thing is
      // taking a lot of time - JPS/SM
      return Base64.decode(str.toCharArray());
    }
  }
}

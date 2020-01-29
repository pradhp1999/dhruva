// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.DsLibs.DsUtil.DsHexEncoding;
import java.io.*;

public class DsTokenSipInteger {
  public static final void write16Bit(OutputStream out, int x) throws IOException {
    out.write((byte) ((x >>> 8) & 0xff));
    out.write((byte) ((x >>> 0) & 0xff));
  }

  public static final int read16Bit(MsgBytes mb) {
    int data = ((mb.msg[mb.i++] << 8) & 0xff00);
    data += ((mb.msg[mb.i++] << 0) & 0xff);
    return data;
  }

  public static final int read16Bit(byte[] data) {
    return read16Bit(new MsgBytes(data, 0, data.length));
  }

  public static final long read32Bit(MsgBytes mb) {
    long data = ((mb.msg[mb.i++] << 24) & 0xff000000);
    data += ((mb.msg[mb.i++] << 16) & 0xff0000);
    data += ((mb.msg[mb.i++] << 8) & 0xff00);
    data += ((mb.msg[mb.i++] << 0) & 0xff);
    return data;
  }

  public static final long read32Bit(byte[] data) {
    return read32Bit(new MsgBytes(data, 0, data.length));
  }

  public static final void write32Bit(OutputStream out, long x) throws IOException {
    out.write((byte) ((x >>> 24) & 0xff));
    out.write((byte) ((x >>> 16) & 0xff));
    out.write((byte) ((x >>> 8) & 0xff));
    out.write((byte) ((x >>> 0) & 0xff));
  }

  public static final void main(String args[]) throws Exception {
    int testInt = 100;

    // byte[] readData = new byte[] {(byte)0x35, (byte)0x66, (byte)0x39, (byte)0xc1};

    // long newInt = DsTokenSipInteger.read32Bit(new MsgBytes(readData, 0, 4));

    // System.out.println("Integer is "+newInt);

    // long val = 895891905;
    long val = Long.MAX_VALUE;

    System.out.println("Start with " + val);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    DsTokenSipInteger.write32Bit(bos, val);

    byte[] valBytes = bos.toByteArray();

    System.out.println("Now it is " + DsHexEncoding.toHex(valBytes));

    MsgBytes mb = new MsgBytes(valBytes, 0, valBytes.length);

    long newInt = DsTokenSipInteger.read32Bit(mb);

    System.out.println("Integer is " + newInt);

    while ((testInt < Integer.MAX_VALUE) && (testInt > Integer.MIN_VALUE)) {
      System.out.println("starting with " + testInt);

      bos = new ByteArrayOutputStream();
      DsTokenSipInteger.write32Bit(bos, testInt);

      byte[] data = bos.toByteArray();
      System.out.println("Encoded string " + DsHexEncoding.toHex(data));

      mb = new MsgBytes(data, 0, data.length);

      long newLong = DsTokenSipInteger.read32Bit(mb);

      System.out.println("Decoded string is " + newLong);
      bos.close();
      testInt = testInt * 10;
    }
  }
}

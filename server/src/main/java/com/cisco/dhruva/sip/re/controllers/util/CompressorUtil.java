/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.controllers.util;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressorUtil {

  private static final Inflater deCompresser = new Inflater();
  private static final Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);

  protected static final Trace LOG = Trace.getTrace(CompressorUtil.class.getName());

  private CompressorUtil() {}

  public static final DsByteString deCompress(DsByteString result) {
    byte[] output = new byte[result.length()];
    deCompresser.reset();
    deCompresser.setInput(result.toByteArray());
    try {
      deCompresser.inflate(output);
    } catch (DataFormatException e) {
      LOG.error("Error in decompressing", e);
      return result;
    }
    return new DsByteString(output);
  }

  public static final DsByteString compress(String result) {
    byte[] output = new byte[result.length()];
    compresser.reset();
    compresser.setInput(result.getBytes());
    compresser.finish();
    compresser.deflate(output);
    return new DsByteString(output);
  }
}

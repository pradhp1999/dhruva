// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.DsLibs.DsSipObject.ByteBuffer;
import com.cisco.dhruva.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.util.log.Trace;

public class DsTokenSDPMsgParser {
  private static final int INCLUSION_FIELD_I = 0x01;
  private static final int INCLUSION_FIELD_U = 0x02;
  private static final int INCLUSION_FIELD_E = 0x04;
  private static final int INCLUSION_FIELD_P = 0x08;
  private static final int INCLUSION_FIELD_C = 0x10;
  private static final int INCLUSION_FIELD_B = 0x20;
  private static final int INCLUSION_FIELD_T = 0x40;

  private static final int INCLUSION_FIELD_Z = 0x01;
  private static final int INCLUSION_FIELD_K = 0x40;
  private static final int INCLUSION_FIELD_A = 0x80;

  private static final int DEFAULT_TIME_T_FIELD = 0x08;
  private static final int DEFAULT_TIME_R_FIELD = 0x10;
  private static final int TIME_INCLUSION_FIELD_T = 0x40;
  private static final int TIME_INCLUSION_FIELD_R = 0x80;

  private static final int MEDIA_INCLUSION_FIELD_I = 0x01;
  private static final int MEDIA_INCLUSION_FIELD_MULT_PORTS = 0x02;
  private static final int MEDIA_INCLUSION_FIELD_DFLT_ADDR = 0x04;
  private static final int MEDIA_INCLUSION_FIELD_MULT_FORMATS = 0x08;
  private static final int MEDIA_INCLUSION_FIELD_C = 0x10;
  private static final int MEDIA_INCLUSION_FIELD_B = 0x20;
  private static final int MEDIA_INCLUSION_FIELD_K = 0x40;
  private static final int MEDIA_INCLUSION_FIELD_A = 0x80;

  public static final int DEPRECATED_DEFAULT_SDP_VERSION = 0x01;
  public static final int DEPRECATED_DEFAULT_SDP_ORIGIN_ID = 0x02;
  public static final int DEPRECATED_DEFAULT_SDP_ORIGIN_VERSION = 0x04;
  public static final int DEPRECATED_DEFAULT_SDP_TIME_START = 0x08;
  public static final int DEPRECATED_DEFAULT_SDP_TIME_STOP = 0x10;
  public static final int DEPRECATED_DEFAULT_SDP_MEDIA_PORTS = 0x20;
  public static final int DEPRECATED_DEFAULT_SDP_MEDIA_FORMATS = 0x40;
  public static final int DEPRECATED_DEFAULT_SDP_ATTRIBUTE_LINES = 0x80;

  public static final int DEFAULT_SDP_VERSION = 0x01;
  public static final int DEFAULT_SDP_ORIGIN_IP_TYPE = 0x02;
  public static final int DEFAULT_SDP_CONNECTION_IP_TYPE = 0x04;
  public static final int DEFAULT_SDP_TIME = 0x08;
  public static final int DEFAULT_SDP_ORIGIN_VERSION = 0x10;
  public static final int DEFAULT_SDP_MEDIA_PORTS = 0x20;

  public static final byte[] SUB_FIELD_SEPARATOR = (" ").getBytes();
  public static final byte[] ZERO_SUB_FIELD = ("0 ").getBytes();
  public static final byte[] TOKEN_SIP_SDP_TIME_FIELD_STOP_ZERO = ("0").getBytes();

  public static final byte[] VERSION_PREFIX = ("v=").getBytes();
  public static final byte[] ORIGIN_PREFIX = ("\r\no=").getBytes();
  public static final byte[] SESSION_PREFIX = ("\r\ns=").getBytes();
  public static final byte[] CONNECTION_PREFIX = ("\r\nc=").getBytes();
  public static final byte[] CONNECTION_NET_PREFIX = ("\r\nc=IN IP4 ").getBytes();
  public static final byte[] TIME_PREFIX = ("\r\nt=").getBytes();
  public static final byte[] MEDIA_PREFIX = ("\r\nm=").getBytes();
  public static final byte[] ATTRIBUTE_PREFIX = ("\r\na=").getBytes();
  public static final byte[] INFO_PREFIX = ("\r\ni=").getBytes();
  public static final byte[] URI_PREFIX = ("\r\nu=").getBytes();
  public static final byte[] EMAIL_PREFIX = ("\r\ne=").getBytes();
  public static final byte[] PHONE_PREFIX = ("\r\np=").getBytes();
  public static final byte[] BANDWIDTH_PREFIX = ("\r\nb=").getBytes();
  public static final byte[] REPEAT_PREFIX = ("\r\nr=").getBytes();
  public static final byte[] ZONE_PREFIX = ("\r\nz=").getBytes();
  public static final byte[] KEY_PREFIX = ("\r\nk=").getBytes();

  public static final byte[] VERSION_ZERO = ("v=0").getBytes();
  public static final byte[] TIME_ZERO = ("\r\nt=0 0").getBytes();

  public static final byte[] ORIGIN_IP_DATA = ("IN IP4 ").getBytes();
  public static final byte[] MEDIA_PORT_SEPARATOR = ("/").getBytes();
  public static final byte[] ATTR_VALUE_SEPARATOR = (":").getBytes();

  public static final byte TOKEN_SIP_CONTENT_GENERIC = 0x70;
  public static final byte TOKEN_SIP_CONTENT_SDP_DEPRECATED = 0x71;
  public static final byte TOKEN_SIP_CONTENT_SDP = 0x72;
  public static final byte TOKEN_SIP_CONTENT_SDP_TIME_DESCRIPTION = 0x73;
  public static final byte TOKEN_SIP_CONTENT_SDP_2ND_SESSION = 0x74;
  public static final byte TOKEN_SIP_CONTENT_SDP_MEDIA_DESCRIPTION = 0x75;

  // Set the logging category
  protected static Trace Log = Trace.getTrace(DsTokenSDPMsgParser.class.getName());

  static final void parse(
      DsSipMessageListener sipMsg, MsgBytes mb, DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {

    int sdpType = mb.msg[mb.i++];
    byte[] messageBody;
    switch (sdpType) {
      case TOKEN_SIP_CONTENT_SDP_DEPRECATED:
        messageBody = parseDeprecated(sipMsg, mb, messageDictionary);
        break;
      case TOKEN_SIP_CONTENT_SDP:
        messageBody = parseNew(sipMsg, mb, messageDictionary);
        break;
      default:
        throw new DsSipParserException(
            "Unsupported SDP token<" + sdpType + "> encountered while parsing");
    }

    sipMsg.messageFound(messageBody, 0, messageBody.length, true);
  }

  private static final byte[] parseDeprecated(
      DsSipMessageListener sipMsg, MsgBytes mb, DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException {
    // 2 ways to do this.  1- build the string.  2- build the object
    if (Log.isDebugEnabled()) Log.debug("Starting parse of  fixed format SDP");

    int flags = mb.msg[mb.i++];

    if (flags < 0) {
      flags = 256 + flags;
    }

    ByteBuffer buffer = ByteBuffer.newInstance();

    // get version
    if ((flags & DEPRECATED_DEFAULT_SDP_VERSION) == DEPRECATED_DEFAULT_SDP_VERSION) {
      // version is non-zero
      buffer.write(VERSION_PREFIX);
      buffer.write(messageDictionary.get(mb));
    } else {
      // version is zero
      buffer.write(VERSION_ZERO);
    }

    // add origin data
    buffer.write(ORIGIN_PREFIX);

    // add origin username
    buffer.write(messageDictionary.get(mb));
    buffer.write(SUB_FIELD_SEPARATOR);

    // add origin id
    if ((flags & DEPRECATED_DEFAULT_SDP_ORIGIN_ID) == DEPRECATED_DEFAULT_SDP_ORIGIN_ID) {
      // origin id is non-zero
      buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
      // sdpBody.append(messageDictionary.get(mb));

      buffer.write(SUB_FIELD_SEPARATOR);
    } else {
      // origin id is zero
      buffer.write(ZERO_SUB_FIELD);
    }

    // add origin version
    if ((flags & DEPRECATED_DEFAULT_SDP_ORIGIN_VERSION) == DEPRECATED_DEFAULT_SDP_ORIGIN_VERSION) {
      // version is non-zero
      buffer.write(messageDictionary.get(mb));
      buffer.write(SUB_FIELD_SEPARATOR);
    } else {
      // version is zero
      buffer.write(ZERO_SUB_FIELD);
    }
    buffer.write(ORIGIN_IP_DATA);

    // origin IP
    buffer.write(messageDictionary.get(mb));

    // session prefix
    buffer.write(SESSION_PREFIX);
    // session data
    buffer.write(messageDictionary.get(mb));

    // connection prefix
    buffer.write(CONNECTION_NET_PREFIX);
    // connection IP data
    buffer.write(messageDictionary.get(mb));

    // time prefix
    buffer.write(TIME_PREFIX);
    // start time
    if ((flags & DEPRECATED_DEFAULT_SDP_TIME_START) == DEPRECATED_DEFAULT_SDP_TIME_START) {
      // time start is non-zero
      buffer.write(messageDictionary.get(mb));
      buffer.write(SUB_FIELD_SEPARATOR);
    } else {
      // time start is zero
      buffer.write(ZERO_SUB_FIELD);
    }
    // end time
    if ((flags & DEPRECATED_DEFAULT_SDP_TIME_STOP) == DEPRECATED_DEFAULT_SDP_TIME_STOP) {
      // time stop is non-zero
      buffer.write(messageDictionary.get(mb));
    } else {
      // time stop is zero
      buffer.write(TOKEN_SIP_SDP_TIME_FIELD_STOP_ZERO);
    }

    // media prefix
    buffer.write(MEDIA_PREFIX);
    // media name
    buffer.write(messageDictionary.get(mb));
    buffer.write(SUB_FIELD_SEPARATOR);
    // media port
    buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));

    // number of media ports
    if ((flags & DEPRECATED_DEFAULT_SDP_MEDIA_PORTS) == DEPRECATED_DEFAULT_SDP_MEDIA_PORTS) {
      buffer.write(MEDIA_PORT_SEPARATOR);
      buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
    }
    buffer.write(SUB_FIELD_SEPARATOR);

    // media transport
    buffer.write(messageDictionary.get(mb));
    buffer.write(SUB_FIELD_SEPARATOR);

    // codec
    if ((flags & DEPRECATED_DEFAULT_SDP_MEDIA_FORMATS) == DEPRECATED_DEFAULT_SDP_MEDIA_FORMATS) {
      // codec is non-zero
      buffer.write(messageDictionary.get(mb));
    } else {
      // codec is zero
      buffer.write(TOKEN_SIP_SDP_TIME_FIELD_STOP_ZERO);
    }

    // sdp attributes

    if ((flags & DEPRECATED_DEFAULT_SDP_ATTRIBUTE_LINES)
        == DEPRECATED_DEFAULT_SDP_ATTRIBUTE_LINES) {
      int numAttributes = mb.msg[mb.i++];

      for (int x = 0; x < numAttributes; x++) {
        buffer.write(ATTRIBUTE_PREFIX);
        DsByteString attributeName = messageDictionary.get(mb);
        buffer.write(attributeName);
        DsByteString attributeValue = messageDictionary.get(mb);
        if ((attributeValue != null) && (attributeValue.length() > 0)) {
          buffer.write(ATTR_VALUE_SEPARATOR);
          buffer.write(attributeValue);
        }
      }
    }
    return buffer.toByteArray();
  }

  private static final byte[] parseNew(
      DsSipMessageListener sipMsg, MsgBytes mb, DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    int fieldInclusionFlags = mb.msg[mb.i++];
    int fieldDefaultFlags = mb.msg[mb.i++];

    ByteBuffer buffer = ByteBuffer.newInstance();

    // get version
    if ((fieldDefaultFlags & DEFAULT_SDP_VERSION) == DEFAULT_SDP_VERSION) {
      // version is non-zero
      buffer.write(VERSION_PREFIX);
      buffer.write(messageDictionary.get(mb));
    } else {
      // version is zero
      buffer.write(VERSION_ZERO);
    }

    // add origin data
    buffer.write(ORIGIN_PREFIX);

    // add origin username
    buffer.write(messageDictionary.get(mb));

    // origin ID
    buffer.write(SUB_FIELD_SEPARATOR);
    if (mb.msg[mb.i++] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
      buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
    } else {
      buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
    }

    buffer.write(SUB_FIELD_SEPARATOR);

    // add origin version
    if ((fieldDefaultFlags & DEFAULT_SDP_ORIGIN_VERSION) == DEFAULT_SDP_ORIGIN_VERSION) {
      if (mb.msg[mb.i++] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
      } else {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
      }
      buffer.write(SUB_FIELD_SEPARATOR);
    } else {
      buffer.write(ZERO_SUB_FIELD);
    }

    if ((fieldDefaultFlags & DEFAULT_SDP_ORIGIN_IP_TYPE) == DEFAULT_SDP_ORIGIN_IP_TYPE) {
      buffer.write(messageDictionary.get(mb));
      buffer.write(SUB_FIELD_SEPARATOR);
    } else {
      buffer.write(ORIGIN_IP_DATA);
    }

    // origin IP
    buffer.write(messageDictionary.get(mb));

    // session prefix
    buffer.write(SESSION_PREFIX);
    // session data
    buffer.write(messageDictionary.get(mb));

    if ((fieldInclusionFlags & INCLUSION_FIELD_I) == INCLUSION_FIELD_I) {
      buffer.write(INFO_PREFIX);
      buffer.write(messageDictionary.get(mb));
    }

    // todo wrong.  It's a URI encoding
    if ((fieldInclusionFlags & INCLUSION_FIELD_U) == INCLUSION_FIELD_U) {
      buffer.write(URI_PREFIX);
      buffer.write(messageDictionary.get(mb));
    }

    if ((fieldInclusionFlags & INCLUSION_FIELD_E) == INCLUSION_FIELD_E) {
      buffer.write(EMAIL_PREFIX);
      buffer.write(messageDictionary.get(mb));
    }

    if ((fieldInclusionFlags & INCLUSION_FIELD_P) == INCLUSION_FIELD_P) {
      buffer.write(PHONE_PREFIX);
      buffer.write(messageDictionary.get(mb));
    }

    if ((fieldInclusionFlags & INCLUSION_FIELD_C) == INCLUSION_FIELD_C) {
      if ((fieldDefaultFlags & DEFAULT_SDP_CONNECTION_IP_TYPE) == DEFAULT_SDP_CONNECTION_IP_TYPE) {
        buffer.write(CONNECTION_PREFIX);
        buffer.write(messageDictionary.get(mb));
        buffer.write(SUB_FIELD_SEPARATOR);
      } else {
        buffer.write(CONNECTION_NET_PREFIX);
      }
      buffer.write(messageDictionary.get(mb));
    }

    if ((fieldInclusionFlags & INCLUSION_FIELD_B) == INCLUSION_FIELD_B) {
      buffer.write(BANDWIDTH_PREFIX);
      buffer.write(messageDictionary.get(mb));
      if (mb.msg[mb.i++] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
      } else {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
      }
    }

    if ((fieldInclusionFlags & INCLUSION_FIELD_T) == INCLUSION_FIELD_T) {
      if ((fieldDefaultFlags & DEFAULT_SDP_TIME) != DEFAULT_SDP_TIME) {
        buffer.write(TIME_ZERO);
      } else {

        buffer.write(TIME_PREFIX);

        if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
        } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
        } else {
          buffer.write(messageDictionary.get(mb));
        }
      }
    }

    // time descriptions
    while ((mb.i < mb.msg.length - 1) && (mb.msg[mb.i] == TOKEN_SIP_CONTENT_SDP_TIME_DESCRIPTION)) {
      mb.i++;
      parseTimeDescription(mb, messageDictionary, buffer);
    }

    if (mb.i == mb.msg.length - 1) {
      return buffer.toByteArray();
    }

    // more session description
    if (mb.msg[mb.i] == TOKEN_SIP_CONTENT_SDP_2ND_SESSION) {
      mb.i++;
      int flags = mb.msg[mb.i++];

      if ((flags & INCLUSION_FIELD_Z) == INCLUSION_FIELD_Z) {
        int numAdjustments = mb.msg[mb.i++];

        for (int x = 0; x < numAdjustments; x++) {
          buffer.write(ZONE_PREFIX);

          if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
          } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
          } else {
            buffer.write(messageDictionary.get(mb));
          }

          buffer.write(SUB_FIELD_SEPARATOR);

          if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
          } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
          } else {
            buffer.write(messageDictionary.get(mb));
          }
        }
      }

      if ((flags & INCLUSION_FIELD_K) == INCLUSION_FIELD_K) {
        parseKeyField(mb, messageDictionary, buffer);
      }

      if ((flags & INCLUSION_FIELD_A) == INCLUSION_FIELD_A) {
        parseAttributes(mb, messageDictionary, buffer);
      }
    }

    // media descriptions
    while ((mb.i < mb.msg.length - 1)
        && (mb.msg[mb.i] == TOKEN_SIP_CONTENT_SDP_MEDIA_DESCRIPTION)) {
      mb.i++;
      parseMediaDescription(mb, messageDictionary, buffer);
    }

    return buffer.toByteArray();
  }

  private static final void parseTimeDescription(
      MsgBytes mb, DsTokenSipMessageDictionary md, ByteBuffer buffer) {
    int timeFlags = mb.msg[mb.i++];
    if ((timeFlags & TIME_INCLUSION_FIELD_T) == TIME_INCLUSION_FIELD_T) {
      if ((timeFlags & DEFAULT_TIME_T_FIELD) == DEFAULT_TIME_T_FIELD) {
        buffer.write(TIME_ZERO);
      } else {
        buffer.write(TIME_PREFIX);

        if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
        } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
        } else {
          buffer.write(md.get(mb));
        }

        buffer.write(SUB_FIELD_SEPARATOR);

        if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
        } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
          buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
        } else {
          buffer.write(md.get(mb));
        }
      }
    }
    if ((timeFlags & TIME_INCLUSION_FIELD_R) == TIME_INCLUSION_FIELD_R) {

      buffer.write(REPEAT_PREFIX);

      // repeat interval
      if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
      } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
      } else {
        buffer.write(md.get(mb));
      }
      buffer.write(SUB_FIELD_SEPARATOR);

      // duration
      if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
      } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
        buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
      } else {
        buffer.write(md.get(mb));
      }

      if ((timeFlags & DEFAULT_TIME_R_FIELD) != DEFAULT_TIME_R_FIELD) {
        int offsets = mb.msg[mb.i++];

        for (int x = 0; x < offsets; x++) {
          buffer.write(SUB_FIELD_SEPARATOR);
          if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_SHORT) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));
          } else if (mb.msg[mb.i] == DsTokenSipConstants.TOKEN_SIP_UNSIGNED_LONG) {
            buffer.write(DsByteString.valueOf(DsTokenSipInteger.read32Bit(mb)));
          } else {
            buffer.write(md.get(mb));
          }
        }
      }
    }
  }

  private static final void parseMediaDescription(
      MsgBytes mb, DsTokenSipMessageDictionary md, ByteBuffer buffer) {
    int mediaFlags = mb.msg[mb.i++];

    // media prefix
    buffer.write(MEDIA_PREFIX);
    // media name
    buffer.write(md.get(mb));
    buffer.write(SUB_FIELD_SEPARATOR);
    // media port
    buffer.write(DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb)));

    // number of media ports
    if ((mediaFlags & MEDIA_INCLUSION_FIELD_MULT_PORTS) == MEDIA_INCLUSION_FIELD_MULT_PORTS) {
      buffer.write(MEDIA_PORT_SEPARATOR);
      buffer.write(String.valueOf(mb.msg[mb.i++]).getBytes());
    }
    buffer.write(SUB_FIELD_SEPARATOR);

    // media transport
    buffer.write(md.get(mb));
    buffer.write(SUB_FIELD_SEPARATOR);

    // codec
    if ((mediaFlags & MEDIA_INCLUSION_FIELD_MULT_FORMATS) == MEDIA_INCLUSION_FIELD_MULT_FORMATS) {
      // mult codecs
      int numCodecs = mb.msg[mb.i++];
      for (int x = 0; x < numCodecs; x++) {
        buffer.write(md.get(mb));
        if (x < (numCodecs - 1)) {
          buffer.write(SUB_FIELD_SEPARATOR);
        }
      }
    } else {
      buffer.write(md.get(mb));
    }

    if ((mediaFlags & INCLUSION_FIELD_I) == INCLUSION_FIELD_I) {
      buffer.write(INFO_PREFIX);
      buffer.write(md.get(mb));
    }

    if ((mediaFlags & MEDIA_INCLUSION_FIELD_C) == MEDIA_INCLUSION_FIELD_C) {
      if ((mediaFlags & MEDIA_INCLUSION_FIELD_DFLT_ADDR) == MEDIA_INCLUSION_FIELD_DFLT_ADDR) {
        buffer.write(CONNECTION_PREFIX);
        buffer.write(md.get(mb));
        buffer.write(SUB_FIELD_SEPARATOR);
      } else {
        buffer.write(CONNECTION_NET_PREFIX);
      }
    }

    if ((mediaFlags & INCLUSION_FIELD_K) == INCLUSION_FIELD_K) {
      parseKeyField(mb, md, buffer);
    }

    if ((mediaFlags & INCLUSION_FIELD_A) == INCLUSION_FIELD_A) {
      parseAttributes(mb, md, buffer);
    }
  }

  private static final void parseKeyField(
      MsgBytes mb, DsTokenSipMessageDictionary md, ByteBuffer buffer) {
    buffer.write(KEY_PREFIX);
    buffer.write(md.get(mb));
    if (mb.msg[mb.i] != DsTokenSipConstants.TOKEN_SIP_NULL) {
      buffer.write(ATTR_VALUE_SEPARATOR);
      buffer.write(md.get(mb));
    }
  }

  private static final void parseAttributes(
      MsgBytes mb, DsTokenSipMessageDictionary md, ByteBuffer buffer) {
    int numAttributes = mb.msg[mb.i++];
    for (int x = 0; x < numAttributes; x++) {
      buffer.write(ATTRIBUTE_PREFIX);
      buffer.write(md.get(mb));
      if (mb.msg[mb.i] != DsTokenSipConstants.TOKEN_SIP_NULL) {
        buffer.write(ATTR_VALUE_SEPARATOR);
        buffer.write(md.get(mb));
      }
    }
  }
}

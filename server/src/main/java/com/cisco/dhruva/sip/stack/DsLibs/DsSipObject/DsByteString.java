/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/*
TODO:DHRUVA ,  Bringing empty class as it has parser dependencies

 */

public class DsByteString {

  public static String toStunDebugString(byte[] data) {
    boolean unknown = false;
    StringBuffer sb = new StringBuffer(256);

    if (data[0] == 0 && data[1] == 1) {
      sb.append("Binding Request\n");
    } else if (data[0] == 1 && data[1] == 1) {
      sb.append("Binding Response\n");
    } else {
      unknown = true;
      sb.append("Unknown Type\n");
    }

    for (int i = 0; i < data.length; i++) {
      if (data[i] >= 0 && data[i] <= 0xf) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(data[i] & 0x000000ff));
      sb.append(' ');

      if ((i + 1) % 8 == 0) {
        sb.append('\n');
      }
    }
    sb.append('\n');

    if (unknown) {
      return sb.toString();
    }

    int msgLen = (data[2] << 8) | (data[3] & 0x000000ff);
    sb.append("Message Length = ");
    sb.append(msgLen);
    sb.append('\n');

    int index = 20; // STUN_HEADER_LENGTH;

    while (index < data.length) {
      sb.append("Type = ");
      switch (data[index + 1]) {
        case 1:
          sb.append("MAPPED-ADDRESS\n");
          break;
        case 2:
          sb.append("RESPONSE-ADDRESS\n");
          break;
        case 3:
          sb.append("CHANGE-REQUEST\n");
          break;
        case 4:
          sb.append("SOURCE-ADDRESS\n");
          break;
        case 5:
          sb.append("CHANGED-ADDRESS\n");
          break;
        case 6:
          sb.append("USERNAME\n");
          break;
        case 7:
          sb.append("PASSWORD\n");
          break;
        case 8:
          sb.append("MESSAGE-INTEGRITY\n");
          break;
        case 9:
          sb.append("ERROR-CODE\n");
          break;
        case 10:
          sb.append("UNKNOWN-ATTRIBUTES\n");
          break;
        case 11:
          sb.append("REFLECTED-FROM\n");
          break;
        default:
          sb.append("UNKNOWN " + (int) data[index + 1] + "\n");
      }

      int len = (data[index + 2] << 8) | (data[index + 3] & 0x000000ff);
      sb.append("Length = ");
      sb.append(len);
      sb.append('\n');

      // known types for responses
      // RESPONSE-ADDRESS / SOURCE-ADDRESS / CHANGED-ADDRESS
      if (data[index + 1] == 1 || data[index + 1] == 4 || data[index + 1] == 5) {
        index += 4; // 2 for type and 2 for length
        sb.append("Family = ");
        if (data[index + 1] == 1 /*IPv4*/) {
          sb.append("IPv4");
        } else {
          sb.append("UNKNOWN");
        }
        sb.append('\n');

        int port = (data[index + 2] << 8) | (data[index + 3] & 0x000000ff);
        sb.append("Port = ");
        sb.append(port);
        sb.append('\n');

        sb.append("Address = ");
        for (int i = index + 4; i < index + 8; i++) {
          sb.append(data[i] & 0x000000ff);
          if (i != index + 7) {
            sb.append('.');
          }
        }
        sb.append('\n');

        index -= 4;
      }
      index += len + 4;
    }

    return sb.toString();
  }
}

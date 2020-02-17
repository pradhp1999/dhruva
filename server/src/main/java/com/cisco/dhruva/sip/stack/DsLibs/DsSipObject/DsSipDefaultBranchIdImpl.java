// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.net.*;
import java.security.*;
import org.apache.logging.log4j.Level;

/**
 * The stateful implementation of the DsSipBranchIdInterface that will provide for a unique branch
 * ID value. The returned branch ID value will be of the format [z9hG4bK&LTID&GT]. where the
 * &LTID&GT will be an integer value from 1 to Long.MAX_VALUE.
 *
 * <p><b>Note:</b> Passing <code>null</code> to {@link #nextBranchId(DsSipRequest)} will cause a
 * stateless key to be build as below, and passing a valid request will cause a stateful key to be
 * build.
 *
 * <p>The stateless implementation of the DsSipBranchIdInterface that will provide a unique branch
 * ID value for a stateless proxy. The returned branch ID value will start with the magic cookie and
 * then contain this proxies unique prefix, followed by a hash of either the topmost Via's magic
 * cookie branch or a combined hash of the Topmost Via, To Tag, From Tag, CSeq number and Call-ID.
 *
 * <p>From RFC 3261 in (16.11 Stateless Proxy):
 *
 * <p>
 *
 * <blockquote>
 *
 * <i> The stateless proxy MAY use any technique it likes to guarantee uniqueness of its branch IDs
 * across transactions. However, the following procedure is RECOMMENDED. The proxy examines the
 * branch ID in the topmost Via header field of the received request. If it begins with the magic
 * cookie, the first component of the branch ID of the outgoing request is computed as a hash of the
 * received branch ID. Otherwise, the first component of the branch ID is computed as a hash of the
 * topmost Via, the tag in the To header field, the tag in the From header field, the Call-ID header
 * field, the CSeq number (but not method), and the Request-URI from the received request. One of
 * these fields will always vary across two different transactions. </i>
 *
 * </blockquote>
 *
 * <p>This implementation follows the recommended method of generating stateless branch keys.
 */
public class DsSipDefaultBranchIdImpl implements DsSipBranchIdInterface {
  /*
   * From Jon Schlegel's document: (Stateful)
   *
   * Branch ID / Transaction Identifier Creation Algorithm
   *
   * On Startup:
   *
   * 1.  The network element should compute a hash of the timestamp and its IP
   * address: H(timestamp + IP)
   *
   * 3.  The network element should create a montonically increasing transaction
   * counter, "transaction_count", and initialize its value to zero.
   *
   * Branch ID Creation
   *
   * 1.  The network element should increment the "transaction_count" variable each
   * time a new transaction is initiated.
   *
   * 2.  The network element should then construct the branch ID for the topmost via
   * as:
   *
   * concat(MAGIC_COOKIE, H(timestamp + IP), transaction_count)
   *
   * Where MAGIC_COOKIE = "z9hG4bK" as defined in bis-09 of the SIP specification.
   *
   */

  private static final boolean useShortKeys;

  /** Code characters for values 0..63. */
  private static final byte[] alphabet = {
    (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H',
    (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
    (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
    (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
    (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
    (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
    (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3',
    (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '.',
    (byte) '~'
  };

  /** The ID to append, for stateful keys. */
  private static long s_lId = 0;
  /** The prefix, for either key. */
  private static byte[] m_prefix;

  /** Make sure that we append exactly 4 bytes to the key for stateless. */
  private static final int MIN_HASH = 64 * 64 * 64 + 1;

  /** Make sure that we append exactly 4 bytes to the key for stateless. */
  private static final int MAX_HASH = 64 * 64 * 64 * 64 - 1;

  static {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      hostName = "127.0.0.1";
    }

    useShortKeys =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SHORT_KEYS, DsConfigManager.PROP_SHORT_KEYS_DEFAULT);

    String ipstr = DsConfigManager.getProperty(DsConfigManager.PROP_LOCAL_IP, hostName);
    init(ipstr);
  }

  private static byte[] toBase64(long i) {
    byte[] buf = new byte[11];
    int charPos = 10;

    if (i >= 0) {
      i = -i;
    }

    while (i <= -64) {
      buf[charPos--] = alphabet[(int) (-(i % 64))];
      i /= 64;
    }
    buf[charPos] = alphabet[(int) (-i)];

    byte[] retval = new byte[11 - charPos];
    System.arraycopy(buf, charPos, retval, 0, (11 - charPos));
    return retval;
  }

  /**
   * Initializes this object with your local IP address.
   *
   * @param localIP the local IP address that the process is running on.
   */
  public static void init(String localIP) {
    long now = System.currentTimeMillis();
    byte[] cookie = DsSipConstants.BS_MAGIC_COOKIE.data();

    if (DsLog4j.messageCat.isEnabled(Level.DEBUG)) {
      DsLog4j.messageCat.log(Level.DEBUG, "now = " + now);
    }

    try {
      if (useShortKeys) {
        long hash = localIP.hashCode() * now;
        byte[] hashArray = toBase64(hash);
        m_prefix = new byte[hashArray.length + cookie.length];
        System.arraycopy(cookie, 0, m_prefix, 0, DsSipConstants.MAGIC_COOKIE_COUNT);
        System.arraycopy(
            hashArray, 0, m_prefix, DsSipConstants.MAGIC_COOKIE_COUNT, hashArray.length);

        if (DsLog4j.messageCat.isEnabled(Level.DEBUG)) {
          DsLog4j.messageCat.log(
              Level.DEBUG,
              "Short Key.  This element's unique branch id prefix is "
                  + DsByteString.newString(m_prefix));
        }
      } else {
        byte[] timeip = null;
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.update(DsByteString.getBytes(Long.toString(now)));

        byte[] ip = DsByteString.getBytes(localIP);

        if (DsLog4j.messageCat.isEnabled(Level.DEBUG)) {
          DsLog4j.messageCat.log(Level.DEBUG, "ip = " + DsByteString.newString(ip));
        }

        md5.update(ip);
        byte[] digest = md5.digest();
        timeip = encode(digest, 0, digest.length);

        m_prefix = new byte[timeip.length + cookie.length];
        System.arraycopy(cookie, 0, m_prefix, 0, DsSipConstants.MAGIC_COOKIE_COUNT);
        System.arraycopy(timeip, 0, m_prefix, DsSipConstants.MAGIC_COOKIE_COUNT, timeip.length);

        if (DsLog4j.messageCat.isEnabled(Level.DEBUG)) {
          DsLog4j.messageCat.log(
              Level.DEBUG,
              "Long Key.  This element's unique branch id prefix is "
                  + DsByteString.newString(m_prefix));
        }
      }
    } catch (Exception exc) {
      if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
        DsLog4j.messageCat.log(
            Level.ERROR, "Exception creating unique prefix for branch IDs ", exc);
      }
    }
  }

  // glommed this code from AE -- should be pretty static so I pasted here -dg
  /**
   * Encodes the bytes in the specified byte array into a new byte array, whose size is an even
   * multiple of 4.
   *
   * @param data the byte array that need to be encoded.
   * @param offset the offset of the data in the specified byte array.
   * @param length the length of the data in the specified byte array.
   * @return an encoded byte array.
   */
  public static byte[] encode(final byte[] data, final int offset, final int length) {
    byte[] out = new byte[((length + 2) / 3) * 4];

    //
    // 3 bytes encode to 4 chars.  Output is always an even
    // multiple of 4 characters.
    //

    for (int i = 0, index = 0; i < length; i += 3, index += 4) {
      boolean quad = false;
      boolean trip = false;

      int val = (0xFF & (int) data[i + offset]);
      val <<= 8;
      if ((i + 1) < length) {
        val |= (0xFF & (int) data[i + 1 + offset]);
        trip = true;
      }
      val <<= 8;
      if ((i + 2) < length) {
        val |= (0xFF & (int) data[i + 2 + offset]);
        quad = true;
      }
      out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
      val >>= 6;
      out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
      val >>= 6;
      out[index + 1] = alphabet[val & 0x3F];
      val >>= 6;
      out[index + 0] = alphabet[val & 0x3F];
    }
    return out;
  }

  /**
   * Generates a branch ID, based on the following.
   *
   * <p>If <code>request</code> is <code>null</code>, a stateful key is generated.
   *
   * <p>Returns the next unique Branch ID for the Via Header for the specified SIP Request. The
   * returned value will be the MAGIC COOKIE plus a unique prefix plus the monotonically increasing
   * integer value from 1 to Long.MAX_VALUE.
   *
   * <p>Otherwise, when <code>request</code> is a valid request, a stateless key is generated.
   *
   * <p>Returns a unique Branch ID for the Via Header for the specified SIP Request. The returned
   * value will be the MAGIC COOKIE a unique prefix, plus a postfix that will be the same for
   * retransmissions. The specified request will not be changed.
   *
   * @param request the request whose branch parameter needs to be updated. This request is just for
   *     the information and is not changed by this implementation. Pass <code>null</code> for a
   *     stateful key.
   * @return the unique stateless Branch ID for the Via Header for the supplied SIP Request.
   */
  public DsByteString nextBranchId(DsSipRequest request) {
    if (request == null) // stateful
    {
      long nextId; // = 0;
      synchronized (this) {
        nextId = s_lId++;
      }

      byte[] bytes;
      if (useShortKeys) {
        bytes = toBase64(nextId);
      } else {
        bytes = DsByteString.getBytes(Long.toString(nextId));
      }

      byte[] res = new byte[bytes.length + m_prefix.length];
      System.arraycopy(m_prefix, 0, res, 0, m_prefix.length);
      System.arraycopy(bytes, 0, res, m_prefix.length, bytes.length);
      return new DsByteString(res);
    } else // stateless
    {
      try {
        DsSipViaHeader viaHeader = request.getViaHeaderValidate();
        DsByteString origBranch = viaHeader.getBranch();
        int hash = 0;
        if (origBranch != null && origBranch.startsWith(DsSipConstants.BS_MAGIC_COOKIE)) {
          // since the previous Via has a key branch, we can use that as the first part of the hash
          hash = Math.abs(origBranch.hashCode() % MAX_HASH);

          if (hash <= MIN_HASH) {
            hash += MIN_HASH;
          }

          byte[] bytes;
          if (useShortKeys) {
            bytes = toBase64(hash);
          } else {
            bytes = DsByteString.getBytes(Long.toString(hash, 36));
          }

          byte[] res = new byte[bytes.length + origBranch.length()];
          System.arraycopy(origBranch.data(), origBranch.offset(), res, 0, origBranch.length());
          System.arraycopy(bytes, 0, res, origBranch.length(), bytes.length);
          return new DsByteString(res);
        } else {
          // the Via did not have a branch, use the key fields to make a hash
          DsByteString via = viaHeader.toByteString();
          DsByteString toTag = request.getToTag();
          if (toTag == null) {
            toTag = request.getToHeaderValidate().getTag();
            if (toTag == null) {
              toTag = DsByteString.BS_NULL;
            }
          }

          DsByteString fromTag = request.getFromHeaderValidate().getTag();
          if (fromTag == null) {
            fromTag = DsByteString.BS_NULL;
          }

          DsByteString callId = request.getCallId();
          if (callId == null) {
            callId = DsByteString.BS_NULL;
          }

          long cseq = request.getCSeqNumber();

          DsByteString requestUri = request.getURI().toByteString();
          if (requestUri == null) {
            requestUri = DsByteString.BS_NULL;
          }

          hash = (int) cseq;
          hash *=
              via.hashCode()
                  * toTag.hashCode()
                  * fromTag.hashCode()
                  * callId.hashCode()
                  * requestUri.hashCode();
          // hash now equals the first part of the key, and will be the same upon retransmit
          byte[] bytes;
          if (useShortKeys) {
            bytes = toBase64(hash);
          } else {
            bytes = DsByteString.getBytes(Long.toString(hash, 36));
          }

          byte[] res = new byte[bytes.length + m_prefix.length];
          System.arraycopy(m_prefix, 0, res, 0, m_prefix.length);
          System.arraycopy(bytes, 0, res, m_prefix.length, bytes.length);
          return new DsByteString(res);
        }
      } catch (DsException e) {
        if (DsLog4j.messageCat.isEnabled(Level.DEBUG)) {
          DsLog4j.messageCat.log(Level.DEBUG, "Exception generation stateless branch ID.", e);
        }
      }
      return DsByteString.BS_NULL;
    }
  }

  //    public final static void main(String args[]) throws Exception
  //    {
  //        System.out.println();
  //        System.out.println("prefix: " + DsByteString.newString(m_prefix));
  //        DsSipDefaultBranchIdImpl impl =  new DsSipDefaultBranchIdImpl();
  //
  //        byte[] msgBytes =
  //        (
  //            "INVITE sip:a@localhost:5555 SIP/2.0\r\n" +
  //            "From: contact <sip:contact@127.0.0.1:6666>\r\n" +
  //            "Via: SIP/2.0/UDP
  // lexus.dynamicsoft.com;branch=z9hG4bK+NHy7ixMuCVWUWWmpnyqew~~1600198614\r\n" +
  //            "To: contact <sip:a@localhost:5555>\r\n" +
  //            "Contact: <sip:contact@127.0.0.1:6666>\r\n" +
  //            "CSeq: 1 INVITE\r\n" +
  //            "Content-Type: application/sdp\r\n" +
  //            "Content-Length: 0\r\n" +
  //            "Call-ID: 973019276149@127.0.0.1\r\n" +
  //            "\r\n"
  //        ).getBytes();
  //
  //        byte[] msgBytes2 =
  //        (
  //            "INVITE sip:a@localhost:5555 SIP/2.0\r\n" +
  //            "From: contact <sip:contact@127.0.0.1:6666>\r\n" +
  //            "Via: SIP/2.0/UDP lexus.dynamicsoft.com\r\n" +
  //            "To: contact <sip:a@localhost:5555>\r\n" +
  //            "Contact: <sip:contact@127.0.0.1:6666>\r\n" +
  //            "CSeq: 1 INVITE\r\n" +
  //            "Content-Type: application/sdp\r\n" +
  //            "Content-Length: 0\r\n" +
  //            "Call-ID: 973019276149@127.0.0.1\r\n" +
  //            "\r\n"
  //        ).getBytes();
  //
  //
  //        byte[] msgBytes =
  //        (
  //            "MESSAGE sip:user@192.168.170.214:5555 SIP/2.0\r\n" +
  //            "Via: SIP/2.0/UDP 192.168.170.213:6666;branch=z9hG4bK392M\r\n" +
  //            "Max-Forwards: 70\r\n" +
  //            "To: contact <sip:im_user@192.168.170.214:5555>\r\n" +
  //            "From: contact <sip:contact@192.168.170.213:6666>\r\n" +
  //            "Contact: <sip:contact@192.168.170.213:6666>\r\n" +
  //            "Call-ID: 117#1307327@192.168.170.213\r\n" +
  //            "CSeq: 1 MESSAGE\r\n" +
  //            "Content-Length: 14\r\n" +
  //            "Content-Type: text/plain\r\n" +
  //            "\r\n" +
  //            "Are you there?"
  //        ).getBytes();
  //
  //// z9hG4bKWHqT2nmvuTEDAKmPCnFz1Q~~-dnbsu8
  //        DsByteString bs1 = new DsByteString("z9hG4bK392M");
  //        DsByteString bs2 = new DsByteString("z9hG4bK1w2M");
  //        System.out.println("hash bs 1 = " + bs1.hashCode());
  //        System.out.println("hash bs 2 = " + bs2.hashCode());
  //
  //        String s1 = "z9hG4bK392M";
  //        String s2 = "z9hG4bK1w2M";
  //        System.out.println("hash s 1 = " + s1.hashCode());
  //        System.out.println("hash s 2 = " + s2.hashCode());
  //
  //        byte[] msgBytes2 =
  //        (
  //            "MESSAGE sip:user@192.168.170.214:5555 SIP/2.0\r\n" +
  //            "Via: SIP/2.0/UDP 192.168.170.213:6666;branch=z9hG4bK1w2M\r\n" +
  //            "Max-Forwards: 70\r\n" +
  //            "To: contact <sip:im_user@192.168.170.214:5555>\r\n" +
  //            "From: contact <sip:contact@192.168.170.213:6666>\r\n" +
  //            "Contact: <sip:contact@192.168.170.213:6666>\r\n" +
  //            "Call-ID: 68#1297485@192.168.170.213\r\n" +
  //            "CSeq: 1 MESSAGE\r\n" +
  //            "Content-Length: 14\r\n" +
  //            "Content-Type: text/plain\r\n" +
  //            "\r\n" +
  //            "Are you there?"
  //        ).getBytes();
  //
  //        DsSipRequest request = (DsSipRequest)DsSipMessage.createMessage(msgBytes, true, true);
  //        DsSipRequest request2 = (DsSipRequest)DsSipMessage.createMessage(msgBytes2, true, true);
  //
  //        System.out.println("key1:   " + impl.nextBranchId(request));
  //        System.out.println("key2:   " + impl.nextBranchId(request2));
  //
  //
  //        System.out.println();
  //        System.out.println("Stateful:");
  //        System.out.println(impl.nextBranchId(null).toString());
  //        System.out.println(impl.nextBranchId(null).toString());
  //        System.out.println(impl.nextBranchId(null).toString());
  //        System.out.println();
  //    }
}

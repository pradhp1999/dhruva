// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

/*
 * This class represents the message dictionary defined in the tokenized SIP draft.
 * The message dictionary contains all of the strings from a SIP message that have no specific encoding of their own.
 *
 * A message dictionary shall be created under 2 circumstances-
 *  1. A tokenized SIP packet is received.  The dictionary is created from the encoded data at the start of the packet.
 *  2. A message is being serialized.  An empty dictionary is created and strings placed into as needed during serialization.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

public class DsTokenSipMessageDictionary implements Serializable {
  DsByteString[] messageDictionary;
  ArrayList messageDictionaryBS;
  // DsByteString[] messageDictionaryBS;
  HashMap messageDictionaryTable;
  int numEntries;
  int maxSize;
  int numEncodings = 0;

  DsTokenSipDictionary dictionary;

  private static final byte STRING_SEPARATOR = 0x00;
  private static final int DEFAULT_MAX_STRINGS = 256;

  private static final byte MIN_SHORTCUT_INDEX = (byte) 0x80;
  private static final byte MAX_SHORTCUT_INDEX = (byte) 0x9F;
  private static final int MAX_NUMBER_OF_SHORTCUTS = MAX_SHORTCUT_INDEX - MIN_SHORTCUT_INDEX;

  // Set the logging category
  protected static Trace Log = Trace.getTrace(DsTokenSipMessageDictionary.class.getName());

  /** Creates a new empty message dictionary to be populated prior to serialization. */
  public DsTokenSipMessageDictionary(DsTokenSipDictionary dictionary) {
    this(dictionary, DEFAULT_MAX_STRINGS);
  }

  public DsTokenSipMessageDictionary(DsTokenSipDictionary dictionary, int maxStrings) {
    messageDictionaryBS = new ArrayList(48);
    messageDictionaryTable = new HashMap(64);
    // messageDictionaryBS = new DsByteString[maxStrings];
    // messageDictionaryTable = new HashMap(maxStrings);
    numEntries = 0;
    maxSize = maxStrings;
    this.dictionary = dictionary;
  }

  /**
   * Creates a new message dictionary populated with the contents of the incoming SIP message
   * dictionary.
   *
   * @param mb The message bytes that comprise the SIP message.
   */
  public DsTokenSipMessageDictionary(DsTokenSipDictionary dictionary, MsgBytes mb)
      throws DsTokenSipInvalidDictionaryException {
    this.dictionary = dictionary;

    // todo No current need for this value.  When would it be useful?  TCP?
    /*int windowSize =*/ DsTokenSipInteger.read16Bit(mb);

    // entry offset is a 2byte encoded integer
    int entryOffset = ((mb.msg[mb.i] << 8) & 0xff) + ((mb.msg[mb.i + 1] << 0) & 0xff);

    // because the offset of the 1st entry marks the end of the string ptrs. AND
    // because the string ptrs. are 2 bytes long, offset/2 should be the # of entries
    numEntries = entryOffset / 2;

    // messageDictionary = new byte[numEntries][];
    messageDictionary = new DsByteString[numEntries];

    int stringStart;
    int stringEnd = 0;

    int lastStringEnd = 0;

    Crc16Hasher hasher = new Crc16Hasher();
    hasher.add("1/com.sprintpcs/1".getBytes());
    int dictionarySignature = DsTokenSipInteger.read16Bit(hasher.hash());

    for (int x = 0; x < numEntries; x++) {
      if (dictionarySignature == dictionary.signature.intValue()) {
        stringStart = 2 + 1 + DsTokenSipInteger.read16Bit(mb);
      } else {
        stringStart = 2 + 2 + 2 + 1 + DsTokenSipInteger.read16Bit(mb);
      }
      stringEnd = getNextStringSeparator(mb.msg, stringStart);
      // messageDictionary[x] = new byte[stringEnd - stringStart];
      messageDictionary[x] = new DsByteString(mb.msg, stringStart, (stringEnd - stringStart));

      // System.arraycopy(mb.msg, stringStart, messageDictionary[x], 0, stringEnd - stringStart);

      lastStringEnd = Math.max(stringEnd + 1, lastStringEnd);

      if (Log.isDebugEnabled())
        Log.debug(
            "Read "
                + x
                + " the following message string from encoded message "
                + messageDictionary[x]);
    }
    // advance mb past the string we just copied
    mb.i = lastStringEnd;
  }

  /**
   * Adds an entry to the dictionary.
   *
   * @param entry The message data to be encoded in the dictionary.
   */
  public synchronized DsTokenSipDictionaryEntry add(DsByteString entry) {
    if (numEntries < maxSize) {
      // messageDictionary[numEntries] = entry.toByteArray();
      messageDictionaryBS.add(numEntries, entry);

      DsTokenSipDictionaryEntry dictionaryEntry =
          new DsTokenSipDictionaryEntry(
              DsTokenSipConstants.TOKEN_SIP_MESSAGE_DICTIONARY,
              numEntries++,
              (numEntries > MAX_NUMBER_OF_SHORTCUTS)
                  ? DsTokenSipDictionaryEntry.NO_SHORTCUT
                  : numEntries + MIN_SHORTCUT_INDEX - 1);
      messageDictionaryTable.put(entry, dictionaryEntry);

      return dictionaryEntry;

    } else {
      throw new ArrayIndexOutOfBoundsException("Exceeded message dictionary max size");
    }
  }

  /**
   * Serializes the message dictionary for transmission.
   *
   * @param out Where to write the serialized dictionary data.
   */
  public synchronized void write(OutputStream out) throws IOException {

    if (numEntries == 0) return;

    // figure out the total number of bytes
    // Start with the headers.  2 bytes for the message size plus the size of the index array
    int totalPacketSize = numEntries * 2;

    // now add the size of the strings and the 0x00 terminator
    for (int x = 0; x < numEntries; x++) {
      totalPacketSize += ((DsByteString) messageDictionaryBS.get(x)).length() + 1;
    }

    if (dictionary.name.equals("1/com.sprintpcs/1")) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PREFIX1);
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_PREFIX2);
      DsTokenSipInteger.write16Bit(out, dictionary.signature.intValue());
      DsTokenSipInteger.write16Bit(out, 0);
    }
    DsTokenSipInteger.write16Bit(out, totalPacketSize);

    // write the first offset
    int offset = numEntries * 2;
    DsTokenSipInteger.write16Bit(out, offset);

    // write subsequent offsets
    for (int x = 1; x < numEntries; x++) {
      offset += ((DsByteString) messageDictionaryBS.get(x - 1)).length() + 1;
      // offset += messageDictionaryBS[x-1].length() + 1;

      // if (Log.isDebugEnabled()) Log.debug("the array offset of " + x + "is "+offset);
      DsTokenSipInteger.write16Bit(out, offset);
    }

    // write message strings
    for (int x = 0; x < numEntries; x++) {
      // if (Log.isDebugEnabled()) Log.debug("the message is "+(new String(messageDictionary[x])));
      ((DsByteString) messageDictionaryBS.get(x)).write(out);
      // messageDictionaryBS[x].write(out);
      out.write(STRING_SEPARATOR);
    }
  }

  // private utility method to search ahead of the initial position and finds the string termination
  // token (STRING_SEPARATOR)
  private final int getNextStringSeparator(byte[] array, int position)
      throws DsTokenSipInvalidDictionaryException {
    while (position < array.length) {
      if (array[position] == STRING_SEPARATOR) return position;
      else position++;
    }

    throw new DsTokenSipInvalidDictionaryException(
        "No string seperator found prior to end of array");
  }

  /**
   * Retrieves the next dictionary entry in the array. This method serves as a front door for
   * retrieving any dictionary entry of any kind. This method will return the dictionary entry based
   * on any of the token SIP spec valid tokens indicating a dictionary entry. The algorithm searches
   * in this order- 1. The message dictionary 2. The set of static dictionaries (local and SIP)
   *
   * @param mb The bytes that reference the dictionary entry to retrieve.
   * @return The dictionary value
   */
  public final synchronized DsByteString get(MsgBytes mb) {
    byte ch = mb.msg[mb.i];

    if (ch == DsTokenSipConstants.TOKEN_SIP_NULL) {
      mb.i++;
      return null;
    }

    DsByteString data;

    // "unsign" the byte
    int tokenAsInt = (ch < 0) ? 256 + ch : ch;

    if (tokenAsInt > 255) {
      if (Log.isEnabled(Level.WARN)) Log.warn("Dictionary tokens must be valid bytes.  It's " + ch);
      return null;
    }

    if (isShortcut(ch)) {
      data = getByShortcut(ch);

      // todo Doesn't matter if it's null now.  By seeing that it is a shortcut, we are relieved
      // from having to know a value
      // was returned.  Just advance the index.
      // if (data != null)
      // {
      mb.i++;
      // }
      return data;
    } else {
      data = getByIndex(tokenAsInt, mb.msg[mb.i + 1]);
      if (data != null) {
        mb.i += 2;
      }
      return data;
    }
  }

  /** Checks to see if the passed token is a shortcut token. */
  private static final boolean isShortcut(int token) {
    return ((token >= MIN_SHORTCUT_INDEX) && (token < 0));
  }

  /** Retrieves the dictionary entry by dictionary ID. */
  private final synchronized DsByteString getByIndex(int section, int index) {
    int tokenAsInt = (index < 0) ? 256 + index : index;

    if (section == DsTokenSipConstants.TOKEN_SIP_MESSAGE_DICTIONARY) {
      if (Log.isEnabled(Level.DEBUG)) {
        Log.debug(
            "Index token <"
                + tokenAsInt
                + "> from dictionary <"
                + section
                + "> - <"
                + messageDictionary[tokenAsInt]
                + ">");
      }
      return (messageDictionary[tokenAsInt]);
    } else {
      return (DsTokenSipMasterDictionary.getBS(dictionary, section, tokenAsInt));
    }
  }

  public final DsTokenSipDictionaryEntry getEncoding(String key) {
    return getEncoding(new DsByteString(key));
  }

  /**
   * Creates a new encoding in the message dictionary.
   *
   * @param key The data to encode.
   * @return The newly created dictionary entry.
   */
  public final DsTokenSipDictionaryEntry getEncoding(DsByteString key) {
    if ((key == null) || (key.length() == 0)) {
      return DsTokenSipDictionaryEntry.sm_EmptyTokenEntry;
    }

    DsTokenSipDictionaryEntry entry = DsTokenSipMasterDictionary.getEncoding(dictionary, key);

    if (entry == null) {

      entry = (DsTokenSipDictionaryEntry) messageDictionaryTable.get(key);
      if (entry == null) {
        // if (Log.isDebugEnabled()) Log.debug("Message number "+numEncodings+"  Dictionary encoding
        // new entry "+key);
        entry = add(key);
        numEncodings++;

      } else {
        // if (Log.isDebugEnabled()) Log.debug("Message Dictionary encoding reusing existing key
        // "+key);
      }
    }
    return entry;
  }

  /**
   * Retrieves the dictionary entry based on the shortcut index. This method serves as a front door
   * for retrieving any dictionary entry of any kind. This method will return the dictionary entry
   * based on any of the token SIP spec valid tokens indicating a dictionary entry. The algorithm
   * searches in this order- 1. The message dictionary 2. The set of static dictionaries (local and
   * SIP)
   *
   * @param token The byte that references the shortcut token.
   * @return The dictionary value
   */
  private final synchronized DsByteString getByShortcut(byte token) {
    // 80..9F is a message string.  All others are should be fetched from the master dictionary

    if (token >= MIN_SHORTCUT_INDEX && token <= MAX_SHORTCUT_INDEX) {
      // if (Log.isEnabledFor(Priority.DEBUG))
      // {
      //    Log.debug("Shortcut token <"+(int)token+"> - <"+(new
      // DsByteString(messageDictionary[token - MIN_SHORTCUT_INDEX]))+">");
      // }
      return messageDictionary[token - MIN_SHORTCUT_INDEX];
    } else {
      // if (Log.isEnabledFor(Priority.DEBUG))
      // {
      //    Log.debug("Shortcut token <"+(int)token+"> -
      // <"+DsTokenSipMasterDictionary.getByShortcut(token)+">");
      // }
      return DsTokenSipMasterDictionary.getByShortcutBS(dictionary, token);
    }
  }

  /**
   * Retrieves the method encoding from the dictionary. The algorithm searches in this order- 1. The
   * message dictionary 2. The static method dictionary
   *
   * @param mb The data to encode.
   * @return The newly created dictionary entry.
   */
  public final synchronized DsByteString getMethodShortcut(MsgBytes mb) {
    int methodIndex = mb.msg[mb.i++];
    switch (methodIndex) {
      case DsTokenSipMethodDictionary.UNKNOWN:
        if (Log.isEnabled(Level.DEBUG)) {
          Log.debug("Method shortcut <" + methodIndex + ">");
        }
        return get(mb);
      case DsTokenSipMethodDictionary.INVALID:
        if (Log.isEnabled(Level.DEBUG)) {
          Log.debug("Method shortcut <" + methodIndex + "> NO TOKEN");
        }

        return null;
      default:
        if (Log.isEnabled(Level.DEBUG)) {
          Log.debug(
              "Method shortcut <" + DsTokenSipMethodDictionary.getByShortcut(methodIndex) + ">");
        }
        return DsTokenSipMethodDictionary.getByShortcut(methodIndex);
    }
  }

  // Utility methods below.  For creating visible displays of the message dictionary for debug
  // purposes.

  final synchronized void dump(PrintStream os) {
    for (int x = 0; x < numEntries; x++) {
      os.println("Index " + x + " -" + messageDictionary[x]);
    }
  }

  /*
      Sample dictionary worksheet

      A message might look like this-


      For the following ordered strings:
      trang
      jschlegel
      63.113.41.212

      Which encodes in hex to:
      74 72 61 6e 67
      6a 73 63 68 6c 65 67 65 6c
      36 33 2e 31 31 33 2e 34 31 2e 32 31 32


      Leaves us with the following message dictionary:
      00 26
      00 06 00 0c 00 16
      74 72 61 6e 67 00
      6a 73 63 68 6c 65 67 65 6c 00
      36 33 2e 31 31 33 2e 34 31 2e 32 31 32 00

      or:
      00 25 00 06 00 0c 00 15 74 72 61 6e 67 00 6a 73 63 68 6c 65 67 65 6c 00 36 33 2e 31 31 33 2e 34 31 2e 32 31 32 00


      Where:
      00 26 - 38 bytes.  The total size of the dictionary
      00 06 - 6 bytes.  Offset to the 1st string.
      00 0c - 12 bytes.  Offset to the 2nd string.
      00 16 - 22 bytes.  Offset to the final string.


  */

  public static final void main(String[] args) throws Exception {

    /*
            byte[] testMessage = new byte[38];
            int x=0;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x25;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x06;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x0c;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x16;
            testMessage[x++] = 0x74;
            testMessage[x++] = 0x72;
            testMessage[x++] = 0x61;
            testMessage[x++] = 0x6e;
            testMessage[x++] = 0x67;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x6a;
            testMessage[x++] = 0x73;
            testMessage[x++] = 0x63;
            testMessage[x++] = 0x68;
            testMessage[x++] = 0x6c;
            testMessage[x++] = 0x65;
            testMessage[x++] = 0x67;
            testMessage[x++] = 0x65;
            testMessage[x++] = 0x6c;
            testMessage[x++] = 0x00;
            testMessage[x++] = 0x36;
            testMessage[x++] = 0x33;
            testMessage[x++] = 0x2e;
            testMessage[x++] = 0x31;
            testMessage[x++] = 0x31;
            testMessage[x++] = 0x33;
            testMessage[x++] = 0x2e;
            testMessage[x++] = 0x34;
            testMessage[x++] = 0x31;
            testMessage[x++] = 0x2e;
            testMessage[x++] = 0x32;
            testMessage[x++] = 0x31;
            testMessage[x++] = 0x32;
            testMessage[x++] = 0x00;

            System.out.println("Test message is "+new String(testMessage)+" and length is "+x);


            DsTokenSipMessageDictionary dictionary = new DsTokenSipMessageDictionary(new MsgBytes(testMessage, 0, x));
            dictionary.dump(System.out);

    */
    DsTokenSipDictionary dictionary =
        new DsTokenSipDictionary(DsByteString.newInstance("1/com.sprintpcs/1"));
    DsTokenSipMessageDictionary messageDictionary = new DsTokenSipMessageDictionary(dictionary);
    /*
        messageDictionary.add(("trang").getBytes());
        messageDictionary.add(("jschlegel").getBytes());
        messageDictionary.add(("63.113.41.212").getBytes());
        messageDictionary.add(("abcdefg").getBytes());
        messageDictionary.add(("hijklmnop").getBytes());
        messageDictionary.add(("qrstuvwxdyz").getBytes());
        messageDictionary.add(("qrstuvwxysz").getBytes());
        messageDictionary.add(("qrstuvwxyze").getBytes());
        messageDictionary.add(("aqrstuvwxyz").getBytes());
        messageDictionary.add(("qsrstuvwxyz").getBytes());
        messageDictionary.add(("qrdstuvwxyz").getBytes());
        messageDictionary.add(("qrsdtuvwxyz").getBytes());
        messageDictionary.add(("qrstduvwxyz").getBytes());
        messageDictionary.add(("qrstudvwxyz").getBytes());
        messageDictionary.add(("qrstuvdwxyz").getBytes());
        messageDictionary.add(("qrstuvwdxyz").getBytes());
        messageDictionary.add(("qasdrstuvwxyz").getBytes());
        messageDictionary.add(("qrstfsdafuvwxyz").getBytes());
        messageDictionary.add(("qrstuvwxfyz").getBytes());
        messageDictionary.add(("qrstuvwdxyz").getBytes());
        messageDictionary.add(("qrstuevwxyz").getBytes());
        messageDictionary.add(("qrstuvwwxyz").getBytes());
        messageDictionary.add(("qrstuvewxyz").getBytes());
        messageDictionary.add(("qrstuvwxgyz").getBytes());
        messageDictionary.add(("qr4stuvwxyz").getBytes());
        messageDictionary.add(("qrs4tuvwxyz").getBytes());
        messageDictionary.add((" qrst4uvwxyz ").getBytes());
        messageDictionary.add(("qrstu4vwxyz").getBytes());
        messageDictionary.add(("qrstuv4wxyz").getBytes());
        messageDictionary.add(("qrstuvw4xyz").getBytes());
        messageDictionary.add(("qrstuvwx4yz").getBytes());
        messageDictionary.add(("qrstuvwxy4z").getBytes());
        messageDictionary.add(("uqrstuvwxyz").getBytes());
        messageDictionary.add(("qurstuvwxyz").getBytes());
        messageDictionary.add(("qrustuvwxyz").getBytes());
        messageDictionary.add(("qrsutuvwxyz").getBytes());
        messageDictionary.add(("qrstuuvwxyz").getBytes());
        messageDictionary.add(("qrstuuvwxyz").getBytes());
        messageDictionary.add(("qrstuvuwxyz").getBytes());
        messageDictionary.add(("qrstuvwuxyz").getBytes());
        messageDictionary.add(("qrstuvwxuyz").getBytes());
        messageDictionary.add(("qrstuvwxyuz").getBytes());
        messageDictionary.add(("qrstu8vwxyz").getBytes());
        messageDictionary.add(("qrst8uvwxyz").getBytes());
        messageDictionary.add(("qrstuv8wxyz").getBytes());
    */
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      messageDictionary.write(bos);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // System.out.println("Dumping dictionary 1");
    // dictionary.dump(System.out);
    // dictionary.dumpBytes(System.out);

    byte[] serializedDictionary = bos.toByteArray();

    // System.out.println("the bytes are ");
    // System.out.println("<"+new DsByteString(serializedDictionary)+">");

    DsTokenSipMessageDictionary dictionary2 =
        new DsTokenSipMessageDictionary(
            dictionary, new MsgBytes(serializedDictionary, 1, serializedDictionary.length));

    // System.out.println("Dumping dictionary 2");
    dictionary2.dump(System.out);
  }
}

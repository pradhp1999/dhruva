// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.io.*;
import java.util.HashMap;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.xml.sax.*;

/**
 * This class is a wrapper for all static dictionaries defined in the tokenized SIP spec. It
 * includes-
 *
 * <p>
 *
 * <pre>
 *      SIP1
 *      SIP2
 *      Local
 * </pre>
 *
 * This class is primarily a set of managed tables which we do a wide variety of lookups on using
 * various keys. Header and Method dictionaries are hard coded. Local and SIP dictionaries are read
 * from an XML file at startup. All shortcuts are hardcoded. All dictionary IDs are hardcoded. All
 * dictionaries are static.
 */
public class DsTokenSipMasterDictionary {
  static final byte SIP_MIN_SHORTCUT_INDEX = (byte) 0xc0;
  static final byte SIP_MAX_SHORTCUT_INDEX = (byte) 0xdf;
  static final int SIP_MAX_NUMBER_OF_SHORTCUTS = SIP_MAX_SHORTCUT_INDEX - SIP_MIN_SHORTCUT_INDEX;

  static final byte LOCAL_MIN_SHORTCUT_INDEX = (byte) 0xa0;
  static final byte LOCAL_MAX_SHORTCUT_INDEX = (byte) 0xbf;
  static final int LOCAL_MAX_NUMBER_OF_SHORTCUTS =
      LOCAL_MAX_SHORTCUT_INDEX - LOCAL_MIN_SHORTCUT_INDEX;

  static final DsByteString dictionaryFileEntry = new DsByteString("entry");

  static final HashMap dictionaryNameTable = new HashMap();
  static final HashMap dictionarySignatureTable = new HashMap();

  // Set the logging category
  protected static Trace Log = Trace.getTrace(DsTokenSipMasterDictionary.class.getName());

  static {
    try {
      init();
    } catch (Exception e) {
      Log.error("TokenSIP load failed: " + e.getMessage());
    }
  }

  static final void init() throws DsTokenSipInvalidDictionaryException {

    DOMParser parser = new DOMParser();

    String fileName =
        (System.getProperty("com.dynamicsoft.DsLibs.DsSipParser.TokenSip.SipDictionary"));
    FileInputStream fis = null;

    try {
      fis = new FileInputStream((fileName == null) ? "token-sip-dictionary.xml" : fileName);

      InputSource is = new InputSource(fis);
      parser.parse(is);
      Document document = parser.getDocument();

      TreeWalker walker =
          ((DocumentTraversal) document)
              .createTreeWalker(
                  document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, false);

      Element element = (Element) walker.getRoot();

      // this is THE high level node
      if (!element.getNodeName().equals("tokensip")) {
        throw new DsTokenSipInvalidDictionaryException(
            "Dictionary definition file does not begin with tokensip Node");
      }

      element = (Element) walker.firstChild();
      // now loop thru each dictionary
      while (element != null) {
        if (!element.getNodeName().equals("dictionary")) {
          throw new DsTokenSipInvalidDictionaryException("Expected dictionary element");
        }

        String dictionaryName = element.getAttribute("name");
        if ((dictionaryName == null) || (dictionaryName.length() <= 0)) {
          throw new DsTokenSipInvalidDictionaryException("No dictionary name provided");
        }

        DsTokenSipDictionary dictionary =
            new DsTokenSipDictionary(DsByteString.newInstance(dictionaryName));

        if (dictionaryNameTable.containsKey(dictionary.name)) {
          throw new DsTokenSipInvalidDictionaryException(
              "Duplicate dictionary name found - " + dictionary.name);
        }
        if (dictionarySignatureTable.containsKey(dictionary.signature)) {
          throw new DsTokenSipInvalidDictionaryException(
              "Duplicate dictionary signature found - " + dictionary.signature);
        }
        dictionaryNameTable.put(dictionary.name, dictionary); // Add after we parse it successfully?
        dictionarySignatureTable.put(
            dictionary.signature, dictionary); // Add after we parse it successfully?

        int sectionIndex;

        element = (Element) walker.firstChild();
        // now loop thru each section
        while (element != null) {
          if (!element.getNodeName().equals("section")) {
            throw new DsTokenSipInvalidDictionaryException("Expected section element");
          }

          sectionIndex = 0;

          DsByteString[] currentSection;
          DsByteString sectionName = DsByteString.newInstance(element.getAttribute("name"));

          if (sectionName.equals("sip")) {
            currentSection = dictionary.sip1Section;
          } else if (sectionName.equals("local")) {
            currentSection = dictionary.localSection;
          } else {
            throw new DsTokenSipInvalidDictionaryException("Unsupported section- " + sectionName);
          }

          element = (Element) walker.firstChild();

          while (element != null) {
            DsByteString name = DsByteString.newInstance(element.getNodeName());

            if (name.equals(dictionaryFileEntry)) {
              if (element.getAttribute("type").compareTo("reserved") != 0) {
                // System.out.println("Entry is  <"
                // +((Text)element.getChildNodes().item(0)).getData() +"> at index
                // "+dictionaryIndex);
                currentSection[sectionIndex] =
                    new DsByteString(((Text) element.getChildNodes().item(0)).getData());
              } else {
                currentSection[sectionIndex] = DsByteString.BS_EMPTY_STRING;
              }

              sectionIndex++;
            } else {
              throw new DsTokenSipInvalidDictionaryException("Expected entry element");
            }
            element = (Element) walker.nextSibling();
          }

          if (sectionName.equals("sip")) {
            dictionary.sip_total_size = sectionIndex;
          } else if (sectionName.equals("local")) {
            dictionary.local_size = sectionIndex;
            currentSection[0] = dictionary.name;
          }

          element = (Element) walker.parentNode();
          element = (Element) walker.nextSibling();
        }

        // sip1
        for (int x = 0; x < dictionary.sip1_size; x++) {
          // populate table for serialization lookup
          dictionary.sip1SectionTable.put(
              dictionary.sip1Section[x],
              new DsTokenSipDictionaryEntry(
                  DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_1,
                  x,
                  (x < SIP_MAX_NUMBER_OF_SHORTCUTS)
                      ? (x + SIP_MIN_SHORTCUT_INDEX)
                      : DsTokenSipDictionaryEntry.NO_SHORTCUT));
          // populate array for indexed parse lookup
          dictionary.sip1SectionBytes[x] = dictionary.sip1Section[x].toByteArray();
        }

        // sip2
        for (int x = dictionary.sip1_size; x < dictionary.sip_total_size; x++) {
          // populate table for serialization lookup
          dictionary.sip1SectionTable.put(
              dictionary.sip1Section[x],
              new DsTokenSipDictionaryEntry(
                  DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_2,
                  x - dictionary.sip1_size,
                  DsTokenSipDictionaryEntry.NO_SHORTCUT));
          // populate array for indexed parse lookup
          dictionary.sip1SectionBytes[x] = dictionary.sip1Section[x].toByteArray();
        }

        for (int x = 0; x < dictionary.local_size; x++) {
          if (!dictionary.localSection[x].equals(DsByteString.BS_EMPTY_STRING)) {
            dictionary.localSectionTable.put(
                dictionary.localSection[x],
                new DsTokenSipDictionaryEntry(
                    DsTokenSipConstants.TOKEN_SIP_LOCAL_DICTIONARY,
                    x,
                    (x < LOCAL_MAX_NUMBER_OF_SHORTCUTS)
                        ? (x + LOCAL_MIN_SHORTCUT_INDEX)
                        : DsTokenSipDictionaryEntry.NO_SHORTCUT));
            dictionary.localSectionBytes[x] = dictionary.localSection[x].toByteArray();
          }
        }

        element = (Element) walker.parentNode();
        element = (Element) walker.nextSibling();
      }
    } catch (Exception e) {
      throw new DsTokenSipInvalidDictionaryException(e.getMessage());
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public static final DsTokenSipDictionary getDictionary(int dictionarySignature)
      throws DsTokenSipInvalidDictionaryException {
    DsTokenSipDictionary dictionary = null;
    if (dictionarySignature == 0) {
      dictionary = (DsTokenSipDictionary) dictionaryNameTable.get("1/com.sprintpcs/1");
    } else {
      dictionary =
          (DsTokenSipDictionary) dictionarySignatureTable.get(new Integer(dictionarySignature));
    }
    if (dictionary == null) {
      throw new DsTokenSipInvalidDictionaryException(
          "No dictionary matches signature " + dictionarySignature);
    }
    return dictionary;
  }

  public static final byte[] getByShortcut(DsTokenSipDictionary dictionary, byte token) {
    if (token < LOCAL_MIN_SHORTCUT_INDEX) {
      return null;
    } else if (token <= LOCAL_MAX_SHORTCUT_INDEX) {
      return dictionary.localSectionBytes[token - LOCAL_MIN_SHORTCUT_INDEX];
    } else if (token <= SIP_MAX_SHORTCUT_INDEX) {
      return dictionary.sip1SectionBytes[token - SIP_MIN_SHORTCUT_INDEX];
    } else {
      return null;
    }
  }

  public static final DsByteString getByShortcutBS(DsTokenSipDictionary dictionary, byte token) {
    if (token < LOCAL_MIN_SHORTCUT_INDEX) {
      return null;
    } else if (token <= LOCAL_MAX_SHORTCUT_INDEX) {
      return dictionary.localSection[token - LOCAL_MIN_SHORTCUT_INDEX];
    } else if (token <= SIP_MAX_SHORTCUT_INDEX) {
      return dictionary.sip1Section[token - SIP_MIN_SHORTCUT_INDEX];
    } else {
      return null;
    }
  }

  public static final byte[] get(DsTokenSipDictionary dictionary, int sectionID, int entry) {
    switch (sectionID) {
      case DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_1:
        return dictionary.sip1SectionBytes[entry];
        // break;
      case DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_2:
        return dictionary.sip1SectionBytes[entry + dictionary.sip1_size];
        // break;
      case DsTokenSipConstants.TOKEN_SIP_LOCAL_DICTIONARY:
        return dictionary.localSectionBytes[entry];
        // break;
      default:
        return null;
        // break;

    }
  }

  public static final DsByteString getBS(
      DsTokenSipDictionary dictionary, int sectionID, int entry) {
    switch (sectionID) {
      case DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_1:
        return dictionary.sip1Section[entry];
        // break;
      case DsTokenSipConstants.TOKEN_SIP_SIP_DICTIONARY_2:
        return dictionary.sip1Section[entry + dictionary.sip1_size];
        // break;
      case DsTokenSipConstants.TOKEN_SIP_LOCAL_DICTIONARY:
        return dictionary.localSection[entry];
        // break;
      default:
        return null;
        // break;

    }
  }

  /**
   * Retrieves the static encoding from the master dictionary.
   *
   * @param key The data to encode.
   * @return The dictionary entry, or null if no dictionary match is found.
   */
  public static final DsTokenSipDictionaryEntry getEncoding(
      DsTokenSipDictionary dictionary, DsByteString key) {
    DsTokenSipDictionaryEntry entry;

    entry = (DsTokenSipDictionaryEntry) dictionary.sip1SectionTable.get(key);

    if (entry == null) {
      entry = (DsTokenSipDictionaryEntry) dictionary.localSectionTable.get(key);
    }

    return entry;
  }

  /**
   * Retrieves the dictionary entry.
   *
   * @param index The entry to retrieve
   * @return The dictionary entry.
   */
  public static final DsByteString getLocalDictionaryEntry(
      DsTokenSipDictionary dictionary, int index) {
    if (index >= dictionary.localSection.length) {
      return null;
    }
    return dictionary.localSection[index];
  }

  /*
   * Retrieves the supported Tokenization parameter value
   */
  /*public static final DsByteString getTokenParamString(DsTokenSipDictionary dictionary)
  {
    return getLocalDictionaryEntry(dictionary, 0);
  }*/

  /*
   * Retrieves the supported Tokenization parameter value
   */
  public static final DsTokenSipDictionary getDictionary(DsByteString dictionaryName) {
    return (DsTokenSipDictionary)
        DsTokenSipMasterDictionary.dictionaryNameTable.get(dictionaryName);
  }
}

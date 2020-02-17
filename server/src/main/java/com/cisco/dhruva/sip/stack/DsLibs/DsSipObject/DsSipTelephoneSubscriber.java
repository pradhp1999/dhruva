// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * This class represents the telephone subscriber information that may be present in a SIP URL or
 * TEL URL.
 */
public class DsSipTelephoneSubscriber
    implements Serializable, Cloneable, DsSipElementListener, DsSipConstants {
  /** Represents an array of bytes that needs to be escaped while being used in a SIP URI. */
  public static final byte[] SIP_ESCAPES = {
    (byte) '"',
    (byte) '#',
    (byte) ':',
    (byte) '@',
    (byte) '[',
    (byte) '\\',
    (byte) ']',
    (byte) '^',
    (byte) '_',
    (byte) '`',
    (byte) '<',
    (byte) '>',
    (byte) '%'
  };

  /** Represents an array of bytes that needs to be escaped while being used in a TEL URI. */
  public static final byte[] TEL_ESCAPES = {
    (byte) '"',
    (byte) '#',
    (byte) ':',
    (byte) '@',
    (byte) '[',
    (byte) '\\',
    (byte) ']',
    (byte) '^',
    (byte) '_',
    (byte) '`',
    (byte) ';',
    (byte) '/',
    (byte) '?',
    (byte) '&',
    (byte) '=',
    (byte) '$',
    (byte) ',',
    (byte) '<',
    (byte) '>',
    (byte) '%'
  };

  private static final byte[] escapes = TEL_ESCAPES;

  /*
      Telephone-subscriber
      '"', '#', ':', '@', '[', '\\', ']', '^', '_', '`',
      reserved
      ';', '/', '?', ':', '@', '&', '=', '+', '$', ','
      user-unreserved
      '&', '=', '+', '$', ',', ';', '?', '/'
      unreserved
      '-', ' ', '.', '!', '', '*', ''', '(', ')', 'a'-'z', 'A'-'Z', '0'-'9'
      Delimiters
      '<', '>', '#', '%', '"'
  */
  // Data members
  private boolean m_isGlobal;
  private boolean m_isNormalized; // if set to true then serializes this URL as
  // per the bis-09 normalization, where it is
  // required while setting as user part of the
  // SIP URL.
  private DsByteString m_phoneNumber;
  private DsByteString m_isdnSubaddress;
  private DsByteString m_postDial;

  private LinkedList m_areaSpecifiers;
  private LinkedList m_serviceProviders;
  private Hashtable m_futureExtensions;

  private DsByteString m_cachedPhoneDigits;

  /** The DsByteString constant for ";isub=" string. */
  public static final DsByteString L_ISUB = new DsByteString(";isub=");
  /** The DsByteString constant for ";portd=" string. */
  public static final DsByteString L_POSTD = new DsByteString(";postd=");
  /** The DsByteString constant for ";phone-context=" string. */
  public static final DsByteString L_PHONE_CONTEXT = new DsByteString(";phone-context=");
  /** The DsByteString constant for ";tsp=" string. */
  public static final DsByteString L_TSP = new DsByteString(";tsp=");

  /**
   * Parses the specified <code>bytes</code> into a DsSipTelephoneSubscriber object as per the
   * grammar specified in the RFC 2806 for Telephone Subscriber Information. The specified <code>
   * bytes</code> should be the value part (after ':' part)of the TEL URL, although the "tel:" will
   * be stipped for you if it is present.
   *
   * @param bytes the byte array containing the telephone subscriber information that needs to be
   *     parsed.
   * @param offset the offset in the specified <code>bytes</code> byte array where from the value of
   *     the Telephone Subscriber Information starts.
   * @param count the number of bytes in the specified <code>bytes</code> byte array that comprise
   *     the Telephone Subscriber Information.
   * @return the parsed DsSipTelephoneSubscriber object.
   * @throws DsSipParserException if there is an error while parsing the specified <code>bytes
   *     </code> byte array into Telephone Subscriber Information.
   */
  public static DsSipTelephoneSubscriber parse(byte[] bytes, int offset, int count)
      throws DsSipParserException {
    DsSipTelephoneSubscriber ts = new DsSipTelephoneSubscriber();
    try {
      if ((count >= 4)
          && (bytes[offset] == (byte) 't' || bytes[offset] == (byte) 'T')
          && (bytes[offset + 1] == (byte) 'e' || bytes[offset + 1] == (byte) 'E')
          && (bytes[offset + 2] == (byte) 'l' || bytes[offset + 2] == (byte) 'L')
          && (bytes[offset + 3] == (byte) ':')) {
        // ignore the "tel:" and just pass in the date of the URL
        DsSipMsgParser.parseTelUrlData((DsSipElementListener) ts, bytes, offset + 4, count - 4);
      } else {
        DsSipMsgParser.parseTelUrlData((DsSipElementListener) ts, bytes, offset, count);
      }
    } catch (DsSipParserListenerException e) {
      throw new DsSipParserException(e);
    }
    return ts;
  }

  /**
   * Parses the specified <code>bytes</code> into a DsSipTelephoneSubscriber object as per the
   * grammar specified in the RFC 2806 for Telephone Subscriber Information. The specified <code>
   * bytes</code> should be the value part (after ':' part)of the TEL URL.
   *
   * @param bytes the byte array containing the telephone subscriber information that needs to be
   *     parsed.
   * @return the parsed DsSipTelephoneSubscriber object.
   * @throws DsSipParserException if there is an error while parsing the specified <code>bytes
   *     </code> byte array into Telephone Subscriber Information.
   */
  public static DsSipTelephoneSubscriber parse(byte[] bytes) throws DsSipParserException {
    return parse(bytes, 0, bytes.length);
  }

  /**
   * Default constructor for instantiating DsSipTelephoneSubscriber object with default Subscriber
   * information.
   */
  public DsSipTelephoneSubscriber() {}

  /**
   * Tells whether the telephone subscriber information is changed, either after parsing the
   * telephone subscriber information or after serializing this object.
   *
   * @return <code>true</code> if the telephone subscriber information is changed, <code>false
   *     </code> otherwise.
   * @deprecated always returns false, should not be used as its of no significance.
   */
  public boolean isChanged() {
    return false;
  }

  /**
   * Tells whether the phone number in this telephone subscriber information is global or not.
   *
   * @return <code>true</code> if the phone number in the telephone subscriber information is
   *     global, <code>false</code> otherwise.
   */
  public boolean isGlobal() {
    return m_isGlobal;
  }

  /**
   * Sets the phone number in this telephone subscriber information to be global or local. The phone
   * number should be set to global only if there are no local phone number specific characters
   * present in the phone number.
   *
   * @param isGlobal if <code>true</code>, then the phone number in the telephone subscriber
   *     information is set to global, set to local otherwise.
   */
  public void setGlobal(boolean isGlobal) {
    if (m_isGlobal != isGlobal) {
      m_isGlobal = isGlobal;

      // the cached phone digits are no longer valid
      m_cachedPhoneDigits = null;
    }
  }

  /**
   * Sets the phone number for this telephone subscriber information. If the specified phone number
   * <code>phoneNumber</code> starts with "+", then the <code>phoneNumber</code> is assumed global,
   * local otherwise. The specified <code>phoneNumber</code> should be a valid phone number (local
   * or global) as per the rfc 2806.
   *
   * @param phoneNumber the phone number to be set for this telephone subscriber information
   */
  public void setPhoneNumber(DsByteString phoneNumber) {
    if (phoneNumber.charAt(0) == '+') {
      m_isGlobal = true;
      m_phoneNumber = phoneNumber.substring(1);
    } else {
      m_isGlobal = false;
      m_phoneNumber = phoneNumber;
    }

    // the cached phone digits are no longer valid
    m_cachedPhoneDigits = null;
  }

  /**
   * Sets the global phone number for this telephone subscriber information. The specified phone
   * number <code>phoneNumber</code> is assumed to be global and should not start with "+". The
   * specified <code>phoneNumber</code> should be a valid global phone number as per the rfc 2806.
   *
   * @param phoneNumber the global phone number to be set for this telephone subscriber information
   */
  public void setGlobalPhoneNumber(DsByteString phoneNumber) {
    m_isGlobal = true;
    m_phoneNumber = phoneNumber;

    // the cached phone digits are no longer valid
    m_cachedPhoneDigits = null;
  }

  /**
   * Sets the local phone number for this telephone subscriber information. The specified phone
   * number <code>phoneNumber</code> is assumed to be local and should be a valid local phone number
   * as per the rfc 2806.
   *
   * @param phoneNumber the local phone number to be set for this telephone subscriber information
   */
  public void setLocalPhoneNumber(DsByteString phoneNumber) {
    m_isGlobal = false;
    m_phoneNumber = phoneNumber;

    // the cached phone digits are no longer valid
    m_cachedPhoneDigits = null;
  }

  /**
   * Returns the phone number for this telephone subscriber information. If the contained phone
   * number is global, then the returned phone number <b>will not</b> contain the leading '+'
   * character.
   *
   * @return the phone number contained in this telephone subscriber information
   */
  public DsByteString getPhoneNumber() {
    return m_phoneNumber;
  }

  /**
   * Returns the phone number for this telephone subscriber information. If the contained phone
   * number is global, then the returned phone number <b>will</b> contain the leading'+' character.
   *
   * @return the phone number contained in this telephone subscriber information. The will be a
   *     leading '+' if the phone number is global.
   */
  public DsByteString getGlobalPhoneNumber() {
    if (!m_isGlobal) {
      return m_phoneNumber;
    }

    int len = m_phoneNumber.length();
    byte[] gpnBytes = new byte[len + 1];
    gpnBytes[0] = (byte) '+';
    System.arraycopy(m_phoneNumber.data(), m_phoneNumber.offset, gpnBytes, 1, len);
    return new DsByteString(gpnBytes);
  }

  /**
   * Returns the phone digits as a string, removing the visual separators from the phone number
   * contained in this telephone subscriber information. If the contained phone number is global,
   * then the returned phone digits <b>will not</b> contain the leading '+' character.
   *
   * @return the phone digits as a string, removing the visual separators from the phone number
   *     contained in this telephone subscriber information
   */
  public DsByteString getPhoneDigits() {
    return removeSeparators(m_phoneNumber);
  }

  /**
   * This is an optimized version of getPhoneDigits that returns the digits <b>with</b> the '+' (if
   * it is a global phone number). The digits that are returned are stored in this object, so that
   * the second time this method is called, the reference is returned, rather than a new copy with
   * freshly stripped separators.
   *
   * @return the phone digits (with the leading '+', if appicable; these digits are stored in this
   *     object for later retreival
   */
  public DsByteString getGlobalPhoneDigits() {
    if (m_cachedPhoneDigits == null) {
      m_cachedPhoneDigits = removeSeparators(m_phoneNumber, isGlobal());
    }

    return m_cachedPhoneDigits;
  }

  /**
   * Tells whether the phone number contained in this telephone subscriber information is equivalent
   * to the specified phone number <code>phoneNumber</code>. The two phone numbers are compared
   * irrespective of the visual separators present in these two phone numbers.
   *
   * <p>Also if either of the two is null, <code>true</code> is returned.
   *
   * @param phoneNumber the phone number with which needs to be compared with the phone number in
   *     this telephone subscriber information.
   * @return <code>true</code> if the phone number contained in this telephone subscriber
   *     information is equivalent to the specified phone number <code>phoneNumber</code>, false
   *     otherwise.
   */
  public boolean equalsPhoneNumber(DsByteString phoneNumber) {
    if (m_phoneNumber == null && phoneNumber == null) return true;
    if (m_phoneNumber != null && phoneNumber != null) {
      return comparePhoneNumbers(m_phoneNumber, phoneNumber);
    }
    return false;
  }

  /**
   * Returns the ISDN Subaddress information contained in this telephone subscriber information if
   * present, otherwise null is returned.
   *
   * @return the ISDN Subaddress information contained in this telephone subscriber information
   */
  public DsByteString getIsdnSubaddress() {
    return m_isdnSubaddress;
  }

  /**
   * Sets the ISDN Subaddress information to be contained in this telephone subscriber information.
   * It will override the ISDN Subaddress information, if already present. The specified <code>
   * isdnSubaddress</code> should be a valid ISDN Subaddress information as per the rfc 2806.
   *
   * @param isdnSubaddress the ISDN Subaddress information to be set in this telephone subscriber
   *     information
   */
  public void setIsdnSubaddress(DsByteString isdnSubaddress) {
    m_isdnSubaddress = isdnSubaddress;
  }

  /**
   * Tells whether the ISDN Subaddress information contained in this telephone subscriber
   * information is equivalent to the specified ISDN Subaddress information <code>isdnSubaddress
   * </code>. Returns <code>true</code> if both are equivalent, <code>false</code> otherwise. Also
   * if any of the two is null, <code>true</code> is returned.
   *
   * @param isdnSubaddress the ISDN Subaddress information that needs to be compared with the ISDN
   *     Subaddress information contained in this telephone subscriber information.
   * @return <code>true</code> if the ISDN Subaddress information in this telephone subscriber
   *     information is equivalent to the specified ISDN Subaddress information <code>isdnSubaddress
   *     </code>, false otherwise.
   */
  public boolean equalsIsdnSubaddress(DsByteString isdnSubaddress) {
    if (m_isdnSubaddress == null && isdnSubaddress == null) return true;
    if (m_isdnSubaddress != null && isdnSubaddress != null) {
      return comparePhoneNumbers(m_isdnSubaddress, isdnSubaddress);
    }
    return false;
  }

  /**
   * Returns the Post Dial information contained in this telephone subscriber information if
   * present, otherwise null is returned.
   *
   * @return the Post Dial information contained in this telephone subscriber information
   */
  public DsByteString getPostDial() {
    return m_postDial;
  }

  /**
   * Sets the Post Dial information to be contained in this telephone subscriber information. It
   * will override the Post Dial information, if already present. The specified <code>postDial
   * </code> should be a valid Post Dial information as per the rfc 2806.
   *
   * @param postDial the Post Dial information to be set in this telephone subscriber information
   */
  public void setPostDial(DsByteString postDial) {
    m_postDial = postDial;
  }

  /**
   * Tells whether the Post Dial information contained in this telephone subscriber information is
   * equivalent to the specified Post Dial information <code>postDial</code>. Returns <code>true
   * </code> if both are equivalent, <code>false</code> otherwise. Also if any of the two is null,
   * <code>true</code> is returned.
   *
   * @param postDial the Post Dial information that needs to be compared with the Post Dial
   *     information contained in this telephone subscriber information.
   * @return <code>true</code> if the Post Dial information in this telephone subscriber information
   *     is equivalent to the specified Post Dial information <code>postDial</code>, false
   *     otherwise.
   */
  public boolean equalsPostDial(DsByteString postDial) {
    if (m_postDial == null && postDial == null) return true;
    if (m_postDial != null && postDial != null) {
      return comparePhoneNumbers(m_postDial, postDial);
    }
    return false;
  }

  /**
   * Returns reference to linked list containing all the area specifiers for this telephone
   * subscriber information. This list contains area specifiers as strings.
   *
   * @return area specifiers contained in this telephone subscriber information
   */
  public LinkedList getAreaSpecifiers() {
    return m_areaSpecifiers;
  }

  /**
   * Returns a string containing all the area specifiers, separated by ';' for this telephone
   * subscriber information.
   *
   * @return area specifiers, separated by ';', contained in this telephone subscriber information
   */
  public DsByteString getAreaSpecifiersAsString() {
    return getParamAsString(m_areaSpecifiers);
  }

  /**
   * Sets area specifiers for this telephone subscriber information to the specified list of area
   * specifiers. The area specifiers are specified as strings. The area specifiers present in the
   * specified list of area specifiers <code>areaSpecifiers</code> should be valid as per the rfc
   * 2806
   *
   * @param areaSpecifiers area specifiers that need to be set for this telephone subscriber
   *     information
   */
  public void setAreaSpecifiers(LinkedList areaSpecifiers) {
    m_areaSpecifiers = areaSpecifiers;
  }

  /**
   * Sets area specifiers for this telephone subscriber information to the specified list of area
   * specifiers separated by ';' in the specified string <code>areaSpecifiers</code>. The area
   * specifiers present in the specified list of area specifiers <code>areaSpecifiers</code> should
   * be valid as per the rfc 2806
   *
   * @param areaSpecifiers ';' separated string of area specifiers that need to be set for this
   *     telephone subscriber information
   */
  public void setAreaSpecifiers(DsByteString areaSpecifiers) {
    removeAreaSpecifiers();
    String as = areaSpecifiers.toString();
    StringTokenizer tokenizer = new StringTokenizer(as, ";");
    while (tokenizer.hasMoreTokens()) {
      addAreaSpecifier(new DsByteString(tokenizer.nextToken()));
    }
  }
  /**
   * Adds the specified area specifier <code>areaSpecifier</code> in the list of area specifiers
   * contained in this telephone subscriber information. The specified area specifier <code>
   * areaSpecifier</code> should be valid as per the rfc 2806.
   *
   * @param areaSpecifier the area specifier that needs to be added in the list of area specifiers
   *     contained in this telephone subscriber information.
   */
  public void addAreaSpecifier(DsByteString areaSpecifier) {
    if (m_areaSpecifiers == null) {
      m_areaSpecifiers = new LinkedList();
    }
    m_areaSpecifiers.add(areaSpecifier);
  }

  /**
   * Adds the specified area specifier <code>areaSpecifier</code> in the list of area specifiers
   * contained in this telephone subscriber information. If there are some escaped characters
   * present in the specified area specifier <code>areaSpecifier</code>, those characters are
   * unescaped before adding. The specified area specifier <code>areaSpecifier</code> should be
   * valid as per the rfc 2806.
   *
   * @param areaSpecifier the area specifier that needs to be added in the list of area specifiers
   *     contained in this telephone subscriber information.
   */
  public void addAreaSpecifierEscaped(DsByteString areaSpecifier) {
    addAreaSpecifier(unescape(areaSpecifier));
  }

  /** Removes all the area specifiers contained in this telephone subscriber information. */
  public void removeAreaSpecifiers() {
    if (m_areaSpecifiers != null) {
      m_areaSpecifiers.clear();
      m_areaSpecifiers = null;
    }
  }

  /**
   * Tells whether the area specifiers present in this telephone subscriber information are
   * equivalent to the specified area specifiers <code>areaSpecifiers</code>. If either of the two
   * lists is null, then <code>true</code> is returned.
   *
   * @param areaSpecifiers area specifiers that need to be compared for this telephone subscriber
   *     information
   * @return <code>true</code> if the area specifiers present in this telephone subscriber
   *     information are equivalent to the specified area specifiers <code>areaSpecifiers</code>,
   *     false otherwise.
   */
  public boolean equalsAreaSpecifiers(LinkedList areaSpecifiers) {
    return compareLists(m_areaSpecifiers, areaSpecifiers);
  }

  /**
   * Returns reference to linked list containing all the service providers for this telephone
   * subscriber information. This list contains service providers as strings.
   *
   * @return service providers contained in this telephone subscriber information
   */
  public LinkedList getServiceProviders() {
    return m_serviceProviders;
  }

  /**
   * Returns a string containing all the service providers, separated by ';' for this telephone
   * subscriber information.
   *
   * @return service providers, separated by ';', contained in this telephone subscriber information
   */
  public DsByteString getServiceProvidersAsString() {
    return getParamAsString(m_serviceProviders);
  }

  /**
   * Sets service providers for this telephone subscriber information to the specified list of
   * service providers. The service providers are specified as strings.
   *
   * <p>The service providers present in the specified list of service providers <code>
   * serviceProviders</code> should be valid as per the rfc 2806
   *
   * @param serviceProviders service providers that need to be set for this telephone subscriber
   *     information
   */
  public void setServiceProviders(LinkedList serviceProviders) {
    m_serviceProviders = serviceProviders;
  }

  /**
   * Sets service providers for this telephone subscriber information to the specified list of
   * service providers separated by ';' in the specified string <code>serviceProviders</code>. The
   * service providers present in the specified list of service providers <code>serviceProviders
   * </code> should be valid as per the rfc 2806
   *
   * @param serviceProviders ';' separated string of service providers that need to be set for this
   *     telephone subscriber information
   */
  public void setServiceProviders(DsByteString serviceProviders) {
    removeServiceProviders();
    String sp = serviceProviders.toString();
    StringTokenizer tokenizer = new StringTokenizer(sp, ";");
    while (tokenizer.hasMoreTokens()) {
      addServiceProvider(new DsByteString(tokenizer.nextToken()));
    }
  }

  /**
   * Adds the specified service provider <code>serviceProvider</code> in the list of service
   * providers contained in this telephone subscriber information. The specified service provider
   * <code>serviceProvider</code> should be valid as per the rfc 2806.
   *
   * @param serviceProvider the service provider that needs to be added in the list of service
   *     providers contained in this telephone subscriber information.
   */
  public void addServiceProvider(DsByteString serviceProvider) {
    if (m_serviceProviders == null) {
      m_serviceProviders = new LinkedList();
    }
    m_serviceProviders.add(serviceProvider);
  }

  /**
   * Adds the specified service provider <code>serviceProvider</code> in the list of service
   * providers contained in this telephone subscriber information. If there are some escaped
   * characters present in the specified service provider <code>serviceProvider</code>, those
   * characters are unescaped before adding. The specified service provider <code>serviceProvider
   * </code> should be valid as per the rfc 2806.
   *
   * @param serviceProvider the service provider that needs to be added in the list of service
   *     providers contained in this telephone subscriber information.
   */
  public void addServiceProviderEscaped(DsByteString serviceProvider) {
    addServiceProvider(unescape(serviceProvider));
  }

  /** Removes all the service providers contained in this telephone subscriber information. */
  public void removeServiceProviders() {
    if (m_serviceProviders != null) {
      m_serviceProviders.clear();
      m_serviceProviders = null;
    }
  }

  /**
   * Tells whether the service providers present in this telephone subscriber information are
   * equivalent to the specified service providers <code>serviceProviders</code>. If either of the
   * two lists is null, then <code>true</code> is returned.
   *
   * @param serviceProviders service providers that need to be compared for this telephone
   *     subscriber information
   * @return <code>true</code> if the service providers present in this telephone subscriber
   *     information are equivalent to the specified service providers <code>serviceProviders</code>
   *     , false otherwise.
   */
  public boolean equalsServiceProviders(LinkedList serviceProviders) {
    return compareLists(m_serviceProviders, serviceProviders);
  }

  /**
   * Returns reference to table containing name-value pairs for the future extensions information
   * contained in this telephone subscriber information. The name-value pairs are strings
   * representing the future extension name and future extension value respectively.
   *
   * @return table containing name-value pairs for the future extensions information contained in
   *     this telephone subscriber information.
   */
  public Hashtable getFutureExtensions() {
    return m_futureExtensions;
  }

  /**
   * Returns a string representation of all the future extensions name-value pairs in this telephone
   * subscriber information, where pairs are separated by ';' and the corresponding name, value
   * elements separated by '='.
   *
   * @return a string representation of all the future extensions name-value pairs in this telephone
   *     subscriber information, where pairs are separated by ';' and the corresponding name, value
   *     elements separated by '='.
   */
  public DsByteString getFutureExtensionsAsString() {
    return getParamAsString(m_futureExtensions);
  }
  /**
   * Returns future extension value for the corresponding future extension name. Return null, if no
   * such future extension name is present in this telephone subscriber information.
   *
   * @param name the future extension name for which the extension value is queried
   * @return future extension value for the corresponding future extension name.
   */
  public DsByteString getFutureExtension(DsByteString name) {
    if ((m_futureExtensions == null) || m_futureExtensions.isEmpty()) {
      return null;
    }
    return (DsByteString) m_futureExtensions.get(name);
  }

  /**
   * Sets the future extensions for this telephone subscriber information to the specified table of
   * future extensions <code>futureExtensions</code>. The specified table should contain
   * future-extension names as keys and the future-extension values as the corresponding values. The
   * future extension names and values specified in the <code>futureExtensions</code> should be
   * strings and should be valid as per the rfc 2806. This will override the existing future
   * extension, if any.
   *
   * @param futureExtensions the future extensions table containing the name-value pairs that need
   *     to be set for this telephone subscriber information.
   */
  public void setFutureExtensions(Hashtable futureExtensions) {
    m_futureExtensions = futureExtensions;
  }

  /**
   * Sets the future extensions for this telephone subscriber information to the specified
   * name-value pairs. The names are extracted from the ';' separated string specified by <code>name
   * </code> and the corresponding values are extracted from the ';' separated string specified by
   * <code>value</code>.<br>
   * For example, if the following parameters are specified<br>
   * name = "name1;name2;name3"<br>
   * value = "value1;value2;value3"<br>
   * then the future extensions specified by "name1=value1", "name2=value2" and "name3=value3"
   * name-value pairs will be added. The future extension names and values specified in the <code>
   * name</code> and <code>value</code> resp. should be valid as per the rfc 2806.
   *
   * @param name the ';' separated list of future extension names that need to be set for this
   *     telephone subscriber information.
   * @param value the ';' separated list of future extension values that need to be set for this
   *     telephone subscriber information.
   */
  public void setFutureExtensions(DsByteString name, DsByteString value) {
    removeFutureExtensions();
    StringTokenizer names = new StringTokenizer(name.toString(), ";");
    StringTokenizer values = new StringTokenizer(value.toString(), ";");
    while (names.hasMoreTokens() && values.hasMoreTokens()) {
      addFutureExtension(new DsByteString(names.nextToken()), new DsByteString(values.nextToken()));
    }
  }

  /**
   * Adds a future extension, with the future extension name specified by <code>extension</code> and
   * future extension value specified by <code>value</code>, in this telephone subscriber
   * information. The future extension name specified by <code>extension</code> and the future
   * extension value specified by <code>value</code> should be valid as per rfc 2806.
   *
   * @param extension the future extension name that needs to be added
   * @param value the future extension value that needs to be added
   */
  public void addFutureExtension(DsByteString extension, DsByteString value) {
    if (m_futureExtensions == null) {
      m_futureExtensions = new Hashtable();
    }
    m_futureExtensions.put(extension, value);
  }

  /**
   * Adds a future extension, with the future extension name specified by <code>extension</code> and
   * future extension value specified by <code>value</code>, in this telephone subscriber
   * information. If there are some escaped characters present in the future extension name
   * specified by <code>extension</code> and the future extension value specified by <code>value
   * </code> , those characters are unescaped before adding.
   *
   * <p>The future extension name specified by <code>extension</code> and the future extension value
   * specified by <code>value</code> should be valid as per rfc 2806.
   *
   * @param extension the future extension name that needs to be added
   * @param value the future extension value that needs to be added
   */
  public void addFutureExtensionEscaped(DsByteString extension, DsByteString value) {
    addFutureExtension(unescape(extension), unescape(value));
  }

  /** Removes all the future extensions contained in this telephone subscriber information. */
  public void removeFutureExtensions() {
    if (m_futureExtensions != null) {
      m_futureExtensions.clear();
      m_futureExtensions = null;
    }
  }

  /**
   * Tells whether the future extensions present in this telephone subscriber information are
   * equivalent to the specified future extensions <code>futureExtensions</code>. If either of the
   * two tables is null, then <code>true</code> is returned.
   *
   * @param futureExtensions future extensions that need to be compared for this telephone
   *     subscriber information
   * @return <code>true</code> if the future extensions present in this telephone subscriber
   *     information are equivalent to the specified future extensions <code>futureExtensions</code>
   *     , false otherwise.
   */
  public boolean equalsFutureExtensions(Hashtable futureExtensions) {
    if (m_futureExtensions == futureExtensions) return true;
    boolean result = false;
    if ((m_futureExtensions != null)
        && !m_futureExtensions.isEmpty()
        && (futureExtensions != null)
        && !futureExtensions.isEmpty()) {
      Enumeration keys = futureExtensions.keys();
      DsByteString key = null;
      DsByteString value = null;
      while (keys.hasMoreElements()) {
        key = (DsByteString) keys.nextElement();
        if (!m_futureExtensions.containsKey(key)) {
          result = false;
          break;
        }
        value = (DsByteString) futureExtensions.get(key);
        result = value.equalsIgnoreCase((DsByteString) m_futureExtensions.get(key));
        if (!result) break;
      }
    }
    return result;
  }

  /**
   * Returns the corresponding string value for the specified parameter <code>name</code>. The
   * various parameter names that can be queried for are<br>
   * <code> <pre>
   * param name           description
   *
   * isub                 ISDNSubaddress
   * postd                post dial
   * tsp                  Service provider
   * phone-context        Area Specifier
   * &LTextension-name&GT future extension name
   *
   * </pre> </code> If there are more than one parameter of same type, then all the parameters will
   * be concatenated but separated by ';'.
   *
   * @param name the name of the parameter whose parameter value(s) is/are queried
   * @return the string representation of the corresponding parameter value(s) for the specified
   *     parameter name.
   */
  public DsByteString getParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_ISUB)) {
      return getIsdnSubaddress();
    } else if (name.equalsIgnoreCase(BS_POSTD)) {
      return getPostDial();
    } else if (name.equalsIgnoreCase(BS_TSP)) {
      return getServiceProvidersAsString();
    } else if (name.equalsIgnoreCase(BS_PHONE_CONTEXT)) {
      return getAreaSpecifiersAsString();
    }
    return getFutureExtension(name);
  }

  /**
   * Sets the parameter specified by <code>name</code> with the value specified by <code>value
   * </code>. The various parameters that can be set are<br>
   * <code> <pre>
   * param name           description                     &LTvalue&GT
   *
   * isub                 ISDNSubaddress                  &LTvalue&GT
   * postd                post dial                       &LTvalue&GT
   * tsp                  Service provider                &LTvalue&GT
   * phone-context        Area Specifier                  &LTvalue&GT
   * extension-name       future extension name           &LTvalue&GT
   *
   * </pre> </code> If there are any existing value(s) for the specified parameter, those values are
   * replaced by this new specified value.
   *
   * @param name the name of the parameter whose parameter value needs to be set.
   * @param value the value of the parameter that needs to be set.
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (name == null) return;
    if (name.equalsIgnoreCase(BS_ISUB)) {
      setIsdnSubaddress(value);
    } else if (name.equalsIgnoreCase(BS_POSTD)) {
      setPostDial(value);
    } else if (name.equalsIgnoreCase(BS_TSP)) {
      setServiceProviders(value);
    } else if (name.equalsIgnoreCase(BS_PHONE_CONTEXT)) {
      setAreaSpecifiers(value);
    } else {
      setFutureExtensions(name, value);
    }
  }

  /**
   * Adds the parameter specified by <code>name</code> with the value specified by <code>value
   * </code>. The various parameters that can be added are<br>
   * <code> <pre>
   * param name           description                     &LTvalue&GT
   *
   * isub                 ISDNSubaddress                  &LTvalue&GT
   * postd                post dial                       &LTvalue&GT
   * tsp                  Service provider                &LTvalue&GT
   * phone-context        Area Specifier                  &LTvalue&GT
   * &LTextension-name&GT future extension name           &LTvalue&GT
   *
   * </pre> </code> The ISDNSubaddress and Post Dial parameters will be replaced by this new value
   * where as for other parameters this new value will be added.
   *
   * @param name the name of the parameter whose parameter value needs to be added.
   * @param value the value of the parameter that needs to be added.
   */
  public void addParameter(DsByteString name, DsByteString value) {
    if (name == null) return;
    if (name.equalsIgnoreCase(BS_ISUB)) {
      setIsdnSubaddress(value);
    } else if (name.equalsIgnoreCase(BS_POSTD)) {
      setPostDial(value);
    } else if (name.equalsIgnoreCase(BS_TSP)) {
      addServiceProvider(value);
    } else if (name.equalsIgnoreCase(BS_PHONE_CONTEXT)) {
      addAreaSpecifier(value);
    } else {
      addFutureExtension(name, value);
    }
  }

  /**
   * Tells whether there are any parameters.
   *
   * @return <code>true</code> if there are parameters, <code>false</code> otherwise.
   */
  public boolean hasParameters() {
    if (m_isdnSubaddress != null) return true;
    if (m_postDial != null) return true;
    if (m_areaSpecifiers != null && !m_areaSpecifiers.isEmpty()) return true;
    if (m_serviceProviders != null && !m_serviceProviders.isEmpty()) return true;
    if (m_futureExtensions != null && !m_futureExtensions.isEmpty()) return true;
    return false;
  }

  /**
   * Serializes the Telephone Subscriber information to the specified <code>out</code> output
   * stream. It escapes the required bytes.
   *
   * @param out the output stream to which this Telephone Subscriber information will be serialized.
   * @throws IOException if there is an I/O error while writing to the writer
   */
  public void write(OutputStream out) throws IOException {
    write(out, true);
  }

  /**
   * Serializes the Telephone Subscriber information (escaped - if <code>escape</code> is true,
   * unescaped - if <code>escape</code> is false) to the specified <code>out</code> output stream.
   *
   * @param out the output stream to which this Telephone Subscriber information will be serialized.
   * @param escape whether the information should be escaped before serializing.
   * @throws IOException if there is an I/O error while writing to the writer
   */
  public void write(OutputStream out, boolean escape) throws IOException {
    if (m_isGlobal) out.write(B_PLUS);
    if (m_phoneNumber != null) {
      m_phoneNumber.write(out);
    }
    if (m_isdnSubaddress != null) {
      L_ISUB.write(out);
      m_isdnSubaddress.write(out);
    }
    if (m_postDial != null) {
      L_POSTD.write(out);
      m_postDial.write(out);
    }
    Iterator iter = null;
    DsByteString key = null;
    DsByteString value = null;
    if ((m_areaSpecifiers != null) && !m_areaSpecifiers.isEmpty()) {
      iter = m_areaSpecifiers.iterator();
      key = L_PHONE_CONTEXT;
      while (iter.hasNext()) {
        value = (DsByteString) iter.next();
        key.write(out);
        if (escape) value = escape(value);
        value.write(out);
      }
    }
    if ((m_serviceProviders != null) && !m_serviceProviders.isEmpty()) {
      iter = m_serviceProviders.iterator();
      key = L_TSP;
      while (iter.hasNext()) {
        value = (DsByteString) iter.next();
        key.write(out);
        if (escape) value = escape(value);
        value.write(out);
      }
    }
    if ((m_futureExtensions != null) && !m_futureExtensions.isEmpty()) {
      Enumeration keys = m_futureExtensions.keys();
      byte semi = B_SEMI;
      byte equals = B_EQUAL;
      int size = m_futureExtensions.size();
      if (m_isNormalized && size > 1) {
        DsByteString[] ks = sort(keys, size);
        for (int i = 0; i < size; i++) {
          key = ks[i];
          value = (DsByteString) m_futureExtensions.get(key);
          out.write(semi);
          if (escape) {
            key = escape(key);
            value = escape(value);
          }
          key.write(out);
          out.write(equals);
          value.write(out);
        } // _for
      } // _if
      else {
        while (keys.hasMoreElements()) {
          key = (DsByteString) keys.nextElement();
          value = (DsByteString) m_futureExtensions.get(key);
          out.write(semi);
          if (escape) {
            key = escape(key);
            value = escape(value);
          }
          key.write(out);
          if (value != null && value.length() > 0) {
            out.write(equals);
            value.write(out);
          }
        } // _while
      } // _else if
    }
  }

  public void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // count the parameters first, since that is the first thing to encode
    int numParams = 0;
    if (m_isdnSubaddress != null && m_isdnSubaddress.length() > 0) {
      numParams++;
    }

    if (m_postDial != null && m_postDial.length() > 0) {
      numParams++;
    }

    if (m_areaSpecifiers != null) {
      numParams += m_areaSpecifiers.size();
    }

    if (m_serviceProviders != null) {
      numParams += m_serviceProviders.size();
    }

    if (m_futureExtensions != null) {
      numParams += m_futureExtensions.size();
    }

    if (numParams == 0) {
      // spec says to write the # of params, even if it's zero!
      out.write(0);
      return;
    }

    // Now we have to check all of the params again and encode them
    out.write(numParams);

    if (m_isdnSubaddress != null && m_isdnSubaddress.length() > 0) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
      md.getEncoding(BS_ISUB).write(out);
      md.getEncoding(m_isdnSubaddress).write(out);
    }

    if (m_postDial != null && m_postDial.length() > 0) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
      md.getEncoding(BS_POSTD).write(out);
      md.getEncoding(m_postDial).write(out);
    }

    if (m_areaSpecifiers != null) {
      ListIterator areaParams = m_areaSpecifiers.listIterator();
      while (areaParams.hasNext()) {
        out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
        md.getEncoding(BS_PHONE_CONTEXT).write(out);
        md.getEncoding((DsByteString) areaParams.next()).write(out);
      }
    }

    if (m_serviceProviders != null) {
      ListIterator tspParams = m_serviceProviders.listIterator();
      while (tspParams.hasNext()) {
        out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
        md.getEncoding(BS_TSP).write(out);
        md.getEncoding((DsByteString) tspParams.next()).write(out);
      }
    }

    if (m_futureExtensions != null) {
      Enumeration keys = m_futureExtensions.keys();
      DsByteString key = null;
      DsByteString value = null;
      while (keys.hasMoreElements()) {
        key = (DsByteString) keys.nextElement();
        value = (DsByteString) m_futureExtensions.get(key);

        out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
        md.getEncoding(key).write(out);

        if (value != null && value.length() > 0) {
          md.getEncoding(value).write(out);
        } else {
          out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
        }
      }
    }
  }

  /** Clones this telephone subscriber information and returns. */
  public Object clone() {
    DsSipTelephoneSubscriber clone = null;
    try {
      clone = (DsSipTelephoneSubscriber) super.clone();
    } catch (CloneNotSupportedException exc) {
    }

    clone.m_areaSpecifiers =
        (m_areaSpecifiers == null) ? null : (LinkedList) m_areaSpecifiers.clone();
    clone.m_serviceProviders =
        (m_serviceProviders == null) ? null : (LinkedList) m_serviceProviders.clone();
    clone.m_futureExtensions =
        (m_futureExtensions == null) ? null : (Hashtable) m_futureExtensions.clone();
    return clone;
  }

  /**
   * Tells whether this telephone subscriber information is equivalent to the specified telephone
   * subscriber information <code>object</code>. Returns <code>true</code> if both are equivalent,
   * <code>false</code> otherwise.
   *
   * @param object the object to compare.
   * @return <code>true</code> if both are equivalent, <code>false</code> otherwise.
   */
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }

    // If this object and the comparator object, have the same reference,
    // then return true, else compare all the components individually.
    if (this == object) {
      return true;
    }

    DsSipTelephoneSubscriber comparator = (DsSipTelephoneSubscriber) object;
    if (m_isGlobal != comparator.m_isGlobal) {
      return false;
    }
    if (!equalsPhoneNumber(comparator.m_phoneNumber)) {
      return false;
    }

    if (!equalsIsdnSubaddress(comparator.m_isdnSubaddress)) {
      return false;
    }
    if (!equalsPostDial(comparator.m_postDial)) {
      return false;
    }

    if (!equalsAreaSpecifiers(comparator.m_areaSpecifiers)) {
      return false;
    }
    if (!equalsServiceProviders(comparator.m_serviceProviders)) {
      return false;
    }
    if (!equalsFutureExtensions(comparator.m_futureExtensions)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the byte string representation(escaped) for this telephone subscriber information.
   *
   * @return the byte string representation(escaped) for this telephone subscriber information.
   */
  public DsByteString toByteString() {
    return toByteString(true);
  }

  /**
   * Returns the byte string representation(escaped or unescaped) for this telephone subscriber
   * information. Returns escaped representation, if the specified parameter <code>escape</code> is
   * <code>true</code>, otherwise returns unescaped string representation of this telephone
   * subscriber information
   *
   * @param escape use true to escape the string.
   * @return the byte string representation(escaped - if <code>escape</code> is true, unescaped - if
   *     <code>escape</code> is false) for this telephone subscriber information.
   */
  public DsByteString toByteString(boolean escape) {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      write(buffer, escape);
    } catch (IOException ioe) {
    }

    return buffer.getByteString();
  }

  /**
   * Returns the string representation(escaped) for this telephone subscriber information.
   *
   * @return the string representation(escaped) for this telephone subscriber information.
   */
  public String toString() {
    return toString(true);
  }

  /**
   * Returns the string representation(escaped or unescaped) for this telephone subscriber
   * information. Returns escaped representation, if the specified parameter <code>escape</code> is
   * <code>true</code>, otherwise returns unescaped string representation of this telephone
   * subscriber information
   *
   * @param escape use true to escape the string.
   * @return the string representation(escaped - if <code>escape</code> is true, unescaped - if
   *     <code>escape</code> is false) for this telephone subscriber information.
   */
  public String toString(boolean escape) {
    DsByteString bs = toByteString(escape);
    return (bs != null) ? bs.toString() : null;
  }

  /**
   * Sets whether this telephone-subscriber information should be normalized as per bis-09 draft
   * before serialization. If set to <code>true</code>, then this telephone-subscriber information
   * will get serialized as:<br>
   * <code> <pre>
   * phone-number
   * isdn-subaddress
   * post-dial
   * area-specifier(s)
   * service-provider(s)
   * lexically ordered future-extension(s)
   * </pre> </code>
   *
   * @param normalize if <code>true</code>, then this telephone-subscriber information will be
   *     serialized as specified above, otherwise it will be serialized as specified above except
   *     that the future-extension(s) may not be lexically ordered.
   */
  public void setNormalized(boolean normalize) {
    if (m_isNormalized != normalize) {
      m_isNormalized = normalize;
    }
  }

  /**
   * Tells whether this telephone-subscriber information will be normalized as per bis-09 draft
   * before serialization. If set to <code>true</code>, then this telephone-subscriber information
   * will get serialized as:<br>
   * <code> <pre>
   * phone-number
   * isdn-subaddress
   * post-dial
   * area-specifier(s)
   * service-provider(s)
   * lexically ordered future-extension(s)
   * </pre> </code>
   *
   * @return <code>true</code>, if this telephone-subscriber information will be serialized as
   *     specified above, <code>false</code> otherwise.
   */
  public boolean isNormalized() {
    return m_isNormalized;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }
    return null;
  }

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
    switch (elementId) {
      case TEL_URL_NUMBER:
        if (buffer[offset] == '+') {
          m_isGlobal = true;
          offset++;
          count--;
        }
        m_phoneNumber = new DsByteString(buffer, offset, count);

        // the cached phone digits are no longer valid
        m_cachedPhoneDigits = null;
        break;
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
    if (BS_ISUB.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_isdnSubaddress = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_POSTD.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_postDial = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_TSP.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      addServiceProvider(new DsByteString(buffer, valueOffset, valueCount));
    } else if (BS_PHONE_CONTEXT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      addAreaSpecifier(new DsByteString(buffer, valueOffset, valueCount));
    } else {
      addFutureExtension(
          new DsByteString(buffer, nameOffset, nameCount),
          new DsByteString(buffer, valueOffset, valueCount));
    }
  }

  /**
   * Determines if a given string meets the qualifications for a phone number. The phone number may
   * contain a leading + character. Aside from that, in order to be valid, it can consist of any
   * combination of the following case sensitive characters.
   *
   * <p>0 1 2 3 4 5 6 7 8 9 A B C D . - ( ) p w # *
   *
   * <p>A single instance of any other character causes this method to return false.
   *
   * @param pn the phone number to test for validity
   * @return <code>true</code> if the pn is non-null and consists only of the above mentioned
   *     characters
   */
  public static boolean isPhoneNumber(DsByteString pn) {
    if (pn == null) {
      return false;
    }

    byte[] data = pn.data();
    int offset = pn.offset();
    int length = pn.length();

    if (length == 0) {
      return false;
    }

    if (data[offset] == '+') {
      ++offset;
      --length;
    }

    for (int i = 0; i < length; i++) {
      switch (data[offset + i]) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case '.':
        case '-':
        case '(':
        case ')':
        case 'p':
        case 'w':
        case '*':
        case '#':
          break;
        default:
          return false;
      }
    }

    return true;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Private members
  ////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a string containing all the string values in the specified list, separated by ';'.
   *
   * @param list the list containing string elements that needs to be concatenated together in a
   *     single string, but separated individually by ';'
   * @return a string containing all the string values in the specified list, separated by ';'.
   */
  private DsByteString getParamAsString(LinkedList list) {
    if ((list == null) || list.isEmpty()) {
      return null;
    }
    DsByteString byteString = null;
    try (ByteBuffer buffer = ByteBuffer.newInstance()) {
      Iterator iter = list.iterator();
      boolean repeat = false;
      while (iter.hasNext()) {
        if (repeat) {
          buffer.write(B_SEMI);
        }
        buffer.write((DsByteString) iter.next());
        repeat = true;
      }
      byteString = buffer.getByteString();
    } catch (IOException ie) {

    }
    return byteString;
  }

  /**
   * Returns a string representation of all the name-value pairs in the specified table, where pairs
   * are separated by ';' and the corresponding name, value elements separated by '='.
   *
   * @param table the table containing string name-value pairs that needs to be concatenated
   *     together in a single string, where pairs are separated by ';' and the corresponding name,
   *     value elements separated by '='.
   * @return a string representation of all the name-value pairs in the specified table, where pairs
   *     are separated by ';' and the corresponding name, value elements separated by '='.
   */
  private DsByteString getParamAsString(Hashtable table) {
    if ((table == null) || table.isEmpty()) {
      return null;
    }
    DsByteString byteString = null;
    try (ByteBuffer buffer = ByteBuffer.newInstance()) {
      boolean repeat = false;
      Enumeration keys = table.keys();
      DsByteString key = null;
      while (keys.hasMoreElements()) {
        if (repeat) {
          buffer.write(B_SEMI);
        }
        key = (DsByteString) keys.nextElement();
        buffer.write(key);
        buffer.write(B_EQUAL);
        buffer.write((DsByteString) table.get(key));
        repeat = true;
      }
      byteString = buffer.getByteString();
    } catch (IOException ie) {

    }
    return byteString;
  }

  private static boolean comparePhoneNumbers(DsByteString phFirst, DsByteString phSecond) {
    return removeSeparators(phFirst).equalsIgnoreCase(removeSeparators(phSecond));
  }

  private static boolean compareLists(LinkedList listFirst, LinkedList listSecond) {
    // return true if they have the same reference
    if (listFirst == listSecond) return true;
    boolean result = false;
    if ((listFirst != null)
        && !listFirst.isEmpty()
        && (listSecond != null)
        && !listSecond.isEmpty()) {
      result = listFirst.containsAll(listSecond);
    }
    return result;
  }

  /**
   * Removes the separators from the specified phone number. The following 4 characters are
   * considered separators '-' '.' '(' ')'. Any other characters will be left in the phone number.
   *
   * @param ph the phone number that the separators will be removed from
   * @return a new string with just the phone digits
   */
  public static DsByteString removeSeparators(DsByteString ph) {
    return removeSeparators(ph, false);
  }

  /**
   * Removes the separators from the specified phone number. The following 4 characters are
   * considered separators '-' '.' '(' ')'. Any other characters will be left in the phone number.
   *
   * @param ph the phone number that the separators will be removed from, if this is <code>null
   *     </code> or 0 length, then an empty string is returned
   * @param isGlobal if this is <code>true</code> then the leading '+' will be in the retuned number
   * @return a new string with just the phone digits
   */
  public static DsByteString removeSeparators(DsByteString ph, boolean isGlobal) {
    if (ph == null) {
      return DsByteString.BS_EMPTY_STRING;
    }

    byte[] phBytes = ph.data();
    int len = ph.length();
    int off = ph.offset();
    byte[] bytes;
    byte ch;
    int j = 0;

    if (len == 0) {
      return DsByteString.BS_EMPTY_STRING;
    }

    if (isGlobal) {
      if (phBytes[off] == B_PLUS) {
        // already has a '+', will get copied below
        bytes = new byte[len];
      } else {
        // need to add the leading '+' char
        bytes = new byte[len + 1]; // +1 for leading '+' char
        bytes[j++] = B_PLUS;
      }
    } else {
      bytes = new byte[len];
    }

    for (int i = 0; i < len; i++) {
      ch = phBytes[off + i];
      if (ch != '-' && ch != '.' && ch != '(' && ch != ')') {
        bytes[j++] = ch;
      }
    }
    return new DsByteString(bytes, 0, j);
  }

  private static DsByteString escape(DsByteString value) {
    return DsSipURL.getEscapedString(value, escapes);
  }

  private static DsByteString unescape(DsByteString value) {
    DsByteString result = null;
    try {
      result = DsSipURL.getUnescapedString(value);
    } catch (Exception e) {
    }
    return result;
  }

  private static DsByteString[] sort(Enumeration keys, int len) {
    DsByteString[] ks = new DsByteString[len];
    int i = 0;
    while (keys.hasMoreElements()) {
      ks[i++] = (DsByteString) keys.nextElement();
    }
    DsByteString t = null;
    for (i = len - 2; i >= 0; i--) {
      for (int j = 0; j <= i; j++) {
        if (DsByteString.compare(ks[j + 1], ks[j]) < 0) {
          t = ks[j];
          ks[j] = ks[j + 1];
          ks[j + 1] = t;
        }
      }
    }
    return ks;
  }
}

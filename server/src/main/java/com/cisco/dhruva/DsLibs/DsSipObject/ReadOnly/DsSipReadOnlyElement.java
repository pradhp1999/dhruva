package com.cisco.dhruva.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import java.util.Arrays;

/**
 * This is the main class for this package. All user interaction happens through this class. Users
 * will need to use the static constants from the other classes to properly index the fields for a
 * given header.
 */
public final class DsSipReadOnlyElement implements DsSipConstants, DsSipHeaderListener {
  /** Integer constant for incomplete. */
  public static final byte INCOMPLETE = 0;
  /** Integer constant for complete. */
  public static final byte COMPLETE = 1;

  /** The original value of the header. */
  DsByteString value; // 12

  /** The type of the header. */
  int type; // 4
  /** <code>true</code> if the header is valid. */
  boolean valid; // 1/8
  /** The array of offsets and counts. */
  byte[] index; // ~17  (via)

  /** <code>true</code> if the header is parsed. */
  boolean parsed; // = false;

  /**
   * Constructor that takes the type and value of a header.
   *
   * @param type the header ID
   * @param value the value of the header
   */
  DsSipReadOnlyElement(int type, DsByteString value) {
    this.type = type;
    this.value = value;
  }

  /** Nulls the reference to the DsByteString value. */
  public void clear() {
    value = null;
  }

  /**
   * Resets the value of the header. Must be the same type as the original type.
   *
   * @param value the value of the header
   * @throws DsSipParserListenerException if there is an exception in the listener
   * @throws DsSipParserException if there is an exception during parsing
   */
  public void reInit(DsByteString value) throws DsSipParserListenerException, DsSipParserException {
    valid = false;
    parsed = false;
    this.value = value;
    Arrays.fill(index, (byte) 0);
    DsSipMsgParser.parseHeader(this, type, value.data(), value.offset(), value.length());
  }

  /**
   * Returns the value of the header.
   *
   * @return the value of the header.
   */
  public DsByteString getValue() {
    return value;
  }

  /**
   * Returns <code>true</code> if the header is complete.
   *
   * @return <code>true</code> if the header is complete.
   */
  public boolean isComplete() {
    return index[index.length - 1] == COMPLETE;
  }

  /**
   * Returns <code>true</code> if the header is valid.
   *
   * @return <code>true</code> if the header is valid.
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Gets an element and converts it to a float.
   *
   * @param id the ID of the element
   * @return the requested element in float form
   */
  public float getElementAsFloat(int id) {
    return getElement(id).parseFloat();
  }

  /**
   * Gets an element and converts it to a int.
   *
   * @param id the ID of the element
   * @return the requested element in int form
   */
  public int getElementAsInt(int id) {
    return getElement(id).parseInt();
  }

  /**
   * Gets an element and converts it to a long.
   *
   * @param id the ID of the element
   * @return the requested element in long form
   */
  public long getElementAsLong(int id) {
    return getElement(id).parseLong();
  }

  /**
   * Gets an element and converts it to a DsSipTransportType.
   *
   * @param id the ID of the element
   * @return the requested element as a DsSipTransportType object
   */
  public DsSipTransportType getElementAsTransport(int id) {
    return DsSipTransportType.intern(getElement(id));
  }

  /**
   * Gets an element in its String form.
   *
   * @param id the ID of the element
   * @return the requested element as a DsByteString object
   */
  public DsByteString getElement(int id) {
    if (index[id + 1] == 0) {
      return DsByteString.BS_EMPTY_STRING;
    }
    return new DsByteString(value.data(), value.offset() + index[id], index[id + 1]);
  }

  /**
   * Tells if a given element is present.
   *
   * @param id the ID of the element
   * @return <code>true</code> if the element is present, else <code>false</code>
   */
  public boolean hasElement(int id) {
    return getElement(id) != DsByteString.BS_EMPTY_STRING;
  }

  /**
   * Factory method used to create Read-Only elements.
   *
   * @param type element type
   * @param value element value
   * @return the create DsSipReadOnlyElement that contains the proper type of underlying element
   *     array
   * @throws DsSipParserListenerException if there is an exception in the listener
   * @throws DsSipParserException if there is an exception during parsing
   */
  public static DsSipReadOnlyElement createElement(int type, DsByteString value)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipReadOnlyElement h = new DsSipReadOnlyElement(type, value);
    DsSipMsgParser.parseHeader(h, type, value.data(), value.offset(), value.length());
    return h;
  }

  /**
   * Factory method used to create Read-Only elements.
   *
   * @param type element type
   * @param element element subtype (e.g. VIA, SIP_URL)
   * @param value element value
   * @return the create DsSipReadOnlyElement that contains the proper type of underlying element
   *     array
   * @throws DsSipParserListenerException if there is an exception in the listener
   * @throws DsSipParserException if there is an exception during parsing
   */
  public static DsSipReadOnlyElement createElement(int type, int element, DsByteString value)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipReadOnlyElement h = new DsSipReadOnlyElement(element, value);
    DsSipMsgParser.parseHeader(h, type, value.data(), value.offset(), value.length());
    return h;
  }

  /*
   * javadoc inherited.
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (parsed) return null;

    parsed = true;

    DsSipElementListener elements = null;
    switch (type) {
      case DsSipConstants.URI:
        index = DsSipURIElements.createIndex();
        elements = new DsSipURIElements(value.offset(), index);
        break;
      case DsSipConstants.VIA:
        index = DsSipViaElements.createIndex();
        elements = new DsSipViaElements(value.offset(), index);
        break;
      case DsSipConstants.TO:
      case DsSipConstants.FROM:
        index = DsSipToFromElements.createIndex();
        elements = new DsSipToFromElements(value.offset(), index);
        break;
      case DsSipConstants.ROUTE:
      case DsSipConstants.RECORD_ROUTE:
        index = DsSipRouteElements.createIndex();
        elements = new DsSipRouteElements(value.offset(), index);
        break;
      case DsSipConstants.ORGANIZATION:
      case DsSipConstants.PRIORITY:
      case DsSipConstants.SUBJECT:
      case DsSipConstants.CALL_ID:
        index = DsSipStringElements.createIndex();
        elements = new DsSipStringElements(value.offset(), index);
        break;
      case DsSipConstants.CSEQ:
        index = DsSipCSeqElements.createIndex();
        elements = new DsSipCSeqElements(value.offset(), index);
        break;
      case DsSipConstants.CONTACT:
        index = DsSipContactElements.createIndex();
        elements = new DsSipContactElements(value.offset(), index);
        break;
      case DsSipConstants.REMOTE_PARTY_ID:
        index = DsSipRemotePartyIdElements.createIndex();
        elements = new DsSipRemotePartyIdElements(value.offset(), index);
        break;
      case DsSipConstants.CONTENT_TYPE:
        index = DsSipContentTypeElements.createIndex();
        elements = new DsSipContentTypeElements(value.offset(), index);
        break;
        // ............more.......
    }

    return elements;
  }

  /*
   * javadoc inherited.
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    valid = isValid;
  }

  /*
   * javadoc inherited.
   */
  public void unknownFound(
      byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount, boolean valid)
      throws DsSipParserListenerException {
    // unknown not supported for read only
  }

  // for debug
  public String toString() {
    String ret = "";
    for (int i = 0; i < index.length - 1; i += 2) {
      ret += new String(value.data(), value.offset() + index[i], index[i + 1]);
      ret += ",";
    }
    if (isComplete()) {
      ret += "COMPLETE\n";
    } else {
      ret += "INCOMPLETE\n";
    }
    return ret;
  }

  /*
      public final static void main(String[] args) throws Exception
      {
          byte[] data = "SIP/2.0/UDP lexus.dynamicsoft.com:5555;maddr=12.23.32.222;received=12.233.232.222;ttl=4;hidden=true;branch=abc123".getBytes();
          System.out.println("Via");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.VIA, new DsByteString(data, 0, data.length)));

          data = "<sip:smayer@dynamicsoft.com;transport=udp>; tag = testTag ; extParamName1 = value1 ; Flag ".getBytes();
          System.out.println("ToFrom");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.TO, new DsByteString(data, 0, data.length)));

          data = "Steve Mayer <sip:smayer@dynamicsoft.com>;expires=3000;action=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("Contact");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.CONTACT, new DsByteString(data, 0, data.length)));


          data = "Steve Mayer <sip:smayer@dynamicsoft.com;transport=udp>;name1=value1;name2=value2".getBytes();
          System.out.println("Route");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.ROUTE, new DsByteString(data, 0, data.length)));

          data = "Steve Mayer <sip:smayer@dynamicsoft.com:4545;transport=udp>;name1=value1;name2=value2".getBytes();
          System.out.println("RecordRoute");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.RECORD_ROUTE, new DsByteString(data, 0, data.length)));

          data = "application/sdp".getBytes();
          System.out.println("ContentType");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.CONTENT_TYPE, new DsByteString(data, 0, data.length)));

          data = "0wou9er09@dynamicsoft.com".getBytes();
          System.out.println("CallId");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.CALL_ID, new DsByteString(data, 0, data.length)));

          data = "1212 INVITE".getBytes();
          System.out.println("CSeq");
          System.out.println(new String(data));
          System.out.println(createElement(DsSipConstants.CSEQ, new DsByteString(data, 0, data.length)));

          data = "Steve Mayer <sip:smayer@dynamicsoft.com;transport=UDP;ttl=0>;expires=3000;action=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("Contact");
          System.out.println(new String(data));
          DsSipReadOnlyElement h = createElement(DsSipConstants.CONTACT, new DsByteString(data, 0, data.length));
          System.out.println(h);
          System.out.println("contact q value  = " + h.getElementAsFloat(DsSipContactElements.QVALUE));
          System.out.println("contact trasport = " + h.getElementAsTransport(DsSipContactElements.TRANSPORT));
          System.out.println("contact ttl exists   = " + h.hasElement(DsSipContactElements.TTL));
          System.out.println("contact ttl      = " + h.getElementAsLong(DsSipContactElements.TTL));


          data = "Steve Mayer <sip:smayer@dynamicsoft.com;maddr=foo;transport=UDP;ttl=0>;expires=3000;action=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("Contact URL");
          System.out.println(new String(data));
          h = createElement(DsSipConstants.CONTACT, DsSipConstants.URI, new DsByteString(data, 0, data.length));
          //System.out.println(h);
          System.out.println("contact trasport = " + h.getElementAsTransport(DsSipURIElements.TRANSPORT));
          System.out.println("contact uri value = " + h.getElement(DsSipURIElements.VALUE));
          System.out.println("contact maddr value = " + h.getElement(DsSipURIElements.MADDR));



          data = "Steve Mayer <sip:smayer@dynamicsoft.com;transport=UDP>;expires=3000;action=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("Contact");
          System.out.println(new String(data));
          h = createElement(DsSipConstants.CONTACT, new DsByteString(data, 0, data.length));
          System.out.println("contact ttl exists   = " + h.hasElement(DsSipContactElements.TTL));

          data = "Dan Gardner <sip:dgardner@dynamicsoft.com;transport=UDP>;screen=3000;=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("");
          System.out.println(new String(data));
          h = createElement(DsSipConstants.REMOTE_PARTY_ID, new DsByteString(data, 0, data.length));
          System.out.println(h);

          data = "Dan Gardner <tel:+129299>;screen=3000;=proxy;q=0.5;extParamName1=value1".getBytes();
          System.out.println("");
          System.out.println(new String(data));
          h = createElement(DsSipConstants.REMOTE_PARTY_ID, new DsByteString(data, 0, data.length));
          System.out.println(h);
      }
  */
}

// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import org.slf4j.event.Level;

/**
 * This is the default message factory. This class creates DsSipMessages and their keys and does
 * validation using a _3way event splitter from the parser. After the message and key are created it
 * assigns the key to the message.
 *
 * <p>This class isn't instantiated directly. It's used like this:
 *
 * <p><code> DsSipMesage msg =
 * DsSipDefaultMessageFactory.getIntance().createMessage(bytes);
 * </code>
 */
public final class DsSipDefaultMessageFactory implements DsSipMessageFactory {
  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public DsSipMessage createMessage(byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException {
    return createMessage(bytes, 0, bytes.length, false, false);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  public DsSipMessage createMessage(DsSipFrameStream in)
      throws DsSipParserListenerException, DsSipParserException, IOException {
    byte[] msg = in.readMsg();
    return createMessage(msg, 0, msg.length, false, false);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  public DsSipMessage createMessage(DsSipFrameStream in, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException, IOException {
    byte[] msg = in.readMsg();
    return createMessage(msg, 0, msg.length, createKey, validate);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public DsSipMessage createMessage(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    return createMessage(bytes, offset, count, false, false);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public DsSipMessage createMessage(
      byte[] bytes, int offset, int count, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException {
    Logger keyCat = DsLog4j.transKeyCat;

    // create a new key
    DsSipTransactionKey key = null;
    if (createKey) {
      if (DsSipMessage.NEW_KEY) {
        key = new DsSipDefaultTransactionKey();
      } else {
        key = new DsSipClassicTransactionKey();
      }
    }
    _3way.reInitF2((DsSipMessageListenerFactory) key);

    _3way.reInitF3(validate ? m_validator : null);
    DsSipMessage message = null;
    try {

      DsSipMsgParser.parse(_3way, bytes, offset, count);
      message = m_msgFactory.getMessage();
    } catch (DsSipMessageValidationException mve) {
      message = m_msgFactory.getMessage();
      message.setKey(key);
      mve.setSipMessage(message);
      throw mve;
    } catch (DsSipKeyValidationException kve) {
      message = m_msgFactory.getMessage();

      // lets try the old style key here
      if (DsSipMessage.NEW_KEY) {
        if (keyCat.isEnabled(Level.INFO)) {
          keyCat.info("Default Key creation failed, Trying Classic Key");
        }

        if (message.isEncoded()) {
          if (keyCat.isEnabled(Level.INFO)) {
            keyCat.info(
                "The incoming message is Tokenized SIP encoded. Decoding it and trying Classic Key");
          }

          ByteBuffer mesageBuffer = ByteBuffer.newInstance();
          try {
            // get tokenized SIP message bytes
            message.write(mesageBuffer);
          } catch (IOException e) {
            throw new DsSipKeyValidationException(
                "Exception while transforming tokenSIP message to standard SIP for key generation");
          }
          bytes = mesageBuffer.toByteArray();
          offset = 0;
          count = bytes.length;
        }
        DsSipClassicTransactionKey oldKey = new DsSipClassicTransactionKey();
        try {
          DsSipMsgParser.parse(oldKey, bytes, offset, count);
          key = oldKey;
          if (m_validator != null) {
            m_validator.validate();
          }
        } catch (DsSipKeyValidationException okve) {
          message.setKey(oldKey);
          okve.setSipMessage(message);
          throw okve;
        } catch (DsSipMessageValidationException mve) {
          message.setKey(key);
          mve.setSipMessage(message);
          throw mve;
        }
      } else {
        // CAFFEINE 2.0 DEVELOPMENT
        // set the SIP message before throwing the exception, so that
        // the logic down the stream will have the message to access
        // necessary information (Or, a NullPointerException will be
        // thrown when this exception is catched where there's some
        // logic trying to send 400 response in the catch block.
        kve.setSipMessage(message);
        throw kve;
      }
    }
    // set the key for message
    message.setKey(key);
    _3way.clear();
    return message;
  }

  /**
   * Returns a thread local instance of DsSipMessageFactory.
   *
   * @return a thread local instance of DsSipMessageFactory.
   */
  public static DsSipMessageFactory getInstance() {
    return (DsSipMessageFactory) tlInstance.get();
  }

  /**
   * Sets the factory that is used to create new message listeners. By default, an instance of
   * DsSipDefaultMessageListenerFactory is used. It returns new instances of appropriate subclasses
   * of DsSipRequests.
   *
   * @param factory the message listener creation factory
   */
  public void setMessageListenerFactory(DsSipDefaultMessageListenerFactory factory) {
    m_msgFactory = factory;
    _3way = new DsSipParserEvent3Way(m_msgFactory, null, m_validator);
  }

  /**
   * Gets the factory that is used to create new message listeners.
   *
   * @return the message listener creation factory
   */
  public DsSipDefaultMessageListenerFactory getMessageListenerFactory() {
    return m_msgFactory;
  }

  private DsSipDefaultMessageFactory() {
    // the default message listener factory is f1, the key is f2,  and the validator is f3
    //    in this order since, exceptions will get thrown
    //
    //    1st if we can't represent the message
    //    2nd if we can't create a key for it
    //    3rd if we don't think it's valid
    //
    m_validator = new DsSipMessageValidator();
    m_msgFactory = new DsSipDefaultMessageListenerFactory();
    _3way = new DsSipParserEvent3Way(m_msgFactory, null, m_validator);
  }

  private DsSipParserEvent3Way _3way;
  private DsSipMessageValidator m_validator;
  private DsSipDefaultMessageListenerFactory m_msgFactory;

  private static DefaultMessageFactoryInit tlInstance = new DefaultMessageFactoryInit();

  static class DefaultMessageFactoryInit extends ThreadLocal {
    protected Object initialValue() {
      return new DsSipDefaultMessageFactory();
    }
  }
}

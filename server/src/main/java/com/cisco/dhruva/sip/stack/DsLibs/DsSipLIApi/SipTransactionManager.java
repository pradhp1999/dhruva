package com.cisco.dhruva.sip.stack.DsLibs.DsSipLIApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.DsLibs.DsUtil.DsException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;


/**
 * A single instance of this class manages all the transactions for a
 * given lower layer. This class is modelled after the Singleton
 * design pattern to insure only one instance exists in the
 * application. The transaction manager takes a transport layer as
 * input which allows it to receive messages that arrive on the
 * network. The transaction manager is responsible for mapping those
 * messages to the transaction they are associated with. If a SIP
 * request arrives that does not correspond to an existing
 * transaction, the transaction manager creates a server transaction
 * and passes it to the SIP method specific interface if it has been
 * set with the setRequestInterface method function. If none has been
 * set the request interface supplied in the constructor is
 * invoked. If none was present in the constructor, the transaction
 * manager automatically responds to the request with a 405 Method Not
 * Allowed.
 *
 **/


public class SipTransactionManager {

  protected static SipTransactionManager smp_theSingleton = null;

  private Logger LOG = DhruvaLoggerFactory.getLogger(SipTransactionManager.class);
  private final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(2);

  public SipTransactionManager() throws DsException {

    if (smp_theSingleton != null) {
      throw new DsException("There can only be one DsSipPacketProcessor ");
    }

    smp_theSingleton = this;
  }

  public static SipTransactionManager getInstance() throws NullPointerException {
    if (smp_theSingleton == null) {
      throw new NullPointerException(
          "SipTransactionManager.getInstance(): "
              + " Trying to get the SipTransactionManager before constructing it.");
    }

    return smp_theSingleton;
  }

  /**
   * This method should now replace processMessage as the primary entry into
   * the Transaction Manager.  It used to be processMessage(), but that
   * required another class to handle the message parsing phase.  It is
   * preferred that the Transaction Manager handle the call to the parser.
   * This way, parser exceptions can be handled directly.  After the message
   * is parsed, processMessage() is called.
   *
   * @param msgBytes the unparsed message to be processed
   */
  public void processMessageBytes(SipMessageBytes msgBytes) {

    DsSipMessage message = null;

    try {
      message = DsSipMessage.createMessage(msgBytes.getMessageBytes(), true, true);
      message.setTimestamp(msgBytes.getTimestamp());
      LOG.info("Dump the SIP message object" + message.toString());
    } catch (DsSipParserException pe) {
      LOG.error("Exception " + pe);
    } catch (DsSipKeyValidationException kve) {
      LOG.error("Exception " + kve);
    } catch (DsSipVersionValidationException vve) {
      LOG.error("Exception " + vve);
    } catch (DsSipMessageValidationException mve) {
      LOG.error("Exception " + mve);
    } catch (DsSipParserListenerException ple) {
      LOG.error("Exception " + ple);
    }
  }
}

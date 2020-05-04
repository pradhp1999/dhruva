package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.transport.MessageForwarder;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements MessageForwarder interface defined by transport layer This is singleton
 * class and only one instance can be initiated Receives the raw sip message and encapsulated in
 * SipMessageBytes object having bindingInfo It will invoke transaction layer asynchronously using
 * completable future
 */
public class SipPacketProcessor implements MessageForwarder {

  protected static SipPacketProcessor smp_theSingleton = null;

  protected ExecutorService sipProcessor;

  /**
   * @param executorService Pass the executor service to be used for sip packet processing
   *     Responsbility of initiating lies with outer layer using this class
   * @throws DsException
   */
  public SipPacketProcessor(ExecutorService executorService) throws DsException {

    if (smp_theSingleton != null) {
      throw new DsException("There can only be one DsSipPacketProcessor ");
    }

    smp_theSingleton = this;
    sipProcessor = executorService;
  }

  /**
   * @return Singleton instance of this class
   * @throws NullPointerException
   */
  public static SipPacketProcessor getInstance() throws NullPointerException {
    if (smp_theSingleton == null) {
      throw new NullPointerException(
          "DsSipPacketProcessor.getInstance(): "
              + " Trying to get the DsSipPacketProcessor before constructing it.");
    }

    return smp_theSingleton;
  }

  /**
   * Implementation of processMessage provided by transport layer Do not block here or do compute
   * intensive work here, return ASAP Callback registered with transport.
   *
   * @param messageBytes Raw sip message bytes received from transport layer. This is full message
   *     and message boundaries are taken care by transport We are not expecting streams here
   * @param bindingInfo Data structure that holds the connection information to be used by upper
   *     layers
   */
  @Override
  public void processMessage(byte[] messageBytes, DsBindingInfo bindingInfo) {

    CompletableFuture<Void> runAsync =
        CompletableFuture.runAsync(
            createMessageBytes(messageBytes, (DsBindingInfo) bindingInfo.clone()),
            sipProcessor.getExecutorThreadPool(ExecutorType.SIP_TRANSACTION_PROCESSOR));
  }

  /**
   * Encapsulated the raw bytes in to Runnable implementation object holding the binding info and
   * bytes
   *
   * @param bytes
   * @param binfo
   * @return
   */
  protected DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo) {
    return new SipMessageBytes(bytes, binfo);
  }
}

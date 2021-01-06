package com.cisco.dhruva.app.util.predicates;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipInviteMessage;
import java.util.function.Predicate;

public class SipPredicates {

  public static final Predicate<IDhruvaMessage> isRequest =
      dhruvaMessage -> dhruvaMessage.isRequest();
  public static final Predicate<IDhruvaMessage> isInvite =
      dhruvaMessage ->
          ((DsSipInviteMessage) dhruvaMessage.getMessageBody().getPayloadData())
              .getMethod()
              .equalsIgnoreCase("INVITE");
}

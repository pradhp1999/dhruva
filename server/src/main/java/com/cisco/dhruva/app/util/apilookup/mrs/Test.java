package com.cisco.dhruva.app.util.apilookup.mrs;

import akka.actor.typed.ActorRef;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.function.Function;
import java.util.function.Predicate;

public class Test {

  Predicate<IDhruvaMessage> cmr =
      dhruvaMessage -> dhruvaMessage.getReqURI().toLowerCase().contains("webex.com");

  Predicate<IDhruvaMessage> shorturi =
      dhruvaMessage -> dhruvaMessage.getReqURI().toLowerCase().matches("webex.com");

  void directive(Function<Predicate, ActorRef> func) {}

  public static void main(String[] args) {}
}

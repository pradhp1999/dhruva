package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.DhruvaProperties;
import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.app.util.predicates.CallingPredicates;
import com.cisco.dhruva.app.util.predicates.SipPredicates;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.util.function.Predicate;
import org.springframework.context.ApplicationContext;

public class OneOnOneCallingFlowActor extends CallFlow {

  private static Predicate<IDhruvaMessage> filter;
  private DhruvaProperties dhruvaProperties;

  // TODO, This is temp fix for predicate checks.
  // Currently we are not handling cascade calls and accepting calls with reqURI ending with
  // webex.com
  static void init() {
    filter =
        SipPredicates.isInvite
            .and(SipPredicates.isRequest)
            .and(CallingPredicates.isHostPortionDialInCalling);
  }

  public OneOnOneCallingFlowActor(ActorContext<Command> context) {
    super(context);
    ApplicationContext applicationContext = SpringApplicationContext.getAppContext();
    if (applicationContext == null) throw new DhruvaException("spring app context null");
    dhruvaProperties = applicationContext.getBean(DhruvaProperties.class);
  }

  static Behavior<Command> create() {
    return Behaviors.setup(OneOnOneCallingFlowActor::new);
  }

  public static Predicate getFilter() {
    init();
    return filter;
  }

  @Override
  ReceiveBuilder<Command> callFlowBehavior() {
    return null;
  }

  @Override
  Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand) {
    getContext().getLog().info("Routing Decision is in cluster L2SIP");

    doCallFlowCommand.replyTo.tell(
        new RouteResponse(
            new Destination(
                DestinationType.SRV,
                dhruvaProperties.getL2SIPClusterAddress() + ":5061",
                "DhruvaTlsPrivate"),
            null,
            doCallFlowCommand.dhruvaMessage));

    return this;
  }
}

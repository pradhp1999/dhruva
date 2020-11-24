package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.DhruvaProperties;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.util.function.Predicate;

public class OneOnOneCallingFlowActor extends CallFlow {

  private static Predicate<IDhruvaMessage> filter;
  private DhruvaProperties dhruvaProperties;

  static void init() {
    filter = dhruvaMessage -> true;
  }

  public OneOnOneCallingFlowActor(ActorContext<Command> context) {
    super(context);
    dhruvaProperties = SpringApplicationContext.getAppContext().getBean(DhruvaProperties.class);
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
                "DhruvaTlsNetwork"),
            null,
            doCallFlowCommand.dhurvaMessage));
    return this;
  }
}

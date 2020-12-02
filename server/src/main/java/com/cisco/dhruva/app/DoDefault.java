package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.function.Predicate;

public class DoDefault extends CallFlow {

  private static Predicate<IDhruvaMessage> filter;

  static void init() {
    filter = dhruvaMessage -> true;
  }

  public DoDefault(ActorContext<CallFlow.Command> context) {
    super(context);
  }

  static Behavior<Command> create() {
    return Behaviors.setup(DoDefault::new);
  }

  public static Predicate<IDhruvaMessage> getFilter() {
    init();
    return filter;
  }

  @Override
  ReceiveBuilder<Command> callFlowBehavior() {
    return null;
  }

  @Override
  Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand) {
    getContext()
        .getLog()
        .info("Routing Decision is Default-SIP , Message will be routed to Route or Request URI");

    doCallFlowCommand.replyTo.tell(
        new RouteResponse(
            new Destination(
                DestinationType.DEFAULT_SIP,
                doCallFlowCommand.dhruvaMessage.getReqURI(),
                doCallFlowCommand.dhruvaMessage.getNetwork()),
            null,
            doCallFlowCommand.dhruvaMessage));
    return this;
  }
}

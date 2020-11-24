package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.function.Predicate;

public class DoDefault extends CallFlow {

  private static final Predicate<IDhruvaMessage> filter = dhruvaMessage -> true;

  public DoDefault(ActorContext<CallFlow.Command> context) {
    super(context);
  }

  static Behavior<Command> create() {
    return Behaviors.setup(DoDefault::new);
  }

  public static Predicate getFilter() {
    return filter;
  }

  @Override
  ReceiveBuilder<Command> callFlowBehavior() {
    return null;
  }

  @Override
  Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand) {
    return defaultSipResponse(doCallFlowCommand);
  }
}

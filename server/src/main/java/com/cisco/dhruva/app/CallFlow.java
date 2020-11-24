package com.cisco.dhruva.app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.app.CallFlow.Command;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class CallFlow extends AbstractBehavior<Command> {

  private static final ConcurrentHashMap<Predicate<IDhruvaMessage>, Supplier<Behavior<Command>>>
      callFlowMap = new ConcurrentHashMap<>();

  static {
    registerCallFlow();
  }

  public CallFlow(ActorContext<Command> context) {
    super(context);
  }

  static ConcurrentHashMap<Predicate<IDhruvaMessage>, Supplier<Behavior<Command>>> getCallFlow() {
    return callFlowMap;
  }

  static void registerCallFlow() {
    callFlowMap.put(DoDefault.getFilter(), DoDefault::create);
  }

  abstract ReceiveBuilder<Command> callFlowBehavior();

  abstract Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand);

  public static Predicate getFilter() {
    return null;
  }

  public interface Command {}

  private abstract static class DoCallFlow implements Command {

    final ActorRef replyTo;
    final IDhruvaMessage dhurvaMessage;

    public DoCallFlow(ActorRef replyTo, IDhruvaMessage dhurvaMessage) {
      this.replyTo = replyTo;
      this.dhurvaMessage = dhurvaMessage;
    }
  }

  public static final class DoCallFlowForRequest extends DoCallFlow {

    public DoCallFlowForRequest(ActorRef replyTo, IDhruvaMessage dhurvaMessage) {
      super(replyTo, dhurvaMessage);
    }
  }

  public static final class DoCallFlowForResponse extends DoCallFlow {

    public DoCallFlowForResponse(ActorRef replyTo, IDhruvaMessage dhurvaMessage) {
      super(replyTo, dhurvaMessage);
    }
  }

  @Override
  public Receive<Command> createReceive() {
    return Optional.ofNullable(callFlowBehavior())
        .orElse(newReceiveBuilder())
        .onMessage(DoCallFlowForRequest.class, this::handleCall)
        .onMessage(DoCallFlowForResponse.class, this::handleResponse)
        .build();
  }

  Behavior<Command> handleCall(CallFlow.DoCallFlow doCallFlowCommand) {
    if (doCallFlowCommand.dhurvaMessage.isMidCall()) {
      return defaultSipResponse(doCallFlowCommand);
    } else {
      return handleRequest((DoCallFlowForRequest) doCallFlowCommand);
    }
  }

  Behavior<Command> handleResponse(DoCallFlowForResponse doCallFlowCommand) {
    return defaultSipResponse(doCallFlowCommand);
  }

  Behavior<Command> defaultSipResponse(DoCallFlow doCallFlowCommand) {
    doCallFlowCommand.replyTo.tell(
        new RouteResponse(
            new Destination(
                DestinationType.DEFAULT_SIP, doCallFlowCommand.dhurvaMessage.getReqURI(), "sipnet"),
            null,
            doCallFlowCommand.dhurvaMessage));
    return this;
  }
}

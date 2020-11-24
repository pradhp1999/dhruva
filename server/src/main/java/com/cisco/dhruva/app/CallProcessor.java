package com.cisco.dhruva.app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.cisco.dhruva.app.CallFlow.DoCallFlowForRequest;
import com.cisco.dhruva.app.CallFlow.DoCallFlowForResponse;
import com.cisco.dhruva.app.ControllerActor.Command;
import com.cisco.dhruva.app.ControllerActor.DoneProcessing;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class CallProcessor extends AbstractBehavior<CallProcessor.CallProcessorCommand> {

  private final ActorRef<Command> referTo;
  private ActorRef<CallFlow.Command> callFlowActor;

  @Override
  public Receive<CallProcessor.CallProcessorCommand> createReceive() {
    return newReceiveBuilder()
        .onMessage(FindCallPathRequest.class, this::callFlowRequest)
        .onMessage(FindCallPathResponse.class, this::callFlowResponse)
        .onMessage(RouteResponse.class, this::onDestinationResponse)
        .build();
  }

  interface CallProcessorCommand {}

  private static class FindCallPath implements CallProcessorCommand {

    final ActorRef replyTo;
    final ActorContext actorContext;
    final IDhruvaMessage dhurvaMessage;

    public FindCallPath(ActorRef replyTo, ActorContext actorSystem, IDhruvaMessage dhurvaMessage) {
      this.replyTo = replyTo;
      this.actorContext = actorSystem;
      this.dhurvaMessage = dhurvaMessage;
    }
  }

  public static final class FindCallPathRequest extends FindCallPath {

    public FindCallPathRequest(
        ActorRef replyTo, ActorContext actorSystem, IDhruvaMessage dhurvaMessage) {
      super(replyTo, actorSystem, dhurvaMessage);
    }
  }

  public static final class FindCallPathResponse extends FindCallPath {

    public FindCallPathResponse(
        ActorRef replyTo, ActorContext actorSystem, IDhruvaMessage dhurvaMessage) {
      super(replyTo, actorSystem, dhurvaMessage);
    }
  }

  public static final class RouteResponse implements CallProcessorCommand {

    final Destination destination;
    final Exception error;
    final IDhruvaMessage message;

    public RouteResponse(Destination destination, Exception error, IDhruvaMessage message) {
      this.destination = destination;
      this.error = error;
      this.message = message;
    }
  }

  public static Behavior<CallProcessorCommand> create(ActorRef<Command> referTo) {
    return Behaviors.setup(ctx -> new CallProcessor(ctx, referTo));
  }

  private CallProcessor(ActorContext<CallProcessorCommand> context, ActorRef<Command> referTo) {
    super(context);
    this.referTo = referTo;
  }

  private Behavior<CallProcessorCommand> callFlowResponse(
      FindCallPathResponse findCallPathResponse) {

    if (callFlowActor == null) {
      findCallPathResponse.replyTo.tell(
          new ControllerActor.DoneProcessing(
              findCallPathResponse.dhurvaMessage,
              null,
              new Exception("CallFlow actor doesnot exist for response")));
    } else {
      callFlowActor.tell(
          new DoCallFlowForResponse(getContext().getSelf(), findCallPathResponse.dhurvaMessage));
    }
    return Behaviors.same();
  }

  private Behavior<CallProcessorCommand> callFlowRequest(FindCallPathRequest findCallPathRequest) {

    if (callFlowActor == null) {
      Optional<Supplier<Behavior<CallFlow.Command>>> actorCreate =
          getCallFlowActor(findCallPathRequest.dhurvaMessage);

      if (actorCreate.isPresent()) {
        createCallFlowActor(actorCreate.get(), findCallPathRequest.dhurvaMessage);

      } else {
        findCallPathRequest.replyTo.tell(
            new ControllerActor.DoneProcessing(
                findCallPathRequest.dhurvaMessage, null, new Exception("Call Path not found")));
        return this;
      }
    }
    callFlowActor.tell(
        new DoCallFlowForRequest(getContext().getSelf(), findCallPathRequest.dhurvaMessage));
    return this;
  }

  @NotNull
  private Optional<Supplier<Behavior<CallFlow.Command>>> getCallFlowActor(
      IDhruvaMessage dhruvaMessage) {
    System.out.println(
        Thread.currentThread().getName()
            + " "
            + Calendar.getInstance().getTimeInMillis()
            + " defineCallFlow");

    ConcurrentHashMap<Predicate<IDhruvaMessage>, Supplier<Behavior<CallFlow.Command>>> callFlowMap =
        CallFlow.getCallFlow();

    Optional<Supplier<Behavior<CallFlow.Command>>> actorCreate =
        callFlowMap.entrySet().stream()
            .filter(predicateBehaviorEntry -> predicateBehaviorEntry.getKey().test(dhruvaMessage))
            .map(Entry::getValue)
            .findFirst();
    return actorCreate;
  }

  private void createCallFlowActor(
      Supplier<Behavior<CallFlow.Command>> actorRefBehaviorFunction, IDhruvaMessage message) {

    callFlowActor = getContext().spawn(actorRefBehaviorFunction.get(), "destination");
  }

  private Behavior<CallProcessorCommand> onDestinationResponse(RouteResponse response) {

    System.out.println(
        Thread.currentThread().getName()
            + " "
            + Calendar.getInstance().getTimeInMillis()
            + " onDestinationResponse");
    referTo.tell(new DoneProcessing(response.message, response.destination, response.error));
    return Behaviors.same();
  }
}

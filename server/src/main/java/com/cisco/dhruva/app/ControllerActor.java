package com.cisco.dhruva.app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.cisco.dhruva.app.CallProcessor.CallProcessorCommand;
import com.cisco.dhruva.app.CallProcessor.FindCallPathRequest;
import com.cisco.dhruva.app.CallProcessor.FindCallPathResponse;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public class ControllerActor {

  private final ActorContext<Command> context;
  private ActorRef<CallProcessorCommand> callProcessorActorRef;
  private ActorRef<RouteResult> requestReplyTo;
  private ActorRef<RouteResult> responseReplyTo;

  public ControllerActor(ActorContext<Command> context) {
    this.context = context;
  }

  public static Behavior<ControllerActor.Command> create() {
    return Behaviors.setup(context -> new ControllerActor(context).behaviour());
  }

  private Behavior<ControllerActor.Command> behaviour() {
    return Behaviors.receive(Command.class)
        .onMessage(FindRouteForRequest.class, this::onFindRouteRequest)
        .onMessage(FindRouteForResponse.class, this::onFindRouteResponse)
        .onMessage(DoneProcessing.class, this::done)
        .onMessage(GracefulShutdown.class, message -> onGracefulShutdown())
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onFindRouteRequest(FindRouteForRequest findRoute) {
    getCallProcessorActor(CallProcessor.create(findRoute.referTo));
    this.requestReplyTo = findRoute.replyTo;
    callProcessorActorRef.tell(
        new FindCallPathRequest(findRoute.referTo, context, findRoute.message));
    return Behaviors.same();
  }

  private Behavior<Command> onFindRouteResponse(FindRouteForResponse findRoute) {
    getCallProcessorActor(CallProcessor.create(findRoute.referTo));
    this.responseReplyTo = findRoute.replyTo;
    callProcessorActorRef.tell(
        new FindCallPathResponse(findRoute.referTo, context, findRoute.message));
    return Behaviors.same();
  }

  private Behavior<Command> onGracefulShutdown() {
    context.getSystem().log().info("Initiating graceful shutdown...");

    // perform graceful stop, executing cleanup before final system termination
    // behavior executing cleanup is passed as a parameter to Actor.stopped
    return Behaviors.stopped(() -> context.getSystem().log().info("Cleanup!"));
  }

  private Behavior<Command> onPostStop() {
    context.getSystem().log().info("Actor {} stopped", getClass().toString());
    return Behaviors.same();
  }

  private void getCallProcessorActor(Behavior<CallProcessorCommand> callProcessorCommandBehavior) {
    if (callProcessorActorRef == null) {
      callProcessorActorRef = context.spawn(callProcessorCommandBehavior, "callFlow");
    }
  }

  private Behavior<Command> done(DoneProcessing doneProcessing) {
    context.getLog().info("App determined result {} ", doneProcessing);
    if (doneProcessing.message.isRequest())
      requestReplyTo.tell(
          new RouteResult(
              doneProcessing.message, doneProcessing.destination, doneProcessing.error));
    else
      responseReplyTo.tell(
          new RouteResult(
              doneProcessing.message, doneProcessing.destination, doneProcessing.error));

    return Behaviors.same();
  }

  public interface Command {}

  public static class RouteResult implements Command {
    public IDhruvaMessage response;
    public Destination destination;
    public Exception error;

    public RouteResult(IDhruvaMessage message, Destination dest, Exception e) {
      this.response = message;
      this.destination = dest;
      this.error = e;
    }
  }

  public enum GracefulShutdown implements Command {
    INSTANCE
  }

  private abstract static class FindRoute implements Command {

    final IDhruvaMessage message;
    final ActorRef<ControllerActor.Command> referTo;
    final ActorRef<ControllerActor.RouteResult> replyTo;

    public FindRoute(
        IDhruvaMessage message, ActorRef<Command> referTo, ActorRef<RouteResult> replyTo) {
      this.message = message;
      this.referTo = referTo;
      this.replyTo = replyTo;
    }
  }

  public static class FindRouteForRequest extends FindRoute {

    public FindRouteForRequest(
        IDhruvaMessage message, ActorRef<Command> referTo, ActorRef<RouteResult> replyTo) {
      super(message, referTo, replyTo);
    }
  }

  public static class FindRouteForResponse extends FindRoute {

    public FindRouteForResponse(
        IDhruvaMessage message, ActorRef<Command> referTo, ActorRef<RouteResult> replyTo) {
      super(message, referTo, replyTo);
    }
  }

  public static class DoneProcessing implements Command {

    final IDhruvaMessage message;
    final Destination destination;
    final Exception error;

    public DoneProcessing(IDhruvaMessage message, Destination destination, Exception error) {
      this.message = message;
      this.destination = destination;
      this.error = error;
    }

    @Override
    public String toString() {
      return "DoneProcessing{"
          + "message="
          + message
          + ", destination="
          + destination
          + ", error="
          + error
          + '}';
    }
  }

  public static void main(String[] args) {
    IDhruvaMessage iDhruvaMessage = new DhruvaMessageImpl(null, null, null);
    iDhruvaMessage.setReqURI("guru@ebex.com");
    //   ActorSystem<Command> actorref = ActorSystem.create(ControllerActor.create(), "Controller");

    //    actorref.tell(new FindRoute(iDhruvaMessage, actorref));
  }
}

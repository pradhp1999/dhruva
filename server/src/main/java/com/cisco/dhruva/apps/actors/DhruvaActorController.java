package com.cisco.dhruva.apps.actors;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/*
   This is the entry point for all the App call processing logic
   Life of this actor has to be per transaction
   For every new transaction , new main actor will be created and handle the response
*/

public class DhruvaActorController extends AbstractBehavior<DhruvaActorController.DhruvaMessage> {

  private final ActorRef<DhruvaMessage> callTypeProcessor;

  public DhruvaActorController(
      ActorContext<DhruvaMessage> context, ActorRef<DhruvaMessage> callTypeProcessor) {
    super(context);
    this.callTypeProcessor = callTypeProcessor;
  }

  public interface DhruvaMessage {};

  public static final class DhruvaRequest implements DhruvaMessage {
    public DhruvaRequest() {}
  }

  public static final class DhruvaResponse implements DhruvaMessage {
    public DhruvaResponse() {}
  }

  static final class Stop implements DhruvaMessage {
    final ActorRef<Done> replyTo;

    Stop(ActorRef<Done> replyTo) {
      this.replyTo = replyTo;
    }
  }

  Behavior<DhruvaMessage> onPostStop() {
    getContext().getLog().info("Actor {} stopped", getClass().toString());
    return Behaviors.stopped();
  }

  @Override
  public Receive<DhruvaMessage> createReceive() {
    return newReceiveBuilder()
        .onMessage(DhruvaRequest.class, this::onNewRequest)
        .onMessage(DhruvaResponse.class, this::onResponse)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  public static Behavior<DhruvaMessage> create(ActorRef<DhruvaMessage> callTypeProcessor) {
    return Behaviors.setup(context -> new DhruvaActorController(context, callTypeProcessor));
  }

  private Behavior<DhruvaMessage> onNewRequest(DhruvaMessage request) {
    getContext().getLog().info("Received request {} ", request);
    return Behaviors.same();
  }

  private Behavior<DhruvaMessage> onResponse(DhruvaMessage response) {
    getContext().getLog().info("Received request {} ", response);
    return Behaviors.same();
  }
}

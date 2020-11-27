package com.cisco.dhruva.router;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.ControllerActor;
import com.cisco.dhruva.app.ControllerActor.Command;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.time.Duration;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppEngine {

  private static final Duration TIMEOUT_SPAWN = Duration.ofSeconds(100);
  private Consumer<IDhruvaMessage> session;

  private ActorRef<Command> actorRef;

  private static final Logger logger = DhruvaLoggerFactory.getLogger(AppEngine.class);

  private static final ActorSystem<SpawnProtocol.Command> system;

  static {
    // TODO,need to call system.terminate while shutting down
    system =
        ActorSystem.create(Behaviors.setup(context -> SpawnProtocol.create()), "guardianActor");
  }

  public AppEngine(Consumer<IDhruvaMessage> session) {
    Objects.requireNonNull(session);
    this.session = session;
    // Spawn Akka actor to handle all messages for this transaction
    this.start();
  }

  public void start() {
    // Initialize
    createControllerActor();
  }

  public void handleRequest(IDhruvaMessage message) throws DhruvaException {
    callActor(message, true);
  }

  public void handleResponse(IDhruvaMessage message) throws DhruvaException {
    callActor(message, false);
  }

  private void callActor(IDhruvaMessage message, boolean isRequest) throws DhruvaException {

    // TODO supply our own executor service
    // Emit event in case of route failure
    if (isRequest) {
      CompletionStage<ControllerActor.RouteResult> result =
          AskPattern.ask(
              actorRef,
              replyTo -> new ControllerActor.FindRouteForRequest(message, actorRef, replyTo),
              Duration.ofSeconds(60),
              system.scheduler());

      result
          .whenCompleteAsync(appCallback)
          .exceptionally(
              throwable -> {
                logger.error("exit" + "error in route processing", throwable.getMessage());
                return new ControllerActor.RouteResult(
                    null, null, new DhruvaException("route failure"));
              });
    } else {
      CompletionStage<ControllerActor.RouteResult> result =
          AskPattern.ask(
              actorRef,
              replyTo -> new ControllerActor.FindRouteForResponse(message, actorRef, replyTo),
              Duration.ofSeconds(60),
              system.scheduler());
      result
          .whenCompleteAsync(appCallback)
          .exceptionally(
              throwable -> {
                logger.error("error in route processing", throwable.getMessage());
                return new ControllerActor.RouteResult(
                    null, null, new DhruvaException("route failure"));
              });
    }
  }

  private void createControllerActor() throws DhruvaException {

    if (actorRef == null) {

      if (system == null) {
        logger.error(
            "Actor guardian system is null, App initialization had failed , all calls will fail ");
        throw new DhruvaException(
            "Actor guardian system is null, App initialization had failed , all calls will fail");
      }

      CompletionStage<ActorRef<Command>> controllerActorFuTure;
      controllerActorFuTure =
          AskPattern.ask(
              system,
              replyTo ->
                  new SpawnProtocol.Spawn<Command>(
                      ControllerActor.create(), "Controller", Props.empty(), replyTo),
              TIMEOUT_SPAWN,
              system.scheduler());

      try {
        actorRef =
            controllerActorFuTure
                .toCompletableFuture()
                .get(TIMEOUT_SPAWN.getSeconds(), TimeUnit.SECONDS);

        // Schedule release of controller actor after 120 sec, TODO make time val configurable
        // Use Scheduled Executor service

        new Timer()
            .schedule(
                wrap(
                    () -> {
                      if (actorRef != null)
                        actorRef.tell(ControllerActor.GracefulShutdown.INSTANCE);
                    }),
                320000);

      } catch (Exception e) {
        throw new DhruvaException(
            "Exception spawning controller actor using spawn-protocol , call will fail",
            e,
            "Could be a bug in Controller actor spawn");
      }
    }
  }

  BiConsumer<ControllerActor.RouteResult, Throwable> appCallback =
      (routeResult, throwable) -> {
        logger.setMDC(routeResult.response.getLogContext().getLogContextAsMap());
        if (routeResult.error != null || throwable != null) {
          logger.error("exception in route processing {}", routeResult.error);
          IDhruvaMessage errorResponse = null;
          try {
            errorResponse =
                new DhruvaMessageImpl(
                    null,
                    null,
                    MessageBody.fromPayloadData(
                        DsProxyResponseGenerator.createNotFoundResponse(
                            (DsSipRequest) routeResult.response.getMessageBody().getPayloadData()),
                        MessageBodyType.SIPRESPONSE));
          } catch (DsException e) {
            logger.error("exception in app layer {}", e);
          }
          session.accept(errorResponse);
        } else {
          ExecutionContext ctx = routeResult.response.getContext();
          ctx.set(PROXY_ROUTE_RESULT, routeResult.destination);
          session.accept(routeResult.response);
        }
      };

  private static TimerTask wrap(Runnable r) {
    return new TimerTask() {

      @Override
      public void run() {
        r.run();
      }
    };
  }
}

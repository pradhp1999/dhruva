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
import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.SpringApplicationContext;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.springframework.context.ApplicationContext;

public class AppEngine {

  private static final Duration TIMEOUT_SPAWN = Duration.ofSeconds(100);
  private Consumer<IDhruvaMessage> session;

  private ActorRef<Command> actorRef;

  private static final Logger logger = DhruvaLoggerFactory.getLogger(AppEngine.class);

  private static final ActorSystem<SpawnProtocol.Command> system;

  private static ExecutorService executorService;
  private static ScheduledExecutorService timer;

  static {
    // TODO,need to call system.terminate while shutting down
    system =
        ActorSystem.create(Behaviors.setup(context -> SpawnProtocol.create()), "guardianActor");
  }

  public AppEngine(Consumer<IDhruvaMessage> session) {
    Objects.requireNonNull(session);
    this.session = session;

    ApplicationContext applicationContext = SpringApplicationContext.getAppContext();
    if (applicationContext == null) throw new DhruvaException("spring app context null");
    executorService = applicationContext.getBean(ExecutorService.class);
    // Spawn Akka actor to handle all messages for this transaction
    this.start();
  }

  public static void startShutdownTimers(ExecutorService executorService) {
    if (timer != null) {
      timer.shutdownNow();
    }
    executorService.startScheduledExecutorService(ExecutorType.AKKA_CONTROLLER_TIMER, 5);
    timer = executorService.getScheduledExecutorThreadPool(ExecutorType.AKKA_CONTROLLER_TIMER);
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
          .whenCompleteAsync(
              appCallback,
              executorService.getExecutorThreadPool(ExecutorType.SIP_TRANSACTION_PROCESSOR))
          .exceptionally(
              throwable -> {
                logger.error("error in route processing", throwable.getMessage());
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
          .whenCompleteAsync(
              appCallback,
              executorService.getExecutorThreadPool(ExecutorType.SIP_TRANSACTION_PROCESSOR))
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

        timer.schedule(
            () -> {
              if (actorRef != null) actorRef.tell(ControllerActor.GracefulShutdown.INSTANCE);
            },
            240,
            TimeUnit.SECONDS);

      } catch (Exception e) {
        throw new DhruvaException(
            "Exception spawning controller actor using spawn-protocol , call will fail",
            e.getCause(),
            "Could be a bug in Controller actor spawn");
      }
    }
  }

  BiConsumer<ControllerActor.RouteResult, Throwable> appCallback =
      (routeResult, throwable) -> {
        logger.setMDC(routeResult.response.getLogContext().getLogContextAsMap());
        if (routeResult.error != null || throwable != null) {
          logger.error("exception in route processing {}", routeResult.error.getMessage());
          IDhruvaMessage errorResponse = null;
          try {
            errorResponse =
                new DhruvaMessageImpl(
                    routeResult.response.getContext().copy(),
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
}

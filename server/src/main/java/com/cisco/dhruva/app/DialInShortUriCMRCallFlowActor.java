package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.DhruvaProperties;
import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.app.util.predicates.CMRPredicates;
import com.cisco.dhruva.app.util.predicates.SipPredicates;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.util.function.Predicate;
import org.springframework.context.ApplicationContext;

public class DialInShortUriCMRCallFlowActor extends CallFlow {
  private DhruvaProperties dhruvaProperties;
  public static Predicate<IDhruvaMessage> filter;

  public DialInShortUriCMRCallFlowActor(ActorContext<Command> context) {

    super(context);
    ApplicationContext applicationContext = SpringApplicationContext.getAppContext();
    if (applicationContext == null) throw new DhruvaException("spring app context null");
    dhruvaProperties = applicationContext.getBean(DhruvaProperties.class);
  }

  static void init() {

    filter =
        SipPredicates.isInvite
            .and(SipPredicates.isRequest)
            .and(CMRPredicates.isUserPortionDialInCMRShortUri)
            .and(CMRPredicates.isHostPortionDialInCMRVanityShortUri);
  }

  public static Predicate getFilter() {
    init();
    return filter;
  }

  static Behavior<Command> create() {
    return Behaviors.setup(DialInShortUriCMRCallFlowActor::new);
  }

  @Override
  ReceiveBuilder<Command> callFlowBehavior() {
    return null;
  }

  @Override
  Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand) {

    getContext().getLog().info("Routing Decision is in CMR dial in ShortUri");

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

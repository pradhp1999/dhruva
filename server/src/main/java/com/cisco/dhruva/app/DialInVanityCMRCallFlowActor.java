package com.cisco.dhruva.app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import com.cisco.dhruva.DhruvaProperties;
import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.app.Destination.DestinationType;
import com.cisco.dhruva.app.util.DhruvaMessageHelper;
import com.cisco.dhruva.app.util.predicates.CMRPredicates;
import com.cisco.dhruva.app.util.predicates.SipPredicates;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageHeaders;
import com.cisco.dhruva.util.SpringApplicationContext;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.function.Predicate;

public class DialInVanityCMRCallFlowActor extends CallFlow {

  private DhruvaProperties dhruvaProperties;
  private static Predicate<IDhruvaMessage> filter;

  public DialInVanityCMRCallFlowActor(ActorContext<Command> context) {

    super(context);
    ApplicationContext applicationContext = SpringApplicationContext.getAppContext();
    if (applicationContext == null) throw new DhruvaException("spring app context null");
    dhruvaProperties = applicationContext.getBean(DhruvaProperties.class);
  }

  static void init() {

    filter =
        SipPredicates.isInvite
            .and(SipPredicates.isRequest)
            .and(CMRPredicates.isUserPortionDialInCMRVanity)
            .and(CMRPredicates.isHostPortionDialInCMRVanityShortUri);
  }

  public static Predicate getFilter() {
    init();
    return filter;
  }

  static Behavior<Command> create() {
    return Behaviors.setup(DialInVanityCMRCallFlowActor::new);
  }

  @Override
  ReceiveBuilder<Command> callFlowBehavior() {
    return null;
  }

  @Override
  Behavior<Command> handleRequest(CallFlow.DoCallFlowForRequest doCallFlowCommand) {

    String normalizeReqUri =
        DhruvaMessageHelper.normalizeReqUri(doCallFlowCommand.dhruvaMessage.getReqURI());
    doCallFlowCommand.dhruvaMessage.setReqURI(normalizeReqUri);

    getContext().getLog().info("Routing Decision is in CMR dial in Vanity ");
    IDhruvaMessage message = doCallFlowCommand.dhruvaMessage;
    MessageHeaders headers = new MessageHeaders(new HashMap<>());
    headers.put(
        "Route",
        "sip:" + dhruvaProperties.getL2SIPClusterAddress() + ":5061" + ";lr;transport=tls");
    message.setHeaders(headers);
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

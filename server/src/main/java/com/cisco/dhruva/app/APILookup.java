package com.cisco.dhruva.app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.cisco.dhruva.app.APILookup.Command;
import java.util.Calendar;

public class APILookup extends AbstractBehavior<Command> {

  public static Behavior<Command> create() {
    return Behaviors.setup(APILookup::new);
  }

  private APILookup(ActorContext<Command> context) {
    super(context);
  }

  public interface Command {}

  public interface Response {}

  public static class MRSResponse implements Response {

    public final String l2sipAddress;

    public MRSResponse(String l2sipAddress) {
      this.l2sipAddress = l2sipAddress;
    }
  }

  public static class MRSLookup implements Command {

    public final String mrsUri;
    public final MRSLookupType mrsLookupType;
    public final ActorRef<CallFlow.Command> replyTo;

    public MRSLookup(
        String mrsUri, MRSLookupType mrsLookupType, ActorRef<CallFlow.Command> replyTo) {
      this.mrsUri = mrsUri;
      this.mrsLookupType = mrsLookupType;
      this.replyTo = replyTo;
    }

    enum MRSLookupType {
      MEETING_NUMBER,
      MEETING_URI
    }
  }

  @Override
  public Receive<Command> createReceive() {
    System.out.println(
        Thread.currentThread().getName()
            + " "
            + Calendar.getInstance().getTimeInMillis()
            + " createReceive");
    return newReceiveBuilder().onMessage(MRSLookup.class, this::onMRSLookup).build();
  }

  private Behavior<Command> onMRSLookup(MRSLookup mrsLookupCommand) {
    //
    //    MRSClient mrsClient = new MRSClient("idbroker.webex.com", mrsLookupCommand.mrsUri, null);
    //
    //    String l2sipurl = mrsClient.getAttribute("MRS_L2SIP_ENDPOINT");

    System.out.println(
        Thread.currentThread().getName()
            + " "
            + Calendar.getInstance().getTimeInMillis()
            + " onMRSLookup");
    String l2sipurl = "l2sip.webex.com";
    //   mrsLookupCommand.replyTo.tell(new CallFlow.MRSResponse(l2sipurl, null));
    return this;
  }
}

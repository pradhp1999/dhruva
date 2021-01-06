package com.cisco.dhruva.app.util.predicates;

import com.cisco.dhruva.app.util.DhruvaMessageHelper;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.function.Predicate;

public class CallingPredicates {

  public static final Predicate<IDhruvaMessage> isHostPortionDialInCalling =
      dhruvaMessage ->
          DhruvaMessageHelper.getHostPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.HostPortionDialInCalling);
}

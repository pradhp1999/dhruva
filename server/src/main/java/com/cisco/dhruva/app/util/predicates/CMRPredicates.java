package com.cisco.dhruva.app.util.predicates;

import com.cisco.dhruva.app.util.DhruvaMessageHelper;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.function.Predicate;

public class CMRPredicates {

  public static final Predicate<IDhruvaMessage> isUserPortionDialInCMRStandard =
      dhruvaMessage ->
          DhruvaMessageHelper.getUserPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.UserPortionDialInCMRStandard);

  public static final Predicate<IDhruvaMessage> isHostPortionDialInCMRStandard =
      dhruvaMessage ->
          DhruvaMessageHelper.getHostPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.HostPortionDialInCMRStandard);

  public static final Predicate<IDhruvaMessage> isUserPortionDialInCMRVanity =
      dhruvaMessage ->
          DhruvaMessageHelper.getUserPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.UserPortionDialInCMRVanity);
  public static final Predicate<IDhruvaMessage> isUserPortionDialInCMRShortUri =
      dhruvaMessage ->
          DhruvaMessageHelper.getUserPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.UserPortionDialInCMRShortUri);

  public static final Predicate<IDhruvaMessage> isHostPortionDialInCMRVanityShortUri =
      dhruvaMessage ->
          DhruvaMessageHelper.getHostPortion(dhruvaMessage.getReqURI())
              .matches(RegexCallFlows.HostPortionDialInCMRVanityShortUri);
}

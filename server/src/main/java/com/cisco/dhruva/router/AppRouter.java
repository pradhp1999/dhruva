package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CallType;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppRouter {

  private final AppRoute<String> appRoute;

  // Define Exception class for Routing errors
  // MeetPass TODO
  @Autowired
  public AppRouter(AppRoute<String> appRoute) {
    this.appRoute = appRoute;
  }

  public RouteResult<String> route(
      final IDhruvaMessage request,
      CallType ctype,
      RouteType rType,
      final AppRouterManager.Track track)
      throws DhruvaException {
    track.setRouteType(rType, request.getReqURI());
    assert appRoute != null;
    if (rType == RouteType.REQURI) appRoute.addRoute(request.getReqURI(), ctype);
    return appRoute.match(request.getReqURI());
  }
}

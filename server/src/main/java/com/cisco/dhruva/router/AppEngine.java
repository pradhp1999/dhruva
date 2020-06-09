package com.cisco.dhruva.router;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;

import com.cisco.dhruva.common.CallType;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppEngine {

  private AbstractAppSession session;

  @Autowired private AppRouterManager routerManager;

  public AppEngine(AbstractAppSession session) {
    Objects.requireNonNull(session);
    // Objects.requireNonNull(routerManager);
    this.session = session;
    // TODO Fix Spring boot up issue, not able to autowire
    this.routerManager = new AppRouterManager();
    this.routerManager.setAppRouter(new AppRouter(new AppRoute<>()));
  }

  public void start() {
    // Initialize

  }

  public void handleMessage(IDhruvaMessage message) {
    AppRouter router = routerManager.getAppRouter();
    RouteResult<String> result =
        router.route(message, CallType.SIP, RouteType.REQURI, AppRouterManager.getTrack());
    ExecutionContext ctx = message.getContext();
    ctx.set(PROXY_ROUTE_RESULT, result.getDestination());
    session.handleResponse(message);
  }
}

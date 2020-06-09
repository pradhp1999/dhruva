package com.cisco.dhruva.router;

import com.cisco.dhruva.common.CallType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AppRoute<T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<SIPRoute<T>> patterns;

  public AppRoute() {
    this.patterns = new ArrayList<>();
  }

  public boolean isEmpty() {
    return getPatterns().isEmpty();
  }

  public void addRoute(final T destination, CallType type) {
    if (type == CallType.SIP) {
      getPatterns().add(new SIPRoute<>(destination));
    }
  }

  public List<SIPRoute<T>> getPatterns() {
    return patterns;
  }

  public RouteResult<T> match(final String path) {
    RouteResult<T> routingResult = null;
    for (SIPRoute<T> route : getPatterns()) {
      RouteResult<T> result = route.match(path);
      if (result != null) {
        routingResult = result;
      }
    }
    return routingResult;
  }
}

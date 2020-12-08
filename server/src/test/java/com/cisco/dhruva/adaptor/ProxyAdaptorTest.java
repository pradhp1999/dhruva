package com.cisco.dhruva.adaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.router.AppEngine;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.SIPRequestBuilder;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

public class ProxyAdaptorTest {
  private ApplicationContext applicationContext;
  private ExecutorService executorService;

  @Test
  public void testHandleRequestFlow() throws Exception {

    applicationContext = mock(ApplicationContext.class);
    executorService = mock(ExecutorService.class);

    SpringApplicationContext springApplicationContext = new SpringApplicationContext();
    springApplicationContext.setApplicationContext(applicationContext);

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
        mock(ScheduledThreadPoolExecutor.class);

    when(executorService.getScheduledExecutorThreadPool(ExecutorType.AKKA_CONTROLLER_TIMER))
        .thenReturn(scheduledThreadPoolExecutor);

    AppEngine.startShutdownTimers(executorService);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsProxyController controller = Mockito.mock(DsProxyController.class);
    AppAdaptorInterface adaptor = f.getProxyAdaptor(controller, new AppSession());
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));

    //    adaptor.handleRequest(request);
  }
}

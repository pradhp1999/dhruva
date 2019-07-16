package com.ciscospark.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/*
 * As of this writing, you cannot run the micro-service in IntelliJ via simply clicking on the "Play" button. You
 * need to create a run/debug configuration that uses a Tomcat container (or Jetty, but that is a royal PITA to set up),
 * and add the exploded hello-world-server jar to the configuration.
 *
 * Note that since this is a Spring Boot Application, this class is also a configuration class - there is generally
 * not a need to create a separate configuration class unless you specifically need to override some behavior that is
 * in AbstractConfig. Since we use autoconfiguration - see CiscoSparkServerAutoConfiguration in cisco-spark-autoconfigure
 * - a configuration instance of AbstractConfig is automatically available to the application, so you can simply
 * autowire in all of the beans that you are accustomed to using, such as wx2Properties, env, etc.
 *
 * In the case that you have to derive from Wx2ConfigAdapter/AbstractConfig to create a configuration class, the
 * pattern to use is as follows:
 *
 * @Configuration
 * @ServletComponentScan
 * @ConditionalOnWebApplication      // To prevent the context being loaded during unit tests
 * public class MyConfiguration extends Wx2ConfigAdapter {
 *
 *   @Override
 *   public void myOverride() { ... }
 *  ...
 * }
 *
 * There should not be many use cases that require this.
 *
 * Values such as service name, metrics namespace and user agent are automatically configured and picked up from
 * application.properties/application.yml. Specifically, the algorithm for this is:
 * 1. service-name <-- spring.application.name. If not present, then "application"
 * 2. metrics namespace <-- cisco-spark.server.name. If not present, then service-name.
 * 3. user agent <-- cisco-spark.server.name. If not present, then service-name.
 * 4. service base public URL <-- http://localhost:8080/cisco-spark.server.api-path, or http://localhost:8080/api if not
 *    present.
 */
@SpringBootApplication(exclude = WebMvcAutoConfiguration.class)
public class HelloWorldApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        setRegisterErrorPageFilter(false);
        return application.sources(HelloWorldApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HelloWorldApplication.class, args);
    }
}

## Hello World, a service that greets you

Aside from serving very little real-world purpose, this service is the beginning of your Cisco Spark service.


### client

The `client` module enables easy consumption of your service's API. From a technical perspective, this module is not required as downstream consumers can directly call your REST API over HTTP, however providing this module is expected and appreciated.

This module must:

1. Implement methods that call each of your public API endpoints.
2. Provide DTO classes that represent the objects accepted and returned by your REST endpoints.

The API exposed by this module is directly consumed by others, so be considerate when changing it.

### server

The `server` module implements your entire service. That includes:

1. API endpoints. Most often implemented as one or more `@RestController`s.
2. Properties and configuration.
3. Health monitoring.
4. Database and migrations.

Services are Spring Boot applications. If you are unfamiliar with Spring in general or Spring Boot, it would be wise to invest some time learning this foundational layer.

**API endpoints**

APIs are not someone else’s problem, they are the problem for everyone building Spark services. That's you. When developing your API, always thinking about the consumer first and keep it simple. Read and follow the [Spark Cloud API Manifesto](https://wiki.cisco.com/display/S4D/Spark+Cloud+API+Manifesto).

Your API will typically be implemented by annotating a class with `@RestController` and using the `@RequestMapping` annotation to specify paths, methods and other attributes of your requests.

**Properties and configuration**

Your service will run in many environments without modification to the source so all configuration must be externalized. Spring makes this very easy. To take advantage of this, define your properties similar to `HelloWorldProperties` – you can group your properties into different classes. The property values will be available at runtime and automatically injected from various sources including your `application.properties` file, environment variables and properties dynamically read from consul.

Your properties will be used primarily to construct your beans from your configuration class or classes or from components discovered through component scanning (see `@ComponentScan`).

**Health monitoring**

Your service automatically exposes a few serviceability endpoints. The `/ping` endpoint returns a JSON object that describes the health of your service and the health of any of your upstream services. It is very important that you add monitoring into your service that monitors its own health and the health of all upstream services.

If your service is degraded or unavailable in an environment and your health monitoring is still reporting that your service is healthy, this is a production-impacting bug in your service monitoring and should be treated as a high priority.

**Database and migrations**

TODO:

* `cisco-spark-starter-cassandra`
* Link to schema review process
* Migrations

### Testing

TODO:

* Unit testing
* Mockito, MockMVC
* Integration testing and when to use
* Journeys
* Testing vs monitoring

### Pipeline

Your service has a pipeline that controls the flow of commits to various environments, including integration, load test and production. This pipeline is fully described and controlled by the `Jenkinsfile` at the root of this repo. By committing changes to this file, you are changing your pipeline.

The Jenkinsfile contains steps based on a groovy based DSL called Jenkins Pipelines. The Jenkinsfile for hello-world offers an example that might be a good starting point for your service. It is suitable for a service that builds and tests using `mvn verify` and deploys to the integration and production environments.

The Jenkinsfile in this repo is also suitable for performing a build and tests against pull requests. Once a PR is opened, Jenkins will automatically perform a build and run the services tests. The results will be reflected in the PR itself. Typically, you will also configure GitHub to not allow merging of a PR unless this check has passed.

Likewise, if you create a new branch, Jenkins will automatically start building that branch for you. This allows for feature branches or a special load-test branch. You can see in the Jenkinsfile where it conditionally performs additional stages for the master branch.

Please note that Jenkins and the build slaves are a shared resource. As much as possible, perform local validation of your changes before opening a PR or pushing to a branch. Relying on the slaves to run your tests as a matter of your development cycle unfairly impacts other teams that rely on that same pool of slaves. 

### IPCentral

Integration with IPCentral is enabled automatically for your service, but it will require you to follow through with any actions identified by the IPCentral tool. In your pipeline, your service's dependencies are captured and stored as an artifact of the build. This dependency tree is used to keep IPCentral's record of your dependencies synchronized with the dependencies you declare in your Maven poms.

### API Documentation

TODO:

* PubHub
* Swagger

### Starters and auto configuration

TODO:

* `cisco-spark-starter-server`
* Pointers to `cisco-spark-base`?

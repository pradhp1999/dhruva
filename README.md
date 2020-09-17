# Dhruva, a SIP Edge service

Dhruva is a SIP Edge service that routes CMR SIP traffic from the Enterprise to the L2SIP in the correct Meetings cluster.

It is deployed in Meetings Platform (meet-PaaS).

## Getting Started

### Prerequisites
- Ensure you have access to clone this repo.
- Clone the repo in your IDE (IntelliJ Ultimate recommended (see below)) and make sure you have JDK8.
- Setup [secure access to Cisco's artifactory](https://sqbu-github.cisco.com/pages/WebexSquared/docs/DeveloperTools/maven.html).

#### Intellij Ultimate

- Talk to your manager for an IntelliJ Ultimate referral link, and create an account with your Cisco Email address.
- You will receive a confirmation Email.
- Confirm your account in the email, it will take you to jetbrains site to create your account.
- Then you can see your license ID.
- Go to Intellij Ultimate, under Help > Register..., enter your username and password.
 
### Build/Tests
- `mvn clean verify` at the top level builds and runs all tests.

### Running in Tomcat in Intellij IDE
- Go to Run -> Edit Configurations
- Click on + sign in new window and find Tomcat Server and select Local
- Now click on configure button to the right of 'Application Server' and give a name to your tomcat configuration
- Add path to your local tomcat installation (make sure the version is `> 7.x`)
- Optionally uncheck "After launch" under the "Open browser" section.
- Set the URL as `http://localhost:8080/dhruva_server_war_exploded/`
- In VM options field enter: `-Xmx2048m -Xms1024m`, and `-DexternalUrlProtocol=http`, and `-DjedisPoolHealthCheckMonitorEnabled=false`.
- Now go to the deployment tab
- Use + button to add the artifact `dhruva-server-exploded.war` (pick one with 'exploded' word in name, it will speedup your tomcat run)
- Now run the application. You should be able to curl `http://localhost:8080/dhruva_server_war_exploded/api/v1/greetings/testuser`
## References

- [Dhruva Wiki Home](https://confluence-eng-gpk2.cisco.com/conf/display/DHRUVA/Dhruva+-+Next+Gen+SIP+Edge)
- [Meetings Platform documentation](https://sqbu-github.cisco.com/pages/WebexPlatform/docs/)

# Docker build (WIP)

Login to artifactory using `docker login dockerhub.cisco.com`

## Building the war files

This step is optional: your IDE should already be able to build the war file with a `mvn verify`.

`docker run --rm -v `pwd`:/opt/code -w /opt/code -e JAVA_VERSION=8 containers.cisco.com/ayogalin/maven-builder:one sh -c "/setenv.sh; java -version; /usr/share/maven/bin/mvn -Dspotbugs.enabled -U --batch-mode -T2C -Dmaven.test.failure.ignore -Dmaven.test.skip=true -DauthNG.statisticsOutput package"`

Change `JAVA_VERSION` to `8` or `11` based on what you need.

This outputs `dhruva-server.war` in the `server/target` folder.

## Building the docker image

`docker build -t dhruva:invalidServiceTag -f docker/dhruva.Dockerfile .`

## Running the docker image

`./docker/local/run-dhruva-in-local.sh`

This will create an image called `dhruva`
You can test the http ping API using a command like this:

`curl localhost:18980/api/v1/ping`.

## TODO

- Add/expose SIP ports.
- IDE instructions.
- Integrate with Jenkins.

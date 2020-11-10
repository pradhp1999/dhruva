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
- Now run the application. You should be able to curl `http://localhost:8080/dhruva_server_war_exploded/api/v1/ping`

### Configuring Dhruva for Calls with TLS
- Provide listen points for Dhruva. Since we are running Dhruva in Tomcat in IntelliJ (more details in 'Running in Tomcat in Intellij IDE'), 
pass the required config as environment variables.
    - For this, go to 'Services' in IDE. You will find the tomcat that you have configured earlier. Right click on that tomcat server 
    and choose 'Edit Configuration'.
    - Choose 'Startup/Connection' section. 
    - Enable checkbox '_Pass environment variables_'
    - Provide listen points as below
        - In 'Name' -> `sipListenPoints`
        - In 'Value' -> `[{
                        	"name": "<networkName>",
                        	"hostIPAddress": "<IP of machine where Dhruva runs",
                        	"transport": "<UDP/TCP/TLS",
                        	"port": <port>,
                        	"recordRoute": true
                        }] `
- To make UDP/TCP calls, it is same as CP. 
- To make TLS calls
    - Following environment variables have to bet set in Intellij, with certificate and private key file contents as its value respectively.
    1. `sipCertificate`
    2. `sipPrivateKey`
    
    ### Using SIPp with TLS1.2
    - Configure sipp with TLSv1.2 (as Dhruva supports that). _Note:_ If sipp is already configured with v1.0,
     and tries to connect with it, Dhruva throws an exception.  
    - You can use sipp from either CentOs/Mac. 
    - Following are the steps to configure sipp(for TLSv1.2). This makes use of v1.2 enabled and patched sipp code in ucre github repo.
        1. ##### CentOS
            `sudo yum install automake ncurses-devel -y`
           ##### MacOS
            `sudo brew install automake ncurses -y`
        2. `git clone git@sqbu-github.cisco.com:ucre/sipp_tls.git` (contains TLSv1.2 enabled sipp code)
        3. `cd sipp_tls/sipp-3.5.2` <br/> 
           `autoreconf -ivf` <br/> 
           `./configure --with-openssl` _Note_: For MacOS this step could fail if compiler is unable to find libraries in standard locations, pass LDFLAGS and CPPFLAGS with correct paths for this step to pass. <br/> 
           `make`
     
    **Reference:** [Sipp for TLS](http://sipp.sourceforge.net/doc3.3/reference.html) - look for '_Installing Sipp_' section
    for manually enabling TLS1.2
           
## References

- [Dhruva Wiki Home](https://confluence-eng-gpk2.cisco.com/conf/display/DHRUVA/Dhruva+-+Next+Gen+SIP+Edge)
- [Meetings Platform documentation](https://sqbu-github.cisco.com/pages/WebexPlatform/docs/)

# Docker build (WIP)

Login to artifactory using `docker login dockerhub.cisco.com`

## Building the war files

This step is optional: your IDE should already be able to build the war file with a `mvn verify`.

`docker run --rm -v `pwd`:/opt/code -w /opt/code containers.cisco.com/ayogalin/maven-builder:one sh -c "/setenv.sh; java -version; /usr/share/maven/bin/mvn -Dspotbugs.enabled -U --batch-mode -T2C -Dmaven.test.failure.ignore -Dmaven.test.skip=true -DauthNG.statisticsOutput package"`

In `docker/env.sh`, change `JAVA_VERSION` to `8` or `11` based on what you need.

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

# Dhruva, a SIP Edge service

Dhruva is a SIP Edge service that routes CMR SIP traffic from the Enterprise to the L2SIP in the correct Meetings cluster.

It is deployed in Meetings Platform (meet-PaaS).

## Getting Started

### Prerequisites
- Redis is needed to run Integration tests.
- Clone the repo in your IDE (IntelliJ Ultimate recommended (see below)).
- Setup [secure access to Cisco's artifactory](https://sqbu-github.cisco.com/pages/WebexSquared/docs/DeveloperTools/maven.html).

#### Intellij Ultimate

- Use [this invitation for IntelliJ IDEA Ultimate by lbogard@cisco.com](https://account.jetbrains.com/a/4007hmtf) and create an account with your Cisco Email address.
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
- In VM options field enter: `-Xmx2048m -Xms1024m`, and `-DexternalUrlProtocol=http`.
- Now go to the deployment tab
- Use + button to add the artifact `dhruva-server-exploded.war` (pick one with 'exploded' word in name, it will speedup your tomcat run)
- Now run the application. You should be able to curl `http://localhost:8080/dhruva_server_war_exploded/api/v1/greetings/testuser`
## References

- [Dhruva Wiki Home](https://wiki.cisco.com/display/WX2/Dhruva+-+Next+Gen+SIP+Edge)
- [Meetings Platform documentation](https://sqbu-github.cisco.com/pages/WebexPlatform/docs/)

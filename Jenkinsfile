#!groovy
library identifier: 'sparkPipeline', changelog: false

/**
 * sparkPipeline constructs a typical spark pipeline. Many options are available to alter the pipeline.
 * For documentation and a list of options, see:
 *     https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/vars/sparkPipeline.txt
 */
sparkPipeline {

    notifySparkRoomId = 'Y2lzY29zcGFyazovL3VzL1JPT00vNDU5NWUzNTAtZjYyMy0xMWU5LThmMWQtYmY3OTJhYmQ3MzY0'

    build = { services ->
        this.sh "mvn versions:set -DnewVersion=${this.env.BUILD_VERSION} && mvn -Dmaven.test.failure.ignore verify"
        this.step([$class: 'JacocoPublisher', changeBuildStatus: true, classPattern: 'server/target/classes,client/target/classes', execPattern: '**/target/**.exec', minimumInstructionCoverage: '1'])
    }

    /*
    * This next bit is temporary. Once we have the pipeline up in meet PaaS, revert to
    * default. Without these changes, the 'default' postDeployTests are called, and since
    * we've currently not deployed in any environment, the tests fail and we never get a
    * successful build that ImageBuilder can pick up.
    */
    integration.postDeployTests = []
    integration.runConsumerTests = false
    integration.consumerTestsPromptAction = false

}

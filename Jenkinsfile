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

    postArchiveArtifacts = { services ->
        if(this.isMasterBranch() || this.isHotfixBranch()) {
            this.initiateImageBuilderInMeetPaas()
        }
    }

}

//Triggers a remote job in Meet PaaS jenkins to download artifacts from this pipeline
def initiateImageBuilderInMeetPaas() {
    echo "target_build:" + env.BUILD_NUMBER
    echo "artifact_url:" + env.JOB_URL

    try {
        def result
        result = retryBuild job: 'team/dhruva/MeetPaaS/router-publish-job-meet-paas',
                parameters: [string(name: 'target_build', value: BUILD_NUMBER),
                             string(name: 'artifact_url', value: JOB_URL),
                             string(name: 'service', value: 'dhruva')],
                promptForAction : false, indirect_build : true

        if (result.result == 'SUCCESS') {
            echo "SUCCESS triggering the image publisher router job"
        }
        // Fail the current build in case the publish to meet paas fails.
        // No point in continuing the deployment, since Dhruva is dedicated for meet-paas.
        if (result.result == 'FAILURE') {
            currentBuild.result = 'FAILURE'
            echo "ERROR triggering image builder router job"
            return
        }
    } catch (Exception ex) {
        echo "ERROR: Could not trigger the image publisher router job"
    }
}
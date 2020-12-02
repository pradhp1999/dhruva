#!groovy
// Get the common library.
@Library('sparkPipeline') _

/**
 * sparkPipeline constructs a typical spark pipeline. Many options are available to alter the pipeline.
 * For documentation and a list of options, see:
 *     https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/vars/sparkPipeline.txt
 */
sparkPipeline {

    builder = 'SPARK_BUILDER_JAVA8'

    notifySparkRoomId = 'Y2lzY29zcGFyazovL3VzL1JPT00vNDU5NWUzNTAtZjYyMy0xMWU5LThmMWQtYmY3OTJhYmQ3MzY0'

    build = { services ->
        this.pyvenv(requirements: 'requirements.txt', verbose: true, suffix: '.linting') {
            this.sh "yamllint -d '{extends: default, rules: {line-length: {max: 2048}, indentation: {spaces: 2, indent-sequences: consistent}, document-start: disable}}' config/*tpl"
        }

        // First we run yamllint over all the files in the config folder:
        this.sh "JAVA_VERSION=8 mvn versions:set -DnewVersion=${this.env.BUILD_VERSION} && JAVA_VERSION=8 mvn -Dmaven.test.failure.ignore clean verify"


        this.step([$class: 'JacocoPublisher', changeBuildStatus: true, classPattern: 'server/target/classes,client/target/classes', execPattern: '**/*.exec', minimumInstructionCoverage: '1'])

       //this.step([$class: 'JacocoPublisher', changeBuildStatus: true, classPattern: 'server/target/classes,client/target/classes', execPattern: '**/target/**.exec', minimumInstructionCoverage: '1'])
        if (this.isMasterBranch() || this.isHotfixBranch()) {
            // Archive the Dockerfiles (and env.sh) in the docker/ folder
            // so that downstream jobs can pull and build the images
            this.archiveArtifacts artifacts: 'docker/*'
        }
    }

    // fail the build if Static Analysis fails
    postBuild = { services ->
        // Report SpotBugs static analysis warnings (also sets build result on failure)
        this.findbugs pattern: '**/spotbugsXml.xml', failedTotalAll: '0'
        this.failBuildIfUnsuccessfulBuildResult("ERROR: Failed SpotBugs static analysis")
    }

    /* Choices: For now, in meetpaas-int, we always want to deploy directly,
    * without asking for a choice. In Prod, we always want to skip (since dhruva
    * isn't there yet)
    */
    integration.deployMode = "deploy"
    production.deployMode = "skip"

    /* Push artifacts to corona and run scans */
    integration.runSecurityScans = true

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

def failBuildIfUnsuccessfulBuildResult(message) {
    // Check for UNSTABLE/FAILURE build result or any result other than "SUCCESS" (or null)
    if (currentBuild.result != null && currentBuild.result != 'SUCCESS') {
        this.failBuild(message)
    }
}

def failBuild(message) {
    echo message
    throw new SparkException(message)
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
                             string(name: 'service', value: 'dhruva'),
                             string(name: 'filters', value: '^.*.war$ ^.*.microservice$ ^.*microservice.yml$ ^.*migration.jar$ ^.*migration-shaded.jar$ ^.*integration.jar ^.*integration-tests.jar ^.*integration-tests.sh ^.*integration-bin.tar.gz  ^.*Dockerfile  ^.*env.sh')],
                promptForAction : false, indirect_build : true

        if (result.result == 'SUCCESS') {
            echo "SUCCESS triggering the image publisher router job"
        }
        // Fail the current build in case the publish to meet paas fails.
        // No point in continuing the deployment, since Dhruva is dedicated for meet-paas.
        if (result.result == 'FAILURE') {
            currentBuild.result = 'FAILURE'
            echo "ERROR triggering image builder router job"
            failBuild("Failed to build and publish image for meetpaas.")
        }
    } catch (Exception ex) {
        echo "ERROR: Could not trigger the image publisher router job"
        throw ex
    }
}

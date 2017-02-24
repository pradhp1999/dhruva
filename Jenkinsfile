#!groovy
@Library('sparkPipeline@extractHelloWorld') _

/**
 * This is a simple pipeline for hello-world. It may work for your service or it may not.
 * Browse https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/samples for more.
 *
 * This pipeline performs a `mvn verify` at the root level after using the `versions:set`
 * maven goal to update the POMs with a version generated from the current build number.
 *
 * Artifacts are then archived. If this is a PR or branch build, the pipeline stops there.
 * If this is a build off of master, then we proceed to the deploy stages, including publishing
 * to maven and publishing our documentation, which, of course, is always up-to-date, right?
 *
 * For each deploy stage, the pipeline is paused until is approved. Once approved, the deploy
 * to the environment is triggered. The actual deploy mechanism is triggered here, but handled
 * by platform controlled jobs because they expose the credentials to the environments.
 *
 * For more information about how to construct a Jenkinsfile, please consult these resources:
 *   - Ask Pipeline room in the Sparkans Team.
 *   - Pipeline Reference:
 *       https://sqbu-jenkins.cisco.com:8443/job/team/job/hello-world/job/pipeline/pipeline-syntax/
 *   - Getting started with Pipeline: https://jenkins.io/doc/
 *   - Examples: https://jenkins.io/doc/pipeline/examples/
 *   - Steps reference for sparkPipeline library: https://sqbu-github.cisco.com/WebExSquared/pipeline
 */

pipelineProperties(
    name: 'hello-world',
    numToKeep: 10,

    /*
     * The room Id refers to the "HelloWorld Pipeline" room, and notifies it with some details. To set this
     * up, create a room in Spark, and then go to https://developer.ciscospark.com/endpoint-rooms-get.html,
     * enable test mode, and then enter the name of your newly-created room to find the room Id. Put that
     * here. Alternatively, if you don't want this, simply remove it and the associated "notify" calls.
     */
    notifySparkRoomId: 'Y2lzY29zcGFyazovL3VzL1JPT00vZmIyYWYyYTAtZmFkNi0xMWU2LWE4MzctZmQ5MjFlYjIzZDA5'
)

buildStage(services: ['redis:3']) {
    sh "mvn versions:set -DnewVersion=${env.BUILD_VERSION}"
    sh 'mvn -Dmaven.test.failure.ignore verify'

    junit '**/target/**/TEST-*.xml'
    archive 'target/microservice.yml'
    archive 'server/target/*.war'

    archiveMavenArtifacts()
    archivePubHub()
}

if (isMasterBranch()) {
    approveStage('Deploy to Test', submitter: 'squared') {
        parallel(
            integration: { deploy 'integration' },
            loadtest: { deploy 'loadtest' }
        )
    }

    approveStage('Deploy to Production', submitter: 'squared') {
        deploy 'production'
    }

    publish()
}

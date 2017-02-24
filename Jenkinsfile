#!groovy
@Library('sparkPipeline') _

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

pipelineProperties numToKeep: 10
pipelineName = 'hello-world'

/*
 * The room Id refers to the "HelloWorld Pipeline" room, and notifies it with some details. To set this
 * up, create a room in Spark, and then go to https://developer.ciscospark.com/endpoint-rooms-get.html,
 * enable test mode, and then enter the name of your newly-created room to find the room Id. Put that
 * here. Alternatively, if you don't want this, simply remove it and the associated "notify" calls.
 */
roomId = 'Y2lzY29zcGFyazovL3VzL1JPT00vY2ZhODk0NDAtZmE5ZC0xMWU2LWJjNzAtYjUwZTY3MzgxYjll'

try {
    if (isMasterBranch()) {
        notify(roomId, "Build started.")
    }

    buildStage(pipelineName, services: ['redis:3']) {
        sh "mvn versions:set -DnewVersion=${env.BUILD_VERSION}"
        sh 'mvn -Dmaven.test.failure.ignore verify'

        junit '**/target/**/TEST-*.xml'
        archive 'target/microservice.yml'
        archive 'server/target/*.war'

        archiveMavenArtifacts()
        archivePubHub()
    }

    if (isMasterBranch()) {
        notify(roomId, "Deploy to Test?") // Yeah, we could probably add this into approveStage at some point ...
        approveStage('Deploy to Test', submitter: 'squared') {
            notify(roomId, "Deploying to integration...")
            parallel(
                    integration: {
                        try {
                            timeout(time: 10, unit: 'MINUTES') { deploy 'integration' }
                        } catch (e) {
                            notify(roomId, "Fatal: there was a problem deploying to `integration` - timeout reachced")
                            throw e
                        }
                    },
                    loadtest: {
                        try {
                            timeout(time: 10, unit: 'MINUTES') {
                                deploy 'loadtest'
                            }
                        } catch (e) {
                            notify(roomId, "Warning: there was a problem deploying to `loadtest` - timeout reached")
                            timeout(time: 10, unit: 'MINUTES') { input message: 'Loadtest deploy has failed, Continue?' }
                        }
                    }
            )
            notify(roomId, "Deployed to integration")
        }

        notify(roomId, "Deploy to Production?")
        approveStage('Deploy to Production', submitter: 'squared') {
            notify(roomId, "Deploying to Production")
            try {
                timeout(time: 20, unit: 'MINUTES') { deploy 'production' }
            } catch(e) {
                notify(roomId, 'Fatal: there was a problem deploying to `production` - timeout reached')
                throw e
            }
            notify(roomId, "Deployed to Production")
        }

        notify(roomId, "Publishing artifacts")
        publish()
        notify(roomId, "Artifacts published. Build finished.")
    }
} catch (e) {
    if (isMasterBranch()) {
        notify(roomId, "Pipeline aborted")
    }
    throw e
}



def notify(String roomId, String markdown) {
    sparkSend(roomId: roomId, markdown: "Build [#${currentBuild.number}](${env.BUILD_URL}console): ${markdown.replaceAll("'", "`")}")
}


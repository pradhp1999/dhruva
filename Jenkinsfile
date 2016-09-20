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
 *       https://sqbu-jenkins.cisco.com:8443/job/team/job/hello-world/job/pipeline-hello-world/pipeline-syntax/
 *   - Getting started with Pipeline: https://jenkins.io/doc/
 *   - Examples: https://jenkins.io/doc/pipeline/examples/
 *   - Steps reference for sparkPipeline library: https://sqbu-github.cisco.com/WebExSquared/pipeline
 */

nodeWith(stage: 'Build', services: ['redis:3']) {
    checkout scm
    inititializeEnv('hello-world')

    sh "mvn versions:set -DnewVersion=${env.BUILD_VERSION}"
    sh 'mvn verify -Dmaven.test.failure.ignore'

    junit '**/target/surefire-reports/TEST-*.xml'
    archive 'target/microservice.yml'
    archive 'client/target/*.jar'
    archive 'server/target/*.war'

    stash name: 'docs', includes: 'docs/'

    // Save artifacts and poms that will later be published to a maven repository.
    publishableArtifacts {
        artifacts << [file: 'client/target/*.jar', pom: 'client/pom.xml']
        artifacts << [file: 'pom.xml', pom: 'pom.xml']
    }
}

if (isMasterBranch()) {
    approveStage('Deploy to integration', submitter: 'squared') {
        deploy 'integration'
    }

    approveStage('Deploy to production', submitter: 'squared') {
        deploy 'production'
    }

    nodeWith(stage: 'Publish docs') {
        unstash 'docs'
        publishDocs name: 'Hello World', includes: 'docs/*'
    }

    publishArtifacts
}

/**
 * Pipeline TODO:
 *   - Rename artifact jobs to platform/pipeline/team/${serviceName}/${targetEnvironment}-artifact
 *   - Rename deploy jobs to platform/pipeline/team/${serviceName}/${targetEnvironment}-deploy
 *   - Have artifact jobs always archive from main pipeline and not other artifact jobs
 *   - What's the purpose of update-version-hello-world job?
 *   - Understand the security concern over maven deploy jobs, but currently have massive security holes in them
 *     - Can inject arbitrary code execution in with commandLines or via pom exec plugins
 *     - Need to not rebuild artifacts. Instead, rely on artifacts archived by main job
 *   - Still very difficult to follow pipeline flow, especially as things go through vortex with dynamically created jobs
 *   - Expose nodeWith service hostnames to body
 *   - Executable JARs.
 */

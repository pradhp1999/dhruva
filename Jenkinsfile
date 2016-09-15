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
 * If this is a build off of master, then we proceed to the deploy stages.
 *
 * For each deploy stage, the pipeline is paused until each stage is approved. Once approved,
 * the deploy to the environment is triggered.
 *
 * For more information about how to construct a Jenkinsfile, please consult these resources:
 *   - Steps reference documentation: https://jenkins.io/doc/pipeline/steps/
 *   - Getting started with Pipeline: https://jenkins.io/doc/
 *   - Examples: https://jenkins.io/doc/pipeline/examples/
 *   - Steps reference for sparkPipeline library: https://sqbu-github.cisco.com/WebExSquared/pipeline
 */

nodeWith(stage: 'Build', services: ['cassandra:2.2', 'redis:3']) {
    checkout scm

    sh "mvn versions:set -DnewVersion=1.1.${currentBuild.number}"
    sh 'mvn verify -Dmaven.test.failure.ignore'

    junit '**/target/surefire-reports/TEST-*.xml'
    archive 'target/microservice.yml'
    archive 'client/target/*.jar'
    archive 'server/target/*.war'
}

if (isMasterBranch()) {
    approveStage('Deploy to integration', submitter: 'squared') {
        deploy 'integration'
    }

    approveStage('Deploy to production', submitter: 'squared') {
        deploy 'production'
    }
}

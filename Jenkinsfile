#!groovy
@Library('sparkPipeline') _

// This is a simple pipeline for hello-world. It may work for your service or it may not.
// Browse https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/samples for more.

stage 'Build'
nodeWith(['cassandra:2.2', 'redis:3']) {
    checkout scm

    sh 'mvn versions:set -DnewVersion=1.0.${TARGET_VERSION}'
    sh 'mvn verify -Dmaven.test.failure.ignore'

    archiveTestResults '**/target/surefire-reports/TEST-*.xml'
    archive 'target/microservice.yml'
    archive 'client/target/*.jar'
    archive 'server/target/*.war'
}

if (isMasterBranch()) {
    approveStage 'deploy to integration', submitter: 'squared'
    deploy 'integration'

    approveStage 'deploy to production', submitter: 'squared'
    deploy 'production'
}

#!groovy

// Set the target version information.
def TARGET_VERSION = "1.1." + currentBuild.number
env.TARGET_VERSION = TARGET_VERSION

// Set the title (beyond the build number) that will be used.
// Set the display name of the build to include both build number and version number.
def TARGET_TITLE = "v" + TARGET_VERSION
currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE

// Use a SPARK_BUILDER node to perform the build. Only allocate the node for as long
// as necessary; the build and isolated tests.
node('SPARK_BUILDER') {
    try {
        // Obtain the current code base, then extract commit information.
        stage 'Get Code'
        GIT_URL = 'git@sqbu-github.cisco.com:WebExSquared/hello-world.git'
        env.GIT_URL = GIT_URL
        checkout poll: false,
                scm: [$class: 'GitSCM', branches: [[name: '*/master']],
                      doGenerateSubmoduleConfigurations: false,
                      extensions: [[$class: 'CleanCheckout']],
                      submoduleCfg: [],
                      userRemoteConfigs: [[credentialsId: env.GIT_MAIN_CREDENTIALS, url: GIT_URL]]]

        sh 'git rev-parse --abbrev-ref HEAD > GIT_BRANCH'
        GIT_BRANCH = readFile('GIT_BRANCH').trim()
        // The following should work, but it always returns HEAD
        // env.GIT_BRANCH = GIT_BRANCH
        env.GIT_BRANCH = 'master'
        sh 'git rev-parse HEAD > GIT_COMMIT'
        GIT_COMMIT = readFile('GIT_COMMIT').trim()
        env.GIT_COMMIT = GIT_COMMIT

        // Perform the build and test part.
        stage 'Build and Test'
        sh '''#!/bin/bash -ex

        #${SPARK_BUILDER_SUPPORT_DIR}/setup-support-services.sh --services=cassandra,redis

        #export cassandraHostAddress=local-spark-cassandra
        #export redisHost=local-spark-redis
        #export _JAVA_OPTIONS="${_JAVA_OPTIONS} -DcassandraHostAddress=${cassandraHostAddress}"
        mvn versions:set -DnewVersion=${TARGET_VERSION}
        mvn clean package
        '''

        // From the build - if it works - archive up the files that are needed for downstream
        // builds. This should include the microservice file and all files referenced by it.
        archive 'target/microservice.yml'
        archive 'server/target/hello-world-server.war'
    } finally {
        deleteDir()
    }
}

artifact_integration = env.JOB_ARCHIVE_GOOD_TO_GO
artifact_production = env.JOB_ARCHIVE_DEPLOY_TO_PROD
pipeline_integration = env.JOB_DEPLOY_INTEGRATION
pipeline_production = env.JOB_DEPLOY_PRODUCTION

stage 'Promote to Integration'
checkpoint 'integration'
currentBuild.description = TARGET_TITLE
timeout(time: 30, unit: 'MINUTES') {
    def integration_input = input(id: 'promote-to-integration',
            message: 'Promote to Integration?',
            ok: 'Deploy', submitter: 'squared')
}

// Store artifacts
build_integration = build job: artifact_integration,
        parameters: [[$class: 'RunParameterValue', description: '', name: 'UPSTREAM', runId: env.JOB_NAME + '#' + env.BUILD_NUMBER]]
upstream_integration = artifact_integration + '#' + build_integration.number

// Run Integration
build job: pipeline_integration,
        parameters: [[$class: 'RunParameterValue', description: '', name: 'UPSTREAM', runId: upstream_integration]]


stage 'Promote to Production'
checkpoint 'production'
currentBuild.description = TARGET_TITLE
timeout(time: 30, unit: 'MINUTES') {
    def production_input = input(id: 'promote-to-production',
            message: 'Promote to Production?',
            ok: 'Deploy', submitter: 'squared')
}

// Store artifacts
build_production = build job: artifact_production,
        parameters: [[$class: 'RunParameterValue', description: '', name: 'UPSTREAM', runId: upstream_integration]]
upstream_production = artifact_production + '#' + build_production.number

// Run Production
build job: pipeline_production
parameters: [[$class: 'RunParameterValue', description: '', name: 'UPSTREAM', runId: upstream_production]]


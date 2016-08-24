#!groovy

def TARGET_VERSION = "1.1." + currentBuild.number
env.TARGET_VERSION = TARGET_VERSION
def TARGET_TITLE = "v" + TARGET_VERSION
currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE

node('SPARK_BUILDER') {
    try {
        stage 'Get Code'
        checkout poll: false,
                scm: [$class: 'GitSCM', branches: [[name: '*/master']],
                      doGenerateSubmoduleConfigurations: false,
                      extensions: [[$class: 'CleanCheckout']],
                      submoduleCfg: [],
                      userRemoteConfigs: [[credentialsId: env.GIT_MAIN_CREDENTIALS, url: 'git@sqbu-github.cisco.com:WebExSquared/hello-world.git']]]

        sh '''#!/bin/bash -e
        echo "# Build Properties" > build.properties
        echo "PUBLISH_GIT_URL=${GIT_URL}" >> build.properties
        echo "PUBLISH_GIT_BRANCH=${GIT_BRANCH}" >> build.properties
        echo "PUBLISH_GIT_COMMIT=${GIT_COMMIT}" >> build.properties
        echo "PUBLISH_VERSION=${TARGET_VERSION}" >> build.properties
        '''
        archive 'build.properties'


        stage 'Build and Test'
        sh '''#!/bin/bash -ex

        #${SPARK_BUILDER_SUPPORT_DIR}/setup-support-services.sh --services=cassandra,redis

        #export cassandraHostAddress=local-spark-cassandra
        #export redisHost=local-spark-redis
        #export _JAVA_OPTIONS="${_JAVA_OPTIONS} -DcassandraHostAddress=${cassandraHostAddress}"
        mvn versions:set -DnewVersion=${TARGET_VERSION}
        mvn clean package
        '''

        archive 'server/target/hello-world-server.war'
        archive 'target/microservice.yml'

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

currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE

node('SPARK_BUILDER') {
    try {
        stage 'Get Code'
        checkout poll: false,
                scm: [$class: 'GitSCM', branches: [[name: '*/master']],
                      doGenerateSubmoduleConfigurations: false,
                      extensions: [[$class: 'CleanCheckout']],
                      submoduleCfg: [],
                      userRemoteConfigs: [[credentialsId: env.GIT_MAIN_CREDENTIALS, url: 'git@sqbu-github.cisco.com:WebExSquared/hello-world.git']]]

        sh '''#!/bin/bash -e
        echo "# Build Properties" > build.properties
        echo "PUBLISH_GIT_URL=${GIT_URL}" >> build.properties
        echo "PUBLISH_GIT_BRANCH=${GIT_BRANCH}" >> build.properties
        echo "PUBLISH_GIT_COMMIT=${GIT_COMMIT}" >> build.properties
        echo "PUBLISH_VERSION=${TARGET_VERSION}" >> build.properties
        '''
        archive 'build.properties'


        stage 'Build and Test'
        sh '''#!/bin/bash -ex

        #${SPARK_BUILDER_SUPPORT_DIR}/setup-support-services.sh --services=cassandra,redis

        #export cassandraHostAddress=local-spark-cassandra
        #export redisHost=local-spark-redis
        #export _JAVA_OPTIONS="${_JAVA_OPTIONS} -DcassandraHostAddress=${cassandraHostAddress}"
        mvn versions:set -DnewVersion=${TARGET_VERSION}
        mvn clean package
        '''

        archive 'server/target/hello-world-server.war'
        archive 'target/microservice.yml'

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
currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE
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
currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE
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


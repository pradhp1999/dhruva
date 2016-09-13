#!groovy

/*
DO NOT, DO NOT, DO NOT, DO NOT, DO NOT, DO NOT, DO NOT, DO NOT, DO NOT -
copy this JenkinsFile and use it as baseline for your pipeline.

Instead, go to https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/samples
and use one of the samples from there.

 */

timestamps {
    env.PIPELINE = 'hello-world'
    is_pr_build = (env.CHANGE_ID != null)
    is_branch_build = (env.BRANCH_NAME != null) && (env.BRANCH_NAME != 'master')

    def TARGET_VERSION = "1.1." + currentBuild.number
    env.TARGET_VERSION = TARGET_VERSION

    // TODO: Replace with Shared Librarys
    def common_pipeline = fileLoader.fromGit('sparks.groovy', 'git@sqbu-github.cisco.com:WebExSquared/pipeline.git',
            'master', env.PIPELINE_CREDENTIALS, 'BASIC_SLAVE')

    def TARGET_TITLE = "v" + TARGET_VERSION
    env.TARGET_TITLE = TARGET_TITLE
    currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE
    node('SPARK_BUILDER') {
        try {
            // Obtain the current code base, then extract commit information.
            stage 'Get Code'
            checkout scm
            common_pipeline.load_git_info('git@sqbu-github.cisco.com:WebExSquared/hello-world.git')

            // Perform the build and test part.
            stage 'Build and Test'
            sh '''#!/bin/bash -ex
            mvn versions:set -DnewVersion=${TARGET_VERSION}
            mvn verify
            '''

            // Archive elements
            archive 'target/microservice.yml'
            archive 'server/target/hello-world-server.war'
        } finally {
            deleteDir()
        }
    }

    // Only run master builds through the deploys
    if (!is_branch_build) {
        common_pipeline.go_to_integration()
        common_pipeline.go_to_production()
    }
}
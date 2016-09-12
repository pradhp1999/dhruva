#!groovy

// Timestamp the whole thing, so on any given line, we know how long it takes
timestamps {
    // Set some common values.
    env.PIPELINE = 'hello-world'

    // If this is a gated build - from Gauntlet or other mechanism, then set
    // gated_build = true
    gated_build = false

    // Flag indicating if this is pull request build.
    is_pr_build = (env.CHANGE_ID != null)

    // Flag indicating if this is a branch build. NOTE: PR builds are a form of branch build.
    is_branch_build = (env.BRANCH_NAME != null) && (env.BRANCH_NAME != 'master')

    // Set the target version information. This approach means that every build
    // will roll the version number - even if the build is bad. This way,
    // every unique build is uniquely identified.
    def TARGET_VERSION = "1.1." + currentBuild.number
    env.TARGET_VERSION = TARGET_VERSION

    // Download the common pipeline libraries. They'll be used later in this pipeline.
    // NOTE: This allocates a separate slave to obtain the file.
    def common_pipeline = fileLoader.fromGit('sparks.groovy', 'git@sqbu-github.cisco.com:WebExSquared/pipeline.git',
            'master', env.PIPELINE_CREDENTIALS, 'BASIC_SLAVE')

    // Set the title (beyond the build number) that will be used.
    // Set the display name of the build to include both build number and version number.
    // We want to store this version number because we want to see it on future builds
    // triggered from a checkpoint.
    def TARGET_TITLE = "v" + TARGET_VERSION
    env.TARGET_TITLE = TARGET_TITLE
    currentBuild.displayName = '#' + currentBuild.number + ' ' + TARGET_TITLE

    // Use a SPARK_BUILDER node to perform the build. Only allocate the node for as long
    // as necessary; for the build an local tests.
    // If your team needs a different build machine, or already has one, specify it here.
    node('SPARK_BUILDER') {
        try {
            // Obtain the current code base, then extract commit information.
            stage 'Get Code'
            checkout scm

            // This will determine any GIT values that might not yet be known. Also, populates the
            // environment with the GIT values used by the maven build process.
            common_pipeline.load_git_info('git@sqbu-github.cisco.com:WebExSquared/hello-world.git')

            // Perform the build and test part.
            // NOTE - UNDER CONSTRUCTION: SPARK_BUILDER nodes are rigged with docker host and several supporting
            // services that can be started as containers.
            stage 'Build and Test'
            sh '''#!/bin/bash -ex

            #${SPARK_BUILDER_SUPPORT_DIR}/setup-support-services.sh --services=cassandra,redis
            #export cassandraHostAddress=local-spark-cassandra
            #export redisHost=local-spark-redis
            #export _JAVA_OPTIONS="${_JAVA_OPTIONS} -DcassandraHostAddress=${cassandraHostAddress}"

            mvn versions:set -DnewVersion=${TARGET_VERSION}
            mvn verify
            '''

            // From the build - if it works - archive up the files that are needed for downstream
            // builds. This should include the microservice file and all files referenced by it.
            // Additional files can be archived if desired - logs, test results.  But at the minimum, the ones
            // referred to in the microservice file must be archived.
            archive 'target/microservice.yml'
            archive 'server/target/hello-world-server.war'
        } finally {
            deleteDir()
        }
    }

    // If this is a gated build, and not running on a Branch or PR, then this might be a good place to merge
    // the code changes to master. Or you can move the code later and merge it after it has passed a deploy-and-test
    // in Integration
    if (gated_build) {
        echo "TODO: Need to merge changes"
    }

    // Only run master builds through the deploys
    if (!is_branch_build) {
        // The following will perform a default go-to-integration action. This will trigger an action that must be manually accepted
        // before the pipeline will continue to run.
        common_pipeline.go_to_integration()

        // The following will perform a default go-to-production action. This will trigger an action that must be manually accepted
        // before the pipeline will continue to run.
        common_pipeline.go_to_production()
    }
}
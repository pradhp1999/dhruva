#!groovy
/*
This Jenkinsfile invokes a python script to run integration-tests in docker container

We use some functions from WebexSquared/pipeline, e.g. notifyPipelineRoom
*/
@Library(['kubedPipeline', 'sparkPipeline']) _

//Being a multi-branch pipeline job, adding parameters via properties is how we can parameterize this job. It's not possible to create from UI
properties([
        parameters ([
                string(name: 'TAG', defaultValue: '34', description: 'Tag of the docker image containers.cisco.com/edge_group/dhruva-integration-tests'),
                choice(name: 'CLUSTER', choices: ['wsjcint01', 'wdfwint01'], description: 'Choose a cluster'),
                string(name: 'UDP_PORT', defaultValue: '11500', description: 'UDP Port'),
                string(name: 'TLS_PORT', defaultValue: '11501', description: 'TLS Port'),
        ])
])
node() {
    lock("dhruva") {
        try {

            // ***** CREDENTIALS USED IN IT-JENKINS *****

            // Bot to talk to Teams rooms
            env.PIPELINE_SPARK_BOT_API_TOKEN = "BeechNotifyBot"
            // github token to talk to sqbu-github.cisco.com and notify build status
            env.WBXKUBED_GIT_CREDENTIALS = 'arunsrin-sqbu-github-authtoken'
            // bot token to talk to containers.cisco.com
            REGISTRY_CREDENTIALS = 'dhruva_ccc_bot'

            // ***** END CREDENTIALS *****

            // 'Dhruva Build & Deploy Notifications - IT Jenkins' room
            notifySparkRoomId = 'Y2lzY29zcGFyazovL3VzL1JPT00vMDc1NDVhZDAtMWYyMi0xMWViLWIxZjgtMzdmOGEzNjNhOGQ5'

            stage('Checkout') {
                cleanWs notFailBuild: true
                checkout scm
            }

            stage('initializeEnv') {
                env.GIT_BRANCH = env.BRANCH_NAME ?: 'master'
                env.BUILD_NAME = env.GIT_BRANCH == 'master' ? env.BUILD_ID : env.GIT_BRANCH + "-" + env.BUILD_NUMBER
                env.GIT_COMMIT_FULL = sh(returnStdout: true, script: 'git rev-parse HEAD || echo na').trim()
                env.GIT_COMMIT_AUTHOR = sh(returnStdout: true, script: "git show -s --format='format:%ae' ${env.GIT_COMMIT_FULL} || echo na").trim()
                // TODO will be nice to have a BUILD_ID+TIMESTAMP+GIT_COMMIT_ID here instead of just BUILD_ID
                setBuildName(env.BUILD_NAME)
                if (env.CHANGE_AUTHOR && !env.CHANGE_AUTHOR_EMAIL) {
                    env.CHANGE_AUTHOR_EMAIL = env.CHANGE_AUTHOR + '@cisco.com'
                }

                if (env.CHANGE_ID && !env.CHANGE_AUTHOR_EMAIL) {
                    env.CHANGE_AUTHOR_EMAIL = env.GIT_COMMIT_AUTHOR
                }
                if (env.CHANGE_ID == null) { // CHANGE_ID will be null if there is no Pull Request
                    // Announce in build notifications room
                    notifyPipelineRoom("integration-tests: Build started.", roomId: notifySparkRoomId)
                } else {
                    // 1:1 notification to PR owner
                    notifyPipelineRoom("integration-tests: Build started.", toPersonEmail: env.CHANGE_AUTHOR_EMAIL)
                }
            }
            stage('build') {
                def data = readYaml(file: "microservice-itjenkins.yml")
                def targetArtifacts = data['wbx3']['artifacts']
                targetArtifacts.each { artifactDef ->
                    def dockerDef = artifactDef.value
                    def registry = dockerDef["dockerRegistry"]
                    if (REGISTRY_CREDENTIALS) {
                        //Credentials which enable docker to access containers.cisco.com
                        docker.withRegistry(registry, REGISTRY_CREDENTIALS) {
                            sh "env"
                            //Failsafe- incase container already exists, remove it
                            sh "docker rm dhruva-integration-test-${params.TAG} || true"
                            //Calls python script to run integration tests with 2 parameters that can be changed via Build-with-Parameters option while running on jenkins
                            sh "python docker/local/run_test_docker_image_with_remote_dhruva.py --cluster=${params.CLUSTER} --tag=${params.TAG} --udpPort=${params.UDP_PORT} --tlsPort=${params.TLS_PORT}"
                            currentBuild.result = 'SUCCESS'
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            currentBuild.result = 'FAILURE'
            echo "ERROR: Could not trigger the image publisher router job"
            throw ex
        }
        finally {
            sh "ls -lrt"
            sh "pwd"
            junit 'integration-tests/*.xml'
            def message
            def details = ''
            if (currentBuild.result == 'FAILURE') {
                message = 'integration tests: Build failed'
            } else if (currentBuild.result == 'SUCCESS') {
                message = 'integration tests: Build passed'
            }
            if (env.CHANGE_ID == null) {
                notifyPipelineRoom("$message $details", roomId: notifySparkRoomId)
            } else {
                notifyPipelineRoom("$message $details", toPersonEmail: env.CHANGE_AUTHOR_EMAIL)
            }
        } // end finally
    }
}
#!groovy
library 'sparkPipeline'

/**
 * Update the version of cisco-spark-parent.
 *
 * Expects the following job parameters
 *      - Pipeline_Id   ID of the pipeline to update
 *      - Parent_ID     Repo name of the parent POM
 *
 * This is a default implementation. Teams can choose to override it in their team folders.
 * This pipeline is intended to trigger on a schedule or be manually run.
 * It uses Maven's versions:update-parent plugin. More information on it can be found at
 *      http://www.mojohaus.org/versions-maven-plugin/examples/update-parent.html
 *      http://www.mojohaus.org/versions-maven-plugin/update-parent-mojo.html
 *
 * versions:update-parent has a few points of note
 *      - It only updates to a valid version i.e. One that can be found in a repository
 *      - It does not fail if an invalid version is specified. But the version remains unchanged.
 *      - It supports downgrades as well as SNAPSHOTS (with -DallowSnapshots)
 *
 * There are caveats:
 *
 *  The version must be specified as a range. i.e. Within square brackets - []. Otherwise the argument
 *  is ignored and the latest version, according to artifactory, is used. That might not work as expected
 *  depending on how artifact versions are defined and deployed.
 *
 *  If the parent version is downgraded, to before some dependencies are defined, an error
 *  can occur on subsequent runs. e.g.
 *
 *      [ERROR]     'dependencies.dependency.version' for com.ciscospark:cisco-spark-integration:jar is missing.
 *
 *  This error occurs even if the update would correct the issue. Hence manual intervention is needed.
 */

timestamps {
    // Pass the pipeline id in from the job
    // For security reasons the pipeline id is passed in instead of the repository name
    pipelineId = params.Pipeline_Id
    parentId = params.Parent_Id ? params.Parent_Id : "cisco-spark-base"

    version = null
    // Grab the latest version of parent
    // Do this outside utilityNode as pipelineVersions calls it internally
    stage("Obtain Version") {
        versions = pipelineVersions(expected:[parentId])
        version = versions[parentId]
    }

    utilityNode {
        gitHubRepo = null
        repoName = null

        // Use the api token to clone, fork, and create a PR on the target pipeline
        withCredentials([usernamePassword(credentialsId: env.GITHUB_API_CREDENTIALS,
                usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {

            stage("Update Version") {
                // Convert the pipeline id into a repository name with the format WebExSquared/dhruva
                sh "git clone -q https://${env.USERNAME}:${env.PASSWORD}@sqbu-github.cisco.com/WebExSquared/pipeline.git pipeline"
                dir("pipeline/scripts") {
                    gitHubRepo = sh (
                            script: "python print-repo-name.py --pipeline ${pipelineId}",
                            returnStdout: true
                    ).trim()
                    // Split out the repo name to use on the fork url
                    repoName = gitHubRepo.split("/")[1]
                }

                sh "git clone -q https://${env.USERNAME}:${env.PASSWORD}@sqbu-github.cisco.com/${gitHubRepo}.git version-pull"

                dir("version-pull") {
                    // Update parent version in the pom.xml
                    script.updatePomParentVersion(version)
                }
            }

            stage("Merge Existing") {
                // Attempt to merge an existing open PR. The merge will succeed if the repo allows it. Typically,
                // a repo will have master protected and require a status check and a code review. In that case, the
                // merge will only succeed if both of those are met, which means a manual code review approval is still
                // required. However, in repos that do not require code reviews, the merge will succeed with just a
                // passing status check.
                withCredentials([usernamePassword(credentialsId: env.GITHUB_API_CREDENTIALS,
                        usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    def apiUrl = "https://sqbu-github.cisco.com/api/v3/repos/$gitHubRepo"
                    def r = httpRequest(url: apiUrl + '/pulls?head=wx2pltfm:master&base=master',
                            customHeaders: [[name: 'Authorization', value: "token $PASSWORD", maskValue: true]])
                    def json = readJSON(text: r.content)

                    if (json.size() == 1) {
                        httpRequest(httpMode: 'PUT', url: json[0]._links.self.href + '/merge',
                                customHeaders: [[name: 'Authorization', value: "token $PASSWORD", maskValue: true]],
                                consoleLogResponseBody: true,
                                validResponseCodes: '100:599')
                    }
                }
            }

            dir("version-pull") {
                noChange = false

                stage("Status") {
                    // Check if there are any changes
                    changeDetected = sh (
                            script: "git diff-files --quiet",
                            returnStatus: true
                    )
                    sh "git diff"
                }

                if(!changeDetected) {
                    print "Not creating a Pull Request as no changes detected"
                    return
                }

                stage("Pull-Request") {
                    // Set the token used by hub to authenticate with sqbu-github.cisco.com
                    // Set the host used by hub
                    withEnv(["GITHUB_TOKEN=${env.PASSWORD}", "GITHUB_HOST=sqbu-github.cisco.com"]) {
                        // Setup username and email used by git
                        sh "git config --local user.email '${env.USERNAME}@cisco.com'"
                        sh "git config --local user.name '${env.USERNAME}'"
                        // hub defaults to using regular github and does not automatically pick up the enterprise version
                        // Without this setting there can be an intermittent error (typically on new machine deploys)
                        //     hub fork
                        //     Error: repository under 'origin' remote is not a GitHub project
                        sh "git config --local --add hub.host sqbu-github.cisco.com"
                        // Fork the code. The PR will be created from the fork. The fork may
                        // already exist in sqbu-github.cisco.com GitHub.
                        sh "hub fork --no-remote"
                        sh "git add -u ."
                        sh "git commit -m 'Updating $parentId to $version'"
                        // When --force is specified all recent changes are pushed to the fork
                        sh "git push --force https://${env.USERNAME}:${env.PASSWORD}@sqbu-github.cisco.com/${env.USERNAME}/${repoName}.git HEAD:master"

                        // Explanation: Creating a pull-request here.
                        // But if a request from the forked repo already exists this command fails with
                        //      Error creating pull request: Unprocessable Entity (HTTP 422)
                        //      A pull request already exists for user:master.
                        // As the latest version has been pushed to the fork it's details are added to the PR.
                        // i.e. The version is updated in the PR.
                        // But the error above causes the job to fail. To prevent the failure I'm
                        //      2>&1    - routing stderr into stdout
                        //      || true - forcing the command to always succeed
                        // and checkout the stdout for both the success and acceptable failure status.
                        // Where success just prints out the PR url. Other scenarios are seen as failures.

                        pr_output = env.UPDATE_PARENT_PR_REVIEWERS ?  sh (
                                script: "hub pull-request -f -h ${env.USERNAME}/${repoName}:master -r ${env.UPDATE_PARENT_PR_REVIEWERS} -m 'Updating $parentId version' 2>&1 || true",
                                returnStdout: true
                        ).trim() : sh (
                                script: "hub pull-request -f -h ${env.USERNAME}/${repoName}:master -m 'Updating $parentId version' 2>&1 || true",
                                returnStdout: true
                        ).trim()

                        print pr_output

                        if (!pr_output.contains("https://sqbu-github.cisco.com/${gitHubRepo}/pull/")
                                && !pr_output.contains("A pull request already exists for")) {
                            error "Failed to create Pull Request"
                        }

                    }
                }
            }
        }
    }
}

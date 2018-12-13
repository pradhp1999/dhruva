#!groovy
library identifier: 'sparkPipeline', changelog: false

/**
 * sparkPipeline constructs a typical spark pipeline. Many options are available to alter the pipeline.
 * For documentation and a list of options, see:
 *     https://sqbu-github.cisco.com/WebExSquared/pipeline/blob/master/vars/sparkPipeline.txt
 */
sparkPipeline {
    notifySparkRoomId = 'Y2lzY29zcGFyazovL3VzL1JPT00vZmIyYWYyYTAtZmFkNi0xMWU2LWE4MzctZmQ5MjFlYjIzZDA5'
    // rename-remove-begin
    
    // Other teams should NOT be keeping 100 master branch builds. This is strictly for hello-world, since
    // it runs constantly as a canary.
    numToKeep = isMasterBranch() ? 100 : 3
    
    // This allows hello-world to run without human intervention, for canary purposes.
    integration.deployMode = 'deploy'
    production.deployMode = 'deploy'
    
    // rename-remove-end
}

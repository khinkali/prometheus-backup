@Library('semantic_releasing') _

podTemplate(label: 'mypod', containers: [
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.0', command: 'cat', ttyEnabled: true)
],
        volumes: [
                hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
        ]) {
    try {
        node('mypod') {
            properties([
                    buildDiscarder(
                            logRotator(artifactDaysToKeepStr: '',
                                    artifactNumToKeepStr: '',
                                    daysToKeepStr: '',
                                    numToKeepStr: '30'
                            )
                    ),
                    pipelineTriggers([cron('30 1 * * *')])
            ])

            stage('create backup') {
                currentBuild.displayName = getTimeDateDisplayName()

                def kc = 'kubectl -n test'
                def containerPath = '/opt/jboss/prometheus-2.0.0.linux-amd64/data'
                def containerName = 'prometheus'
                def podLabel = 'app=prometheus'
                def repositoryUrl = 'bitbucket.org/khinkali/prometheus_backup'
                container('kubectl') {
                    backup(podLabel, containerName, containerPath, repositoryUrl, kc)
                }
            }

        }

    } catch (all) {
        slackSend "Build Failed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
    }
}


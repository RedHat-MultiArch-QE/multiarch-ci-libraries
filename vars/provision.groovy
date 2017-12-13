/**
 * provision.groovy
 *
 * Attempts to provision a multi-arch host.
 *
 * @param arch String representing architecture of the host to provision.
 * @param connectToMaster Boolean specifying whether to run cinch to connect the provisioned host to the Jenkins master
 * @param installAnsible Boolean specifying whether to install ansible on the provisioned hsot.
 */
def LinkedHashMap call(String arch, Boolean connectToMaster, Boolean installAnsible) {
  stage('Provision Slave') {
    // Get linchpin topology
    git 'https://github.com/RedHat-MultiArch-QE/multiarch-ci-provisioner'

    def slave = [ buildNumber: null, arch: null, name: null, error: null ]
    def arch = arch
    def slaveTarget = "jenkins-slave"
    def slaveName = "${arch}-slave-${env.BUILD_NUMBER}"
    def provisioned = true
    def connectedToMaster = false
    def ansibleInstalled = false
    def error = ""

    try {
      sh "linchpin --workspace workspace --verbose up ${slaveTarget}"
      if (connectToMaster) {
        def extraVars = "\'{ \"rpm_key_imports\":[], \"jenkins_master_repositories\":[], \"jenkins_master_download_repositories\":[], \"jslave_name\":${slaveName}, \"jslave_label\":${slaveName}, \"arch\":${arch} }\'"
        sh "cinch workspace/inventories/${slaveTarget}.inventory --extra-vars ${extraVars}"
        connectedToMaster = true
      }
      if (installAnsible) {
        node (slaveName) {
          sh 'sudo yum install python-devel openssl-devel libffi-devel -y'
          sh 'sudo pip install --upgrade pip; sudo pip install --upgrade setuptools; sudo pip install --upgrade ansible'
        }
        ansibleInstalled = true
      }
    } catch (e) {
      error = e.toString()
      provisioned = false
    } finally {
      // Archive slave name in a slave.properties file
      def results = "name:${slaveName}\n" +
      "target:${slaveTarget}\n" +
      "arch:${arch}\n" +
      "provisioned:${provisioned}\n" +
      "jenkins.master.connected:${connectedToMaster}\n" +
      "ansible.installed:${ansibleInstalled}\n" +
      "error:${error}\n"
      writeFile(file: "workspace/${slaveTarget}.properties", text: results)
      archiveArtifacts artifacts: 'workspace/*-slave.properties', fingerprint: true
    }

    return slave
  }
}

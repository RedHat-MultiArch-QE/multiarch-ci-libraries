/**
 * provision.groovy
 *
 * Attempts to provision a multi-arch host.
 *
 * @param arch String representing architecture of the host to provision.
 * @param connectToMaster Boolean specifying whether to run cinch to connect the provisioned host to the Jenkins master
 * @param installAnsible Boolean specifying whether to install ansible on the provisioned hsot.
 */
import com.redhat.multiarch.ci.Slave

Slave call(String arch, Boolean connectToMaster, Boolean installAnsible) {
  // Get linchpin topology
  git 'https://github.com/RedHat-MultiArch-QE/multiarch-ci-provisioner@dev'

  Slave slave = new Slave(
    arch: arch,
    target: 'jenkins-slave',
    name: "${arch}-slave"
  )

  try {
    sh "linchpin --workspace workspace --verbose up ${slave.target}"
    slave.provisioned = true
    if (connectToMaster) {
      def extraVars = "\'{ \"rpm_key_imports\":[], \"jenkins_master_repositories\":[], \"jenkins_master_download_repositories\":[], \"jslave_name\":${slave.name}, \"jslave_label\":${slave.name}, \"arch\":${slave.arch} }\'"
      sh "cinch workspace/inventories/${slave.target}.inventory --extra-vars ${extraVars}"
      slave.connectedToMaster = true
    }
    if (installAnsible) {
      node (slaveName) {
        sh 'sudo yum install python-devel openssl-devel libffi-devel -y'
        sh 'sudo pip install --upgrade pip; sudo pip install --upgrade setuptools; sudo pip install --upgrade ansible'
      }
      slave.ansibleInstalled = true
    }
  } catch (e) {
    slave.error = e.toString()
  }

  slave
}

/**
 * provision.groovy
 *
 * Attempts to provision a multi-arch host.
 *
 * @param arch String representing architecture of the host to provision.
 * @param config ProvisioningConfig Configuration for provisioning.
 */
import com.redhat.multiarch.ci.Slave
import com.redhat.multiarch.ci.ProvisioningConfig

Slave call(String arch,
           ProvisioningConfig config
) {
  Slave slave = new Slave(
    arch: arch,
    target: 'jenkins-slave',
    name: "${arch}-slave"
  )

  try {

    withCredentials([file(credentialsId: config.KEYTABCREDENTIALID,
            variable: 'KEYTAB')]) {
      sh "kinit ${config.krbPrincipal} -k -t ${KEYTAB}"

      // prepare beaker client config.
      def clientConf = readFile '/etc/beaker/client.conf'
      writeFile(file: "/tmp/client.conf", text: "${clientConf}\nKRB_KEYTAB = \"${KEYTAB}\"\n")

      // tell beaker to use our config file.
      env.BEAKER_CLIENT_CONF = '/tmp/client.conf'

      // test to make sure we can authenticate.
      sh 'bkr whoami'

    }

    if (config.provisioningRepoUrl != null) {
      // Get linchpin workspace
      git(url: config.provisioningRepoUrl, branch: config.provisioningRepoRef)
    } else {
      checkout scm
    }

    // Attempt provisioning
    sh "linchpin --workspace ${config.provisioningWorkspaceDir} --template-data '{ arch: ${slave.arch}, job_group: ${config.jobgroup} }' --verbose up ${slave.target}"

    sh 'find . | grep inventory'

    slave.inventory = sh (returnStdout: true, script: """
            ls -1 ${config.provisioningWorkspaceDir}/inventories/*.inventory
            """).trim()

    sh "cat ${slave.inventory}"

    slave.provisioned = true
    
    if (config.runOnSlave) {
      def extraVars = "\'{ \"rpm_key_imports\":[], \"jenkins_master_repositories\":[], \"jenkins_master_download_repositories\":[], \"jslave_name\":${slave.name}, \"jslave_label\":${slave.name}, \"arch\":${slave.arch} }\'"
      sh "cinch ${config.provisioningWorkspaceDir}/inventories/${slave.target}.inventory --extra-vars ${extraVars}"
      slave.connectedToMaster = true
    }
    if (config.installAnsible) {
      node (slaveName) {
        sh 'sudo yum install python-devel openssl-devel libffi-devel -y'
        sh 'sudo pip install --upgrade pip; sudo pip install --upgrade setuptools; sudo pip install --upgrade ansible'
      }
      slave.ansibleInstalled = true
    }
  } catch (e) {
    echo e.getMessage()
    slave.error = e.getMessage()
  }

  slave
}

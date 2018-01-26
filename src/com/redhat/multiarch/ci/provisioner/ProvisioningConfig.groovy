package com.redhat.multiarch.ci.provisioner

class ProvisioningConfig {
  // Job group for Beaker provisioning.
  String jobgroup = 'multiarch-qe'
  // Host requirements to pass to linchpin
  List<HashMap> hostrequires = null
  // name of Openshift tenant to pull provisioning image from.
  String tenant = 'redhat-multiarch-qe'
  // docker url to pull provisioning image from.
  String dockerUrl = '172.30.1.1:5000'
  // name of provisioning image. Can include tag name.
  String provisioningImage = 'provisioner'
  // Provisioning repo url
  String provisioningRepoUrl = null
  // Provisioning repo ref
  String provisioningRepoRef = null
  // Provisioning workspace location (needed for Linchpin)
  // This can reference a relative path in the above repo
  // or it can reference a relative path that already exists
  // in the current directory
  String provisioningWorkspaceDir = 'workspace'
  // Kerberos principal for Beaker authentication.
  String krbPrincipal = 'jenkins/multiarch-qe-jenkins.rhev-ci-vms.eng.rdu2.redhat.com'
  // ID of Jenkins credential for keytab needed for Beaker authentication.
  String keytabCredentialId = 'KEYTAB'
  // ID of Jenkins credential for SSH private key to will be
  // copied to provisioned resource.
  // *** This must be the same as what was added to Beaker ***
  String sshPrivKeyCredentialId = 'SSHPRIVKEY'
  // ID of Jenkins credential for SSH public key to will be
  // copied to provisioned resource
  // *** This must be the same as what was added to Beaker ***
  String sshPubKeyCredentialId = 'SSHPUBKEY'
  // ID of the Jenkins credential for the username and password
  // used by cinch to connect the provisioned host to the Jenkins master
  String jenkinsSlaveCredentialId = 'JENKINS_SLAVE_CREDENTIALS'
  // URL of the Jenkins master that cinch will use to connect the provisioned
  // host as a slave.
  String jenkinsMasterUrl = ""
  // Extra arguments passed to the jswarm call.
  // Allows for the connection to be tunneled in the case of an OpenShift hosted Jenkins.
  String jswarmExtraArgs = ""
  // Whether the closure should be run on directly on the provisioned slave.
  Boolean runOnSlave = true
  // Whether Ansible should be installed on the provisioned slave.
  Boolean installAnsible = true

  ProvisioningConfig(params, env) {
    this.keytabCredentialId = params.KEYTABCREDENTIALID ?: this.keytabCredentialId
    this.sshPrivKeyCredentialId = params.SSHPRIVKEYCREDENTIALID ?: this.sshPrivKeyCredentialId
    this.sshPubKeyCredentialId = params.SSHPUBKEYCREDENTIALID ?: this.sshPubKeyCredentialId
    this.jenkinsSlaveCredentialId = params.JENKINSSLAVECREDENTIALID ?: this.jenkinsSlaveCredentialId
    this.jenkinsMasterUrl = env.JENKINS_MASTER_URL ?: this.jenkinsMasterUrl
    this.jswarmExtraArgs = env.JSWARM_EXTRA_ARGS ?: this.jswarmExtraArgs
  }
}

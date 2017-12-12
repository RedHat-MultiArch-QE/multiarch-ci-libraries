/**
 * getSlave.groovy
 *
 * Provisions a multi-arch slave.
 *
 * @param arch String specifying the architecture of the slave to provision.
 * @param runOnProvisionedHost Boolean determining whether cinch should be run post provisioning.
 * @param installAnsible Boolean determining whether ansible should be installed on the provisioned slave.
 * @return LinkedHashMap contained the name and buildNumber of the provisioned slave.
 */
def LinkedHashMap call(String arch, Boolean runOnProvisionedHost, def Boolean installAnsible = true) {
  def slave = [ buildNumber: null, arch: null, name: null, error: null ]

  try {
    stage('Provision Slave') {
      println("Provisioning ${arch}-slave with runOnProvisionedHost=${runOnProvisionedHost} and installAnsible=${installAnsible}")
      provisioner(arch, runOnProvisionedHost, installAnsible)

      // Load slave properties (you may need to turn off sandbox or approve this in Jenkins)
      slave = readProperties(file: "${arch}-slave.properties", defaults: slave)
      println "Assigned the host name from the properties file ${slave}"
    }
  } catch (e) {
    // If provision fails, grab the build number from the error message and set build status to not built
    currentBuild.result = 'FAILURE'
    slave.error = e.toString()
  } finally {
    println "Provisioned: ${slave}"
    return slave
  }
}

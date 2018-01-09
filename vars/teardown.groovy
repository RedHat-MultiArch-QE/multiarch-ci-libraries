/**
 * teardown.groovy
 *
 * Runs a teardown for provisioned slave.
 *
 * @param slave Slave to be torn down.
 * @param arch String specifying the arch to run tests on.
 * @param config ProvisioningConfig for provisioning.
 *
 */
import com.redhat.multiarch.ci.Slave
import com.redhat.multiarch.ci.ProvisioningConfig

def call(Slave slave, String arch, ProvisioningConfig config) {
  // Prepare the cinch teardown inventory
  if (!slave.provisioned) {
    // The provisioning job did not successfully provision a machine, so there is nothing to teardown
    currentBuild.result = 'SUCCESS'
    return
  }

  // Preform the actual teardown
  try {
    sh "teardown workspace/inventories/${slave.target}.inventory"
  } catch (e) {
    println e
  }

  try {
    sh "linchpin --workspace workspace --template-data \'{ arch: $arch, job_group: $config.jobgroup }\' --verbose destroy ${slave.target}"
  } catch (e) {
    println e

    if (slave.error) {
      currentBuild.result = 'FAILURE'
    }
  }
}

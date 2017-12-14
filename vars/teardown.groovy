/**
 * teardown.groovy
 *
 * Runs a teardown for provisioned slave.
 *
 * @param slave Slave to be torn down.
 */
import com.redhat.multiarch.ci.Slave

def call(Slave slave) {
  // Prepare the cinch teardown inventory
  stage('Teardown Setup') {
    if (!slave.provisioned) {
      // The provisioning job did not successfully provision a machine, so there is nothing to teardown
      currentBuild.result = 'SUCCESS'
      return
    }
  }

  // Preform the actual teardown
  stage('Teardown') {
    try {
      sh "teardown workspace/inventories/${slave.target}.inventory"
    } catch (e) {
      println e
    }

    try {
      sh "linchpin --workspace workspace --verbose destroy ${slave.target}"
    } catch (e) {
      println e

      if (slave.error) {
        currentBuild.result = 'FAILURE'
      }
    }
  }
}

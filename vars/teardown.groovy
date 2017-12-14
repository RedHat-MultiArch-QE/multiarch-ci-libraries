def call(Slave slave) {
  node ('provisioner') {
    // Prepare the cinch teardown inventory
    stage('Teardown Setup') {
      if (!slave.provisione) {
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

        if (slaveProps.error) {
          currentBuild.result = 'FAILURE'
        }
      }
    }
  }
}

def call() {
  node ('provisioner') {
    def slaveProps = null

    // Prepare the cinch teardown inventory
    stage('Teardown Setup') {

      // Load slave properties (you may need to turn off sandbox or approve this in Jenkins)
      def propertyFiles = findFiles glob: 'workspace/*-slave.properties'
      slaveProps = readProperties file: "${propertyFiles[0].path}"

      if (slaveProps.provisioned == false) {
        // The provisioning job did not successfully provision a machine, so there is nothing to teardown
        currentBuild.result = 'SUCCESS'
        return
      }
    }

    // Preform the actual teardown
    stage('Teardown') {
      try {
        sh "teardown workspace/inventories/${slaveProps.target}.inventory"
      } catch (e) {
        println e
      }

      try {
        sh "linchpin --workspace workspace --verbose destroy ${slaveProps.target}"
      } catch (e) {
        println e

        if (slaveProps.error == null || slaveProps.error.isEmpty()) {
          currentBuild.result = 'FAILURE'
        }
      }
    }
  }
}

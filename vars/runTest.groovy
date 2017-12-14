/**
 * runTest.groovy
 *
 * Runs @test on the multi-arch capable provisioner container, and runs @onTestFailure if it encounters an Exception.
 * @param arch String specifying the arch to run tests on.
 * @param runOnSlave Boolean that specificies whether the
 *        closure should be run on directly on the provisioned slave.
 * @param installAnsible Boolean that specificies whether Ansible should
 *        be installed on the provisioned slave.
 * @param test Closure that takes the Slave used by the test.
 * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
 */
import com.redhat.multiarch.ci.Slave

def call(String arch, Boolean runOnSlave, Boolean installAnsible, Closure test, Closure onTestFailure) {
  podTemplate(
    name: 'provisioner',
    label: 'provisioner',
    cloud: 'openshift',
    serviceAccount: 'jenkins',
    idleMinutes: 0,
    namespace: 'redhat-multiarch-qe',
    containers: [
      // This adds the custom slave container to the pod. Must be first with name 'jnlp'
      containerTemplate(
        name: 'jnlp',
        image: "172.30.1.1:5000/redhat-multiarch-qe/provisioner",
        ttyEnabled: false,
        args: '${computer.jnlpmac} ${computer.name}',
        command: '',
        workingDir: '/tmp',
        privileged: true
      )
    ]
  ) {
    ansiColor('xterm') {
      timestamps {
        node('provisioner') {
          try {
            def Slave slave = provision(arch, runOnSlave, installAnsible)

            // Property validity check
            if (slave == null || slave.name == null || slave.arch == null) {
              throw new Exception("Invalid provisioned slave: ${slave}")
            }

            // If the provision failed, there will be an error
            if (slave.error != null && !slave.error.isEmpty()) {
              throw new Exception(slave.error)
            }

            if (runOnSlave) {
              node(slave.name) {
                test(slave))
                return
              }
            }

            test(slave)
          } catch (e) {
            onTestFailure(slave, e)
          } finally {
            // Ensure teardown runs before the pipeline exits
            stage ('Teardown Slave') {
              teardown()
            }
          }
        }
      }
    }
  }
}

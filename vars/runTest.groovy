/**
 * runTest.groovy
 *
 * Runs @test on the multi-arch capable provisioner container, and runs @onTestFailure if it encounters an Exception.
 * @param arch String specifying the arch to run tests on.
 * @param config ProvisioningConfig Configuration for provisioning.
 * @param test Closure that takes the Slave used by the test.
 * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
 */
import com.redhat.multiarch.ci.Slave
import com.redhat.multiarch.ci.ProvisioningConfig

def call(String arch,
         ProvisioningConfig config,
         Closure test,
         Closure onTestFailure) {

  podTemplate(
    name: 'provisioner',
    label: 'provisioner',
    cloud: 'openshift',
    serviceAccount: 'jenkins',
    idleMinutes: 0,
    namespace: config.tenant,
    containers: [
      // This adds the custom slave container to the pod. Must be first with name 'jnlp'
      containerTemplate(
        name: 'jnlp',
        image: "${config.dockerUrl}/${config.tenant}/${config.provisioningImage}",
        ttyEnabled: false,
        args: '${computer.jnlpmac} ${computer.name}',
        command: '',
        workingDir: '/tmp'
      )
    ]
  ) {
    ansiColor('xterm') {
      timestamps {
        node('provisioner') {
          Slave slave
          try {
            stage('Provision Slave') {
              slave = provision(arch, config)

              // Property validity check
              if (!slave.name || !slave.arch) {
                throw new Exception("Invalid provisioned slave: ${slave}")
              }

              // If the provision failed, there will be an error
              if (slave.error) {
                error slave.error
              }
            }

            if (config.runOnSlave) {
              node(slave.name) {
                test(slave, config)
                return
              }
            }

            test(slave, config)
          } catch (e) {
            onTestFailure(e, slave)
          } finally {
            // Ensure teardown runs before the pipeline exits
            stage ('Teardown Slave') {
              teardown(slave, arch, config)
            }
          }
        }
      }
    }
  }
}

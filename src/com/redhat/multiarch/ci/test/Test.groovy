package com.redhat.multiarch.ci.test

import com.redhat.multiarch.ci.provisioner.Host
import com.redhat.multiarch.ci.provisioner.Provisioner
import com.redhat.multiarch.ci.provisioner.ProvisioningConfig

class Test {
  String arch
  ProvisioningConfig config
  Closure test
  Closure onTestFailure

  /**
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  Test(String arch,
       ProvisioningConfig config,
       Closure test,
       Closure onTestFailure) {
    this.arch = arch
    this.config = config
    this.test = test
    this.onTestFailure = onTestFailure
  }

  /**
   * Runs @test on the multi-arch capable provisioner container,
   * and runs @onTestFailure if it encounters an Exception.
   */
  def run() {
    Provisioner provisioner = new Provisioner(config)

    podTemplate(
      name: 'provisioner',
      label: 'provisioner',
      cloud: 'openshift',
      serviceAccount: 'jenkins',
      idleMinutes: 0,
      namespace: config.tenant,
      containers: [
        // This adds the custom provisioner slave container to the pod. Must be first with name 'jnlp'
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

            Host host
            try {
              stage('Provision Slave') {
                host = provisioner.provision(arch, config)

                // Property validity check
                if (!host.name || !host.arch) {
                  error "Invalid provisioned host: ${host}"
                }

                // If the provision failed, there will be an error
                if (host.error) {
                  error host.error
                }
              }

              if (config.runOnSlave) {
                node(host.name) {
                  test(host, config)
                  return
                }
              }

              test(host, config)
            } catch (e) {
              onTestFailure(e, host)
            } finally {
              // Ensure teardown runs before the pipeline exits
              stage ('Teardown Slave') {
                provisioner.teardown(host, arch, config)
              }
            }
          }
        }
      }
    }
  }
}

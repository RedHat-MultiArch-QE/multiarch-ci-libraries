package com.redhat.multiarch.ci.test

import com.redhat.multiarch.ci.provisioner.Host
import com.redhat.multiarch.ci.provisioner.Provisioner
import com.redhat.multiarch.ci.provisioner.ProvisioningConfig

class Test {
  def script
  String arch
  ProvisioningConfig config
  Closure test
  Closure onTestFailure
  Closure postTest

  /**
   * @param script WorkflowScript that the test will run in.
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host used by the test.
   * @param onTestFailure Closure that take the Host used by the test and the Exception that occured.
   */
  Test(def script, String arch, ProvisioningConfig config, Closure test, Closure onTestFailure, Closure postTest) {
    this.script = script
    this.arch = arch
    this.config = config
    this.test = test
    this.onTestFailure = onTestFailure
    this.postTest = postTest
  }

  /*
   * Runs @test on a multi-arch provisioned host for the specified arch.
   * Runs @onTestFailure if it encounters an Exception.
   */
  def run() {
    Provisioner provisioner = new Provisioner(script, config)

    script.podTemplate(
      name: "provisioner-${config.version}",
      label: "provisioner-${config.version}",
      cloud: config.cloudName,
      serviceAccount: 'jenkins',
      idleMinutes: 0,
      namespace: config.tenant,
      containers: [
        // This adds the custom provisioner slave container to the pod. Must be first with name 'jnlp'
        script.containerTemplate(
          name: 'jnlp',
          image: "${config.dockerUrl}/${config.tenant}/${config.provisioningImage}-${config.version}",
          ttyEnabled: false,
          args: '${computer.jnlpmac} ${computer.name}',
          command: '',
          workingDir: '/tmp'
        )
      ]
    ) {
      script.ansiColor('xterm') {
        script.timestamps {
          script.node("provisioner-${config.version}") {

            Host host
            try {
              script.stage('Provision Host') {
                host = provisioner.provision(arch)

                // Property validity check
                if (!host.name || !host.arch) {
                  script.error "Invalid provisioned host: ${host}"
                }

                // If the provision failed, there will be an error
                if (host.error) {
                  script.error host.error
                }
              }

              if (config.runOnSlave) {
                script.node(host.name) {
                  test(host, config)
                }
                return
              }

              test(host, config)
            } catch (e) {
              onTestFailure(e, host)
            } finally {
              postTest()
 
              // Ensure teardown runs before the pipeline exits
              script.stage ('Teardown Host') {
                provisioner.teardown(host, arch)
              }
            }
          }
        }
      }
    }
  }
}

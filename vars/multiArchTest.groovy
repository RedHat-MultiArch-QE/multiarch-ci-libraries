/**
 * multiArchTest.groovy
 *
 * Runs @test on the multi-arch capable provisioner container, and runs @onTestFailure if it encounters an Exception.
 *
 * @param test Closure that takes not parameters but runs the test.
 * @param onTestFailure Closure that takes a single parameter that is the Exception that occured in test.
 */
def call(Closure test, Closure onTestFailure) {
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
            test()
          catch (e) {
            onTestFailure(e)
          }
        }
      }
    }
  }
}

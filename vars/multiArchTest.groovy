/**
 * multiArchTest.groovy
 *
 * Runs @body on the multi-arch capable provisioner container.
 *
 * @param body Closure that is the test being run.
 */
def call(Closure body) {
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
          body()
        }
      }
    }
  }
}

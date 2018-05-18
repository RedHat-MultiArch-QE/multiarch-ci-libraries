def call(Closure test) {
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
        workingDir: '/tmp',
        privileged: true
      )
    ]
  ) {
    script.ansiColor('xterm') {
      script.timestamps {
        script.node("provisioner-${config.version}") {
          test()
        }
      }
    }
  }
}

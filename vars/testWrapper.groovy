import com.redhat.multiarch.ci.provisioner.ProvisioningConfig

def call(ProvisioningConfig config, Closure test) {
  podTemplate(
    name: "provisioner-${config.version}",
    label: "provisioner-${config.version}",
    cloud: config.cloudName,
    serviceAccount: 'jenkins',
    idleMinutes: 0,
    namespace: config.tenant,
    containers: [
      // This adds the custom provisioner slave container to the pod. Must be first with name 'jnlp'
      containerTemplate(
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
    ansiColor('xterm') {
      timestamps {
        node("provisioner-${config.version}") {
          test()
        }
      }
    }
  }
}

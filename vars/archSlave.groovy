/**
 * archSlave.groovy
 *
 * Runs closure body on a multi-arch slave for each arch in params.ARCHES.
 *
 * @param onSuccess Closure that takes two parameters representing the name String and the architecture String of the slave.
 * @param onFailure Closure that takes two parameters representing the Exception that occured and the architecture String of the slave.
 * @param runOnSlave Boolean that specificies whether the
 *        closure should be run on directly on the provisioned slave.
 */
def call(def Closure onSuccess = null, def Closure onFailure = null, def Boolean runOnSlave = true) {
  arches(
    { a ->
      def String arch = new String(a)
      return {
        def LinkedHashMap slave = [:]
        try {
          slave = getSlave(arch, runOnSlave)

          // Property validity check
          if (slave == null || slave.name == null || slave.arch == null) {
            throw new Exception("Invalid provisioned slave: ${slave}")
          }

          // If the provision failed, there will be an error
          if (slave.error != null && !slave.error.isEmpty()) {
            throw new Exception(slave.error)
          }

          if (onSuccess == null) return
          
          if (runOnSlave) {
            node(slave.name) {
             onSuccess(slave.name, slave.arch)
            }
            return
          }

          onSuccess(slave.name, slave.arch)
        } catch (e) {
          if (onFailure == null) return
          onFailure(e, slave.arch)
        } finally {
          // Ensure teardown runs before the pipeline exits
          stage ('Teardown Slave') {
            build(
              [
                job: '/multiarch-qe/teardown-multiarch-slave',
                parameters: [
                  string(name: 'BUILD_NUMBER', value: slave.buildNumber)
                ],
                propagate: false,
                wait: true
              ]
            )
          }
        }
      }
    }
  )
}

/**
 * archSlave.groovy
 *
 * Runs closure body on a multi-arch slave for each arch in params.ARCHES.
 *
 * @param body Closure that takes a two String parameters representing the name and architecture of the slave.
 * @param runOnSlave Boolean that specificies whether the
 *        closure should be run on directly on the provisioned slave.
 * @param installAnsible Boolean that specificies whether Ansible should 
 *        be installed on the provisioned slave.
 */
def call(Closure body, def Boolean runOnSlave = true, def Boolean installAnsible = true) {
  arches(
    { a ->
      def String arch = new String(a)
      return {
        def LinkedHashMap slave = [:]
        try {
          slave = getSlave(arch, runOnSlave, installAnsible)

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
              body(slave.name, slave.arch)
            }
            return
          }

          body(slave.name, slave.arch)
        } catch (e) {
          // This is just a wrapper step to ensure that teardown is run upon failure
          println(e)
        } finally {
          // Ensure teardown runs before the pipeline exits
          stage ('Teardown Slave') {
            teardown()
          }
        }
      }
    }
  )
}

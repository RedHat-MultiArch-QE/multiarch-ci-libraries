import com.redhat.multiarch.ci.provisioner.*
import com.redhat.multiarch.ci.test.*
import com.redhat.multiarch.ci.task.*

class TestUtils {
  static ProvisioningConfig config = null

  static ProvisioningConfig getProvisioningConfig(WorkflowScript script) {
    if (!config) config = new ProvisioningConfig(script.params, script.env)
    config
  }

  /**
   * Runs @test on a multi-arch provisioned host for the specified arch.
   * Runs @onTestFailure if it encounters an Exception.
   *
   * @param script WorkflowScript that the test will run in.
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host used by the test.
   * @param onTestFailure Closure that take the Host used by the test and the Exception that occured.
   */
  static def test(
    WorkflowScript script,
    String arch,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
    (new Test(arch, config, test, onTestFailure)).run()
  }


  /**
   * Runs @test on a multi-arch provisioned host for each arch in arches param.
   * Runs @onTestFailure if it encounters an Exception.
   *
   * @param script WorkflowScript that this test will run in.
   * @param arches List<String> specifying the arches to run single host tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host used by the test.
   * @param onTestFailure Closure that take the Host used by the test and the Exception that occured.
   */
  static def parallelMultiArchTest(
    WorkflowScript script,
    List<String> arches,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
    (new MultiArchTest(script, arches, config, test, onTestFailure)).run()
  }

  static def downloadTests(def params) {
    if (params.TEST_REPO) {
      git url: params.TEST_REPO, branch: params.TEST_REF, changelog: false
    }
    else {
      checkout scm
    }
  }

  static def runTests(def params, Host host) {
    // Cinch Mode
    if (config.runOnSlave) {
      sh "ansible-playbook -i 'localhost,' -c local ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml"
      sh "for i in ${params.TEST_DIR}/scripts/*/test.sh; do bash \$i; done"

      return
    }

    // SSH Mode
    sh ". /home/jenkins/envs/provisioner/bin/activate; ansible-playbook -i '${host.inventory}' ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml"
    sh "for i in ${params.TEST_DIR}/scripts/*/test.sh; do ssh root@${host.hostName} < \$i; done"
  }

  static def archiveOutput(def params) {
    try {
      archiveArtifacts allowEmptyArchive: true, artifacts: "${params.TEST_DIR}/ansible-playbooks/**/artifacts/*", fingerprint: true
      junit "${params.TEST_DIR}/ansible-playbooks/**/reports/*.xml"
    }
    catch (e) {
      // We don't care if this step fails
    }
    try {
      archiveArtifacts allowEmptyArchive: true, artifacts: "${params.TEST_DIR}/scripts/**/artifacts/*", fingerprint: true
      junit "${params.TEST_DIR}/scripts/**/reports/*.xml"
    }
    catch (e) {
      // We don't care if this step fails
    }
  }
}

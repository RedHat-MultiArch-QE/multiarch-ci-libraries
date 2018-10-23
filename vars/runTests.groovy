import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Mode

void call(ProvisioningConfig config, ProvisionedHost host) {

    private final String ACTIVATE_PROVISIONER = '. /home/jenkins/envs/provisioner/bin/activate;'
    List<Exception> exceptions = []

    // JNLP Mode
    if (config.mode == Mode.JNLP) {
        // Run Playbooks
        try {
            sh "ansible-playbook -i 'localhost,' -c local ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml"
        } catch (e) {
            exceptions.add(e)
        }
        // Run tests
        try {
            sh "for i in ${params.TEST_DIR}/scripts/*/test.sh; do bash \$i ${host.scriptParams}; done"
        } catch (e) {
            exceptions.add(e)
        }
        // Collect Artifacts
        // Do nothing here since artifacts are already on the host where the archive step will run
    }

    // SSH Mode
    if (config.mode == Mode.SSH) {
        // Run Playbooks
        try {
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Run Scripts
        try {
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' ${config.SCRIPT_RUNNER_PLAYBOOK} \
                    --extra_vars '{script_params:"${host.scriptParams}"}'
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Collect Artifacts
        try {
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' ${config.ARTIFACT_COLLECTOR_PLAYBOOK}
            """)
        }
    }

    exceptions.each {
        e ->
        error(e.message)
    }
}

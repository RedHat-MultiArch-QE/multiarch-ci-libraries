import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Mode

void call(ProvisioningConfig config, ProvisionedHost host) {
    final String ACTIVATE_PROVISIONER = '. /home/jenkins/envs/provisioner/bin/activate;'
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
                ansible-playbook -i '${host.inventoryPath}' playbooks/run_scripts.yml \
                    -e '{test_dir:"${params.TEST_DIR}", script_params:"${host.scriptParams ?: ''}"}'
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Collect Artifacts
        try {
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' playbooks/collect_results.yml \
                    -e '{test_dir:"${params.TEST_DIR}"}'
            """)
        } catch (e) {
            exceptions.add(e)
        }
    }

    exceptions.each {
        e ->
        error(e.message)
    }
}

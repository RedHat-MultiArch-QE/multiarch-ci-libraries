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
                ansible-playbook -i '${host.inventoryPath}' --key-file "~/.ssh/id_rsa" \
                    ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Run Scripts
        try {
            String runScriptsPath = 'playbooks/run_scripts.yml'
            String runScripts = libraryResource(runScriptsPath)
            writeFile(file:runScriptsPath, text:runScripts)
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' --key-file "~/.ssh/id_rsa" \
                    -e '{"test_dir":"${params.TEST_DIR}", script_params:"${host.scriptParams ?: ''}"}' \
                    ${runScriptsPath}
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Collect Artifacts
        try {
            String collectResultsPath = 'playbooks/collect_results.yml'
            String collectResults = libraryResource(collectResultsPath)
            writeFile(file:collectResultsPath, text:collectResults)
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' --key-file "~/.ssh/id_rsa" \
                    -e '{"test_dir":"${params.TEST_DIR}"}' \
                    ${collectResultsPath}
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

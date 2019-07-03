import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Mode

void call(ProvisioningConfig config, ProvisionedHost host) {
    final String ACTIVATE_PROVISIONER = '. /home/jenkins/envs/ansible/bin/activate;'
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
            String runScriptsPlaybook = 'run_scripts.yml'
            String runScripts = libraryResource("playbooks/${runScriptsPlaybook}")
            writeFile(file:runScriptsPlaybook, text:runScripts)
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' --limit master_node --key-file "~/.ssh/id_rsa" \
                    -e '{"test_dir":"${params.TEST_DIR}", "inventory":"${host.inventoryPath}", \
                         "script_params":"${host.scriptParams ?: ''}"}' \
                    ${runScriptsPlaybook}
            """)
        } catch (e) {
            exceptions.add(e)
        }
        // Collect Artifacts
        try {
            String collectResultsPlaybook = 'collect_results.yml'
            String collectResults = libraryResource("playbooks/${collectResultsPlaybook}")
            writeFile(file:collectResultsPlaybook, text:collectResults)
            sh("""
                ${ACTIVATE_PROVISIONER}
                ansible-playbook -i '${host.inventoryPath}' --key-file "~/.ssh/id_rsa" \
                    -e '{"test_dir":"${params.TEST_DIR}"}' \
                    ${collectResultsPlaybook}
            """)
        } catch (e) {
            exceptions.add(e)
        }
    }

    try {
        archiveArtifacts(
            allowEmptyArchive:true,
            artifacts:"${params.TEST_DIR}/ansible-playbooks/**/artifacts/**/*.*",
            fingerprint:true
        )
        junit "${params.TEST_DIR}/ansible-playbooks/**/reports/**/*.xml"
    }
    catch (e) {
        // We don't care if this step fails
        echo("Ignoring exception: ${e}")
    }
    try {
        archiveArtifacts(
            allowEmptyArchive:true,
            artifacts:"${params.TEST_DIR}/scripts/**/artifacts/**/*.*",
            fingerprint:true
        )
        junit "${params.TEST_DIR}/scripts/**/reports/**/*.xml"
    }
    catch (e) {
        // We don't care if this step fails
        echo("Ignoring exception: ${e}")
    }

    exceptions.each {
        e ->
        error(e.message)
    }
}

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Mode

void call(ProvisioningConfig config, ProvisionedHost host) {
    List<Exception> exceptions = []

    // JNLP Mode
    if (config.mode == Mode.JNLP) {
        try {
            sh "ansible-playbook -i 'localhost,' -c local ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml"
        } catch (e) {
            exceptions.add(e)
        }
        try {
            sh "for i in ${params.TEST_DIR}/scripts/*/test.sh; do bash \$i; done"
        } catch (e) {
            exceptions.add(e)
        }
    }

    // SSH Mode
    if (config.mode == Mode.SSH) {
        try {
            sh("""
                ls -a provisioning/workspace;
                . /home/jenkins/envs/provisioner/bin/activate;
                ansible-playbook --key-file '~/.ssh/id_rsa' -i '${host.inventoryPath}' \
                    ${params.TEST_DIR}/ansible-playbooks/*/playbook.yml;
                ls -a provisioning/workspace;
            """)
        } catch (e) {
            exceptions.add(e)
        }
        try {
            sh("""
                    for i in ${params.TEST_DIR}/scripts/*/test.sh
                        do ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
                            -i ~/.ssh/id_rsa root@${host.hostname} < \$i
                    done
                """)
        } catch (e) {
            exceptions.add(e)
        }
    }

    exceptions.each {
        e ->
        script.error(e.message)
    }
}

/**
 * A mock for a pipeline script used to test the external API.
 */
class PipelineTestScript extends Script {
    @Override
    Object run() {
        null
    }

    @SuppressWarnings(['Instanceof', 'LineLength', 'MethodSize'])
    PipelineTestScript() {
        binding.with {
            params = [:]
            env = [
                environment:[:],
            ]
            ansiColor = {
                string, body ->
                LOG.info("ansiColor(${string})")
                body()
            }
            podTemplate = {
                map, body ->
                LOG.info('podTemplate()')
                body()
            }
            containerTemplate = {
                map ->
                LOG.info('containerTemplate()')
            }
            timestamps = {
                body ->
                LOG.info('timestamps()')
                body()
            }
            parallel = {
                tasks ->
                LOG.info('parallel()')
                tasks.each {
                    task ->
                    LOG.info("Running on Host: ${task.key}")
                    task.value()
                }
            }
            node = {
                name, body ->
                LOG.info("node(${name})")
                body()
            }
            stage = {
                stage, body ->
                LOG.info("stage(${stage})")
                body()
            }
            echo = {
                msg ->
                LOG.info("echo(${msg})")
            }
            sh = {
                sh ->
                if (sh instanceof Map) {
                    LOG.info(sh.script)
                    return 'hostname'
                }

                LOG.info(sh)
            }
            file = {
                cred ->
                LOG.info("file(${cred.credentialsId})")
                binding.setProperty("${cred.variable}", "${cred.credentialsId}")
                cred.credentialsId
            }
            usernamePassword = {
                cred ->
                LOG.info("usernamePassword(${cred.credentialsId})")
                binding.setProperty("${cred.usernameVariable}", "${cred.credentialsId}")
                binding.setProperty("${cred.passwordVariable}", "${cred.credentialsId}")
                cred.credentialsId
            }
            withCredentials = {
                credList, body ->
                LOG.info("withCredentials(${credList})")
                body()
            }
            error = {
                error ->
                LOG.severe("error(${error})")
            }
            scm = [:]
            git = {
                repo ->
                LOG.info("git(${repo})")
            }
            checkout = {
                scm ->
                LOG.info("checkout(${scm})")
            }
            readFile = {
                file ->
                LOG.info("readFile(${file})")
                '''
                {"4": {"action": "up", "targets": [{"beaker-slave": {"1": {"uhash": "178e46", "rc": 0}, "outputs": {"inventory_path": ["/home/jpoulin/Projects/scratch/postup-hook-test/inventories/beaker-slave-178e46.inventory"], "resources": {"os_keypair_res": [], "rax_server_res": [], "aws_ec2_res": [], "os_server_res": [], "ovirt_vms_res": [], "aws_ec2_key_res": [], "gcloud_gce_res": [], "aws_s3_res": [], "duffy_res": [], "os_sg_res": [], "dummy_res": [{"failed": false, "changed": true, "hosts": ["dummy-node-178e46-0.example.net"], "dummy_file": "/tmp/dummy.hosts"}], "beaker_res": [], "aws_cfn_res": [], "os_heat_res": [], "os_obj_res": [], "libvirt_res": [], "os_volume_res": []}}, "inputs": {"hooks_data": {"postup": [{"context": true, "type": "ansible", "name": "test", "actions": [{"extra_vars": {"test": "Hello World"}, "playbook": "site.yml"}]}]}, "layout_data": {"inventory_layout": {"hosts": [{"count": 1, "name": "beaker-slave", "host_groups": ["rhel7", "certificate_authority", "repositories", "jenkins_slave", "master_node"]}]}}, "topology_data": {"topology_name": "beaker-slave", "resource_groups": [{"resource_group_name": "dummy-slaves", "resource_definitions": [{"count": 1, "role": "dummy_node", "name": "dummy-node"}], "resource_group_type": "dummy"}]}}}}]}}
                '''
            }
            currentBuild = {
                [result:'']
            }
        }
    }
}

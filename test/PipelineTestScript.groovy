import java.util.logging.Logger
import groovy.json.JsonSlurper

/**
 * A mock for a pipeline script used to test the external API.
 */
@SuppressWarnings(['Instanceof', 'LineLength'])
class PipelineTestScript extends Script {

    private static final Logger LOG = Logger.getLogger(PipelineTestScript.name)

    @Override
    Object run() {
        null
    }

    Map<String, Integer> methodCallCounts = [
        ansiColor:0,
        checkout:0,
        containerTemplate:0,
        echo:0,
        error:0,
        file:0,
        git:0,
        node:0,
        parallel:0,
        podTemplate:0,
        readJSON:0,
        sh:0,
        stage:0,
        timestamps:0,
        usernamePassword:0,
        withCredentials:0,
    ]

    void resetCounts() {
        methodCallCounts.each {
            key, value ->
            methodCallCounts[key] = 0
        }
    }

    Closure ansiColor = {
        string, body ->
        methodCallCounts['ansiColor']++
        LOG.info("ansiColor(${string})")
        body()
    }

    Closure checkout = {
        scm ->
        methodCallCounts['checkout']++
        LOG.info("checkout(${scm})")
    }

    Closure containerTemplate = {
        map ->
        methodCallCounts['containerTemplate']++
        LOG.info('containerTemplate()')
    }

    Closure echo = {
        msg ->
        methodCallCounts['echo']++
        LOG.info("echo(${msg})")
    }

    Closure error = {
        error ->
        methodCallCounts['error']++
        LOG.severe("error(${error})")
    }

    Closure file = {
        cred ->
        methodCallCounts['file']++
        LOG.info("file(${cred.credentialsId})")
        binding.setProperty("${cred.variable}", "${cred.credentialsId}")
        cred.credentialsId
    }

    Closure git = {
        repo ->
        methodCallCounts['git']++
        LOG.info("git(${repo})")
    }

    Closure node = {
        name, body ->
        methodCallCounts['node']++
        LOG.info("node(${name})")
        body()
    }

    Closure parallel = {
        tasks ->
        methodCallCounts['parallel']++
        LOG.info('parallel()')
        tasks.each {
            task ->
            LOG.info("Running on Host: ${task.key}")
            task.value()
        }
    }

    Closure podTemplate = {
        map, body ->
        methodCallCounts['podTemplate']++
        LOG.info('podTemplate()')
        body()
    }

    Closure readJSON = {
        file ->
        methodCallCounts['readJSON']++
        LOG.info("readJSON(${file})")
        JsonSlurper slurper = new JsonSlurper()
        slurper.parseText(
        '''
        {"4": {"action": "up", "targets": [{"beaker-slave": {"1": {"uhash": "178e46", "rc": 0}, "outputs": {"inventory_path": ["/home/jpoulin/Projects/scratch/postup-hook-test/inventories/beaker-slave-178e46.inventory"], "resources": {"os_keypair_res": [], "rax_server_res": [], "aws_ec2_res": [], "os_server_res": [], "ovirt_vms_res": [], "aws_ec2_key_res": [], "gcloud_gce_res": [], "aws_s3_res": [], "duffy_res": [], "os_sg_res": [], "dummy_res": [{"failed": false, "changed": true, "hosts": ["dummy-node-178e46-0.example.net"], "dummy_file": "/tmp/dummy.hosts"}], "beaker_res": [], "aws_cfn_res": [], "os_heat_res": [], "os_obj_res": [], "libvirt_res": [], "os_volume_res": []}}, "inputs": {"hooks_data": {"postup": [{"context": true, "type": "ansible", "name": "test", "actions": [{"extra_vars": {"test": "Hello World"}, "playbook": "site.yml"}]}]}, "layout_data": {"inventory_layout": {"hosts": [{"count": 1, "name": "beaker-slave", "host_groups": ["rhel7", "certificate_authority", "repositories", "jenkins_slave", "master_node"]}]}}, "topology_data": {"topology_name": "beaker-slave", "resource_groups": [{"resource_group_name": "dummy-slaves", "resource_definitions": [{"count": 1, "role": "dummy_node", "name": "dummy-node"}], "resource_group_type": "dummy"}]}}}}]}}
       ''')
    }

    Closure sh = {
        sh ->
        methodCallCounts['sh']++
        if (sh instanceof Map) {
            LOG.info(sh.script)
            return 'hostname'
        }

        LOG.info(sh)
    }

    Closure stage = {
        stage, body ->
        methodCallCounts['stage']++
        LOG.info("stage(${stage})")
        body()
    }

    Closure timestamps = {
        body ->
        methodCallCounts['timestamps']++
        LOG.info('timestamps()')
        body()
    }

    Closure usernamePassword = {
        cred ->
        methodCallCounts['usernamePassword']++
        LOG.info("usernamePassword(${cred.credentialsId})")
        binding.setProperty("${cred.usernameVariable}", "${cred.credentialsId}")
        binding.setProperty("${cred.passwordVariable}", "${cred.credentialsId}")
        cred.credentialsId
    }

    Closure withCredentials = {
        credList, body ->
        methodCallCounts['withCredentials']++
        LOG.info("withCredentials(${credList})")
        body()
    }

    PipelineTestScript() {
        binding.with {
            currentBuild = {
                [result:'']
            }
            env = [
                environment:[:],
            ]
            params = [:]
            scm = [:]
        }
    }
}

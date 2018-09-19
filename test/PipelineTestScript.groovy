import java.util.logging.Logger
import groovy.json.JsonSlurper

/**
 * A mock for a pipeline script used to test the external API.
 */
@SuppressWarnings(['Instanceof', 'JavaIoPackageAccess'])
class PipelineTestScript extends Script {

    private static final Logger LOG = Logger.getLogger(PipelineTestScript.name)

    @Override
    Object run() {
        null
    }

    List<String> testLog = []

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
        scm:0,
        sh:0,
        stage:0,
        timestamps:0,
        usernamePassword:0,
        withCredentials:0,
    ]

    void reset() {
        resetCounts()
        testLog = []
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
        testLog.push(msg)
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
        slurper.parseText(this.getClass().getResource('resources/linchpin.latest').text)
    }

    Closure scm = {
        methodCallCounts['scm']++
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
        }
    }

    private void resetCounts() {
        methodCallCounts.each {
            key, value ->
            methodCallCounts[key] = 0
        }
    }
}

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

    void reset() {
        testLog = []
    }

    Closure ansiColor = {
        string, body ->
        LOG.info("ansiColor(${string})")
        body()
    }

    Closure checkout = {
        scm ->
        LOG.info("checkout(${scm})")
    }

    Closure containerTemplate = {
        map ->
        LOG.info('containerTemplate()')
    }

    Closure dir = {
        dir, body ->
        LOG.info("dir(${dir})")
        body()
    }

    Closure echo = {
        msg ->
        testLog.push(msg)
        LOG.info("echo(${msg})")
    }

    Closure error = {
        error ->
        LOG.severe("error(${error})")
    }

    Closure file = {
        cred ->
        LOG.info("file(${cred.credentialsId})")
        binding.setProperty("${cred.variable}", "${cred.credentialsId}")
        cred.credentialsId
    }

    Closure git = {
        repo ->
        LOG.info("git(${repo})")
    }

    Closure node = {
        name, body ->
        LOG.info("node(${name})")
        body()
    }

    Closure parallel = {
        tasks ->
        LOG.info('parallel()')
        tasks.each {
            task ->
            LOG.info("Running on Host: ${task.key}")
            task.value()
        }
    }

    Closure podTemplate = {
        map, body ->
        LOG.info('podTemplate()')
        body()
    }

    Closure readJSON = {
        file ->
        LOG.info("readJSON(${file})")
        JsonSlurper slurper = new JsonSlurper()
        slurper.parseText(this.getClass().getResource('linchpin.latest').text)
    }

    Closure sh = {
        sh ->
        if (sh instanceof Map) {
            LOG.info(sh.script)
            return 'hostname'
        }

        LOG.info(sh)
    }

    Closure stage = {
        stage, body ->
        LOG.info("stage(${stage})")
        body()
    }

    Closure timestamps = {
        body ->
        LOG.info('timestamps()')
        body()
    }

    Closure usernamePassword = {
        cred ->
        LOG.info("usernamePassword(${cred.credentialsId})")
        binding.setProperty("${cred.usernameVariable}", "${cred.credentialsId}")
        binding.setProperty("${cred.passwordVariable}", "${cred.credentialsId}")
        cred.credentialsId
    }

    Closure withCredentials = {
        credList, body ->
        LOG.info("withCredentials(${credList})")
        body()
    }

    PipelineTestScript() {
        binding.with {
            currentBuild = [
                result:'SUCCESS',
            ]
            env = [
                environment:[:],
            ]
            params = [:]
            scm = [:]
        }
    }
}

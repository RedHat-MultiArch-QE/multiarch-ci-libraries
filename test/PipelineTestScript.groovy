/**
 * A mock for a pipeline script used to test the external API.
 */
class PipelineTestScript extends Script {
    @Override
    Object run() {
        null
    }

    @SuppressWarnings('Instanceof')
    PipelineTestScript() {
        binding.with {
            params = [:]
            env = [environment:[:]]
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
                    return ''
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
        }
    }
}

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import java.util.logging.Logger
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mock
import com.redhat.ci.hosts.ProvisionedHost

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
@RunWith(MockitoJUnitRunner)
class TestUtilsTest extends Script {
    private static final Logger LOG = Logger.getLogger('TestUtilsTest')
    private final Binding binding = new Binding()
    @Mock
    private final ProvisionedHost host

    @Before
    void setUp() {
        setBinding(binding)
        binding.with {
            params = [:]
            env = [:]
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

    private final Closure body = {
        ProvisionedHost host, ProvisioningConfig config ->
        LOG.info('body(host, config)')
        LOG.info("Running on host ${host.id}")
    }

    private final Closure onFailure = {
        Exception e, ProvisionedHost host ->
        LOG.info('onFailure(e, host)')
        LOG.severe(e.toString())
        LOG.severe("Failed on host ${host.id} with exception")
    }

    private final Closure onComplete = {
        ->
        LOG.info('onComplete()')
    }

    @Test
    void shouldGetProvisioningConfig() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        assert(config != null)
    }

    @SuppressWarnings('JUnitPublicNonTestMethod')
    Object run() {
        null
    }

    @Test
    void shouldRunTestOnSingleHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runTest(this, 'x86_64', config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldRunTestOnMultiArchHosts() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runParallelMultiArchTest(
            this,
            ['x86_64', 'ppc64le', 'aarch64', 's390x'],
            config,
            body,
            onFailure,
            onComplete)
        assert(config != null)
    }
}

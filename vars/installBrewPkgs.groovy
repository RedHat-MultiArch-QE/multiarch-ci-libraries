import com.redhat.ci.Utils
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig

void call(Map params, Boolean privileged = false, ProvisioningConfig config = null, ProvisionedHost host = null) {
    Boolean taskRepoCreated = false
    if (params.CI_MESSAGE != '') {
        tid = getTaskId(params.CI_MESSAGE)
        createTaskRepo(taskIds:tid)
        taskRepoCreated = true
    } else if (params.TASK_ID != '') {
        createTaskRepo(taskIds:params.TASK_ID)
        taskRepoCreated = true
    }

    if (taskRepoCreated == true) {
        Utils.genericInstall(this, config ?: new ProvisioningConfig(), host) {
            privilegedOverride, sh ->
            context = [
                env:[HOME:env.HOME],
                files:['task-repo.properties'],
            ]
            String sudo = (privileged || privilegedOverride) ? 'sudo' : ''
            sh("""
                ${sudo} yum install -y yum-utils
                URL=\$(cat task-repo.properties | grep TASK_REPO_URLS= | sed 's/TASK_REPO_URLS=//' | sed 's/;/\\n/g')
                ${sudo} yum-config-manager --add-repo \${URL}
                ${sudo} cat /etc/yum.repos.d/*download.eng.bos.redhat.com*
                ${sudo} sed -i 's/gpgcheck=1/gpgcheck=0/g' /etc/yum.repos.d/*download.eng.bos.redhat.com*
                echo "gpgcheck=0" | ${sudo} tee -a /etc/yum.repos.d/*download.eng.bos.redhat.com*
                ${sudo} yum clean all
            """, context)
        }
    }
}

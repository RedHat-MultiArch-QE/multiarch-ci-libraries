package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import groovy.json.JsonOutput

/**
 * A base provisioner that defines shared utility methods to perform actions upon a provisioned host.
 */
abstract class AbstractProvisioner implements Provisioner {
    public static final String TEARDOWN_NOOP = 'Teardown NoOp'
    protected static final String ACTIVATE_VIRTUALENV = '. /home/jenkins/envs/provisioner/bin/activate; '
    protected static final String PROVISIONING_DIR = 'provisioning'

    Script script = null
    String type = null
    protected List<String> supportedHostTypes = []
    protected List<String> supportedProviders = []
    protected Boolean available = false

    protected AbstractProvisioner(Script script) {
        this.script = script
    }

    /**
     * Attempts to provision the TargetHost.
     *
     * @param target TargetHost Specifies parameters for the host to provision.
     */
    abstract ProvisionedHost provision(TargetHost target, ProvisioningConfig config)

    /**
     * Attempts a teardown of the ProvisionedHost.
     *
     * @param host ProvisionedHost Host to be torn down.
     */
    abstract void teardown(ProvisionedHost host, ProvisioningConfig config)

    /**
     * Determines whether a host type is supported for provisioning.
     */
    @Override
    Boolean supportsHostType(String hostType) {
        supportedHostTypes.contains(hostType)
    }

    /**
     * Filters a list of host types to those that are supported for provisioning.
     */
    @Override
    List<String> filterSupportedHostTypes(List<String> hostTypes) {
        hostTypes.findAll {
            hostType ->
            supportsHostType(hostType)
        }
    }

    /**
     * Determines whether a provider is supported for provisioning.
     */
    @Override
    Boolean supportsProvider(String provider) {
        supportedProviders.contains(provider)
    }

    /**
     * Getter for availability status.
     */
    @Override
    Boolean getAvailable() {
        this.@available
    }

    /**
     * Injects user credentials to create extra vars needed for Cinch.
     */
    protected String getCinchExtraVars(ProvisionedHost host, ProvisioningConfig config) {
        script.withCredentials([
            script.usernamePassword(credentialsId:config.jenkinsSlaveCredentialId,
                                    usernameVariable:'JENKINS_SLAVE_USERNAME',
                                    passwordVariable:'JENKINS_SLAVE_PASSWORD'),
        ]) {
            Map extraVars = [
                'rpm_key_imports':[],
                'jenkins_master_repositories':[],
                'jenkins_master_download_repositories':[],
                'jslave_name':"${host.displayName}",
                'jslave_label':"${host.displayName}",
                'arch':"${host.arch}",
                'jenkins_master_url':"${config.jenkinsMasterUrl}",
                'jenkins_slave_username':"${script.JENKINS_SLAVE_USERNAME}",
                'jenkins_slave_password':"${script.JENKINS_SLAVE_PASSWORD}",
                'jswarm_version':"${config.jswarmVersion}",
                'jswarm_filename':'swarm-client-{{ jswarm_version }}.jar',
                'jswarm_extra_args':"${config.jswarmExtraArgs}",
                'jenkins_slave_repositories':[[
                    'name':'epel',
                    'mirrorlist':'https://mirrors.fedoraproject.org/metalink?arch=\$basearch&repo=epel-7'
                ]]
            ]

            JsonOutput.toJson(extraVars)
        }
    }
}

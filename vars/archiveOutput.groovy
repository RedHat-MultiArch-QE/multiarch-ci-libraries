void call() {
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
        echo "Ignoring empty archive/report warning: ${e}"
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
        echo "Ignoring empty archive/report warning: ${e}"
    }
}

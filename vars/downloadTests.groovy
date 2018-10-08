void call() {
    if (params.TEST_REPO) {
        git(url:params.TEST_REPO, branch:params.TEST_REF, changelog:false)
    }
    else {
        checkout(scm)
    }
}

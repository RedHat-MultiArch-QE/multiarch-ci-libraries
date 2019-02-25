Map call(String message) {
    if (!message) {
        return [:]
    }

    final String CI_MESSAGE_FILE = 'message.json'
    writeFile(file:CI_MESSAGE_FILE, text:message)
    readJSON(file:CI_MESSAGE_FILE)
}

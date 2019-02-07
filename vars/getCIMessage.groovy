Map call(String message) {
    final String CI_MESSAGE_FILE = 'message.json'
    if (!message) {
        return [:]
    }
    
    writeFile(file:CI_MESSAGE_FILE, text:message)
    readJSON(file:CI_MESSAGE_FILE)
}

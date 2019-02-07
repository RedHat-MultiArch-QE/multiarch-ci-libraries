String call(String message='') {
    final String CI_MESSAGE_FILE = 'message.json'
    writeFile(file:CI_MESSAGE_FILE, text:params.CI_MESSAGE)

    Map json = readJSON(file:CI_MESSAGE_FILE)
    return json
}

String call(String message='') {
    final String CI_MESSAGE_FILE = 'message.json'
    echo("CI_MESSAGE=${message}")
    writeFile(file:CI_MESSAGE_FILE, text:message)

    Map json = readJSON(file:CI_MESSAGE_FILE)
    tid = json['build'].task_id

    tid.toString()
}

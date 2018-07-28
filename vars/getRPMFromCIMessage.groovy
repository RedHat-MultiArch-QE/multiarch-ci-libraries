void call(String message='', String arch='') {
    final String CI_MESSAGE_FILE = 'message.json'
    echo("CI_MESSAGE=${message}")
    writeFile(file:CI_MESSAGE_FILE, text:message)

    Map json = readJSON(file:CI_MESSAGE_FILE)
    json['rpms'][arch].each { rpm ->
        echo("downloading: ${rpm}")
        sh """
           brew download-build --rpm ${rpm}
        """
    }
}

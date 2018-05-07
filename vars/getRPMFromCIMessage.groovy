import groovy.json.*

def call(String message='', String arch='') {
  println "CI_MESSAGE=${message}"
  writeFile file: "message.json", text: message

  def json = readJSON file: 'message.json'
  json['rpms'][arch].each { rpm ->
    println "downloading: ${rpm}"
    sh """
      brew download-build --rpm ${rpm}
    """
  }
}

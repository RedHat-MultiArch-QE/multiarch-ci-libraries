import groovy.json.*

def call(String message='') {
  println "CI_MESSAGE=${message}"
  writeFile file: "message.json", text: message

  def json = readJSON file: 'message.json'
  tid = json['build'].task_id

  return tid.toString()
}

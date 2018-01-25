import groovy.json.*

def call(String message='', String arch='') {
    println "CI_MESSAGE=${message}"

    def json = new JsonSlurperClassic().parseText(message)
    json['rpms'][arch].each { rpm ->
        println "downloading: ${rpm}"
        sh """
           brew download-build --rpm ${rpm}
        """
    }
}

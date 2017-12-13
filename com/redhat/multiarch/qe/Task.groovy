pacakge com.redhat.multiarch.qe;

class Task {
    def String name;
    def LinkedHashMap params;

    Task(String name, LinkedHashMap params) {
        this.name = name;
        this.params = params;
    }
}

package com.redhat.multiarch.ci.task

class Task {
  String name
  Map params

  /**
   * Calls a @body for each task in @tasks.
   *
   * @param script Context to call pipeline steps.
   * @param tasks List<Task> to be parallelized.
   * @param body Closure that takes a single parameter representing a Map of arguments.
   */
  static def parallelize(def script, List<Task> tasks, Closure body) {
    def parallelTasks = [:]
    for (task in tasks) {
      parallelTasks[task.name] = body(task.params)
    }

    script.parallel parallelTasks
  }
}

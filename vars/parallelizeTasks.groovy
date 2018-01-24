/**
 * parallelizeTasks.groovy
 *
 * Calls a @body for each task in @tasks.
 *
 * @param tasks List<Task> to be parallelized.
 * @param body Closure that takes a single parameter representing a Map of arguments.
 */
import com.redhat.multiarch.ci.task.Task

def call(List<Task> tasks, Closure body) {
  def parallelTasks = [:]
  for (task in tasks) {
    parallelTasks[task.name] = body(task.params)
  }

  parallel parallelTasks
}

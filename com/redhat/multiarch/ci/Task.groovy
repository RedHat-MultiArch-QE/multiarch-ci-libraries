package com.redhat.multiarch.ci

class Task {
  def String name;
  def LinkedHashMap params;

  Task(String name, LinkedHashMap params) {
    this.name = name;
    this.params = params;
  }
}

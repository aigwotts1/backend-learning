package com.aigwotts1.tasktracker.task;


/*Java Records, finalized in Java 16 via JEP 395,
 are a special type of class designed to act as immutable data carriers with minimal boilerplate.
 They allow you to define a pure data holder—like a Plain Old Java Object (POJO) .*/
public record Task(long id, String title, boolean completed) {
}

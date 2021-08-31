package com.broderickwestrope.whiteboard.todo_list.Models;

// The model of what a task will contain
public class TaskModel {
    /*
    id - This is the auto-incremented value created by the database manager to identify each task
    task - This is the name/description of the task and is seen as the main piece of text
    location - This is the location that the task is to take place. It is displayed under then task text and is optional
    status - This is the boolean value of whether or not the task is complete, toggled with a checkbox
    */

    private int id, status;
    private String task;
    private String location;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

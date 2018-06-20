package com.example.mw5.todolist;

import java.sql.Date;

public class TodoTask {
    private long id;
    private String description;
    private boolean completed;
    private long createdAt;
    private long due;
    private int priority;

    public TodoTask(long id, String description, boolean completed, long createdAt, long due, int priority) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.due = due;
        this.priority = priority;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreated_at() {
        return createdAt;
    }
    public void setCreated_at(long created_at) {
        this.createdAt = created_at;
    }

    public long getDue() {
        return due;
    }
    public void setDue(long due) {
        this.due = due;
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
}

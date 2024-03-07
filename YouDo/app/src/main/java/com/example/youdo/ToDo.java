package com.example.youdo;


public class ToDo {
    private int todoId;
    private String googleTodoId;
    private String name;
    private String date;
    private String time;
    private String description;
    private int typeId;
    private int userId;
    private boolean done;

    // functions

    public ToDo() {
    }
    public int getTodoId() {
        return todoId;
    }
    public void setTodoId(int todoId) {
        this.todoId = todoId;
    }
    public String getName() {
        return name;
    }
    public void setName( String name) {
        this.name = name;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getTypeId() {
        return typeId;
    }
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGoogleTodoId() {
        return googleTodoId;
    }

    public void setGoogleTodoId(String googleTodoId) {
        this.googleTodoId = googleTodoId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}

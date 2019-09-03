package org.apache.cordova.todo.models;

import com.google.gson.Gson;

public class Todo {
    public Long id;
    public String label;
    public Boolean checked;

    static private Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }

    public static Todo fromJson(String json) {
        if (json == null) return null;
        return gson.fromJson(json, Todo.class);
    }
}

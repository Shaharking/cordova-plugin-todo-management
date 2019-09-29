package org.apache.cordova.todo.models;

import com.google.gson.Gson;

/**
* Todo is the main entity we'll be using to presnet our todos
*
* @author Shahar Shalev
* 
*/
public class Todo {
    /**The id of the todo */
    public Long id;
    /** The text of the todo */
    public String label;
    /** Wheter it's done or not */
    public Boolean checked;

    static private Gson gson = new Gson();

    /**
     * 
     * @return json string of the object
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /**
     * 
     * @param json
     * @return Todo item from the json string of the todo.
     */
    public static Todo fromJson(String json) {
        if (json == null) return null;
        return gson.fromJson(json, Todo.class);
    }
}

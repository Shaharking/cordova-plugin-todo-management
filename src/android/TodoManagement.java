package org.apache.cordova.todo;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.content.ContentValues;

import com.google.gson.Gson;

import org.apache.cordova.todo.models.Todo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class TodoManagement extends CordovaPlugin {

    private DatabaseHelper databaseHelper = null;

    private Gson gson = new Gson();

    public TodoManagement()
    {

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getInstance(this.cordova.getContext());
        }

        if (action.equals("get")) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Todo> todoList = databaseHelper.getAllTodos();
                        String jsonString = gson.toJson(todoList);
                        JSONArray r = new JSONArray(jsonString);
                        callbackContext.success(r);
                    }
                    catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("getById"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        long id = todo.getLong("id");
                        Todo todoItem = databaseHelper.getById(id);
                        String jsonString = todoItem.toJson();
                        JSONObject r = new JSONObject(jsonString);
                        callbackContext.success(r);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("post") || action.equals("put"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable(){
                public void run() {
                    try {
                        Todo todoItem = Todo.fromJson(todo.toString());
                        long result = databaseHelper.addOrUpdateTodo(todoItem);
                        callbackContext.success();
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if(action.equals("delete"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable(){
                public void run() {
                    try {
                        long id = todo.getLong("id");
                        databaseHelper.deleteTodo(id);
                        callbackContext.success();
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }

                }
            });
            return true;
        }
        return false;
    }
}

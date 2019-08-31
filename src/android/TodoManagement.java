package org.apache.cordova.todo.TodoManagement

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class TodoManagement extends CordovaPlugin {

    public SQLiteDatabase TodoDb;

    public TodoManagement()
    {
        TodoDb = SQLiteDatabase.openOrCreateDatabase("todo.db", null);
        TodoDb.execSQL("CREATE TABLE IF NOT EXISTS Todos(id INT PRIMARY KEY NOT NULL, label TEXT, checked INT NOT NULL);");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("get")) {
            JSONArray r = this.getTodoList();
            callbackContext.success(r);
            return true;
        }
        if (action.equals("post"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable(){
                public void run() {
                    try {
                        Integer id = todo.getInt("id");
                        String label = todo.getString("label");
                        Integer checked = todo.getInt("checked");
                        insertTodo(id, label, checked);
                        callbackContext.success(true);
                    } catch (JSONException e) {
                        callbackContext.error("Error at post");
                        e.printStackTrace();
                    }

                }
            });

        }
        if (action.equals("put"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable(){
                public void run() {
                    try {
                        Integer id = todo.getInt("id");
                        String label = todo.getString("label");
                        Integer checked = todo.getInt("checked");
                        updateTodo(id, label, checked);
                        callbackContext.success(true);
                    } catch (JSONException e) {
                        callbackContext.error("Error at put");
                        e.printStackTrace();
                    }

                }
            });
        }
        if(action.equals("delete"))
        {
            JSONObject todo = args.getJSONObject(0);
            this.cordova.getThreadPool().execute(new Runnable(){
                public void run() {
                    try {
                        Integer id = todo.getInt("id");
                        deleteTodo(id);
                        callbackContext.success(true);
                    } catch (JSONException e) {
                        callbackContext.error("Error at delete");
                        e.printStackTrace();
                    }

                }
            });
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    public JSONArray getTodoList()
    {

        String searchQuery = "select * from Todos";
        Cursor cursor = TodoDb.rawQuery(searchQuery, null);

        JSONArray resultSet = new JSONArray();
        JSONObject returnObj = new JSONObject();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {

                    try {

                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d("TAG_NAME", resultSet.toString());
        return resultSet;
    }

    public boolean insertTodo (Integer id, String label, Integer checked) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("label", label);
        contentValues.put("checked", checked);
        TodoDb.insert("Todos", null, contentValues);
        return true;
    }

    public boolean updateTodo (Integer id, String label, Integer checked) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("label", label);
        contentValues.put("checked", checked);
        TodoDb.update("Todos", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteTodo (Integer id) {
        return TodoDb.delete("Todos",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
}

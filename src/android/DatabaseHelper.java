package org.apache.cordova.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.apache.cordova.todo.models.Todo;

import java.util.ArrayList;
import java.util.List;

/**
* DatabaseHelper is the main entity we'll be using to work with our database
*
* @author Shahar Shalev
* 
*/
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * The instance of class , we use singleton pattern here
     */
    private static DatabaseHelper sInstance;

    /**
     * The name of our class file, for logging
     */
    private final String TAG_DATABASEHELPER = "DatabaseHelper";
    // Database Info
    /** Database name */
    private static final String DATABASE_NAME = "todosDatabase";
    /** Database version */
    private static final int DATABASE_VERSION = 1;

    // Table Names
    /** Single table - todos which we will save everyting in it. */
    private static final String TABLE_TODOS = "todos";

    // Todos Table Columns
    /** The id of the todo */
    private static final String KEY_TODO_ID = "id";
    /** The text of the todo */
    private static final String KEY_TODO_LABEL = "label";
    /** Is checked or not */
    private static final String KEY_TODO_CHECKED = "checked";
    /**
     * 
     * Create or get the DatabaseHelper instance 
     * @param context  application context
     * @return DatabaseHelper instance
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** 
     * Called when the database connection is being configured.
     * Configure database settings for things like foreign key support, write-ahead logging, etc.
     */ 
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Called when the database is created for the FIRST time.
     * If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODO_TABLE = "CREATE TABLE " + TABLE_TODOS +
                "(" +
                    KEY_TODO_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                    KEY_TODO_LABEL + " TEXT," + // Define a foreign key
                    KEY_TODO_CHECKED + " INTEGER" +
                ")";

        db.execSQL(CREATE_TODO_TABLE);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will only be called if a database already exists on disk with the same DATABASE_NAME,
     * but the DATABASE_VERSION is different than the version of the database that exists on disk.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
            onCreate(db);
        }
    }
    /**
     * 
     * @param todo The todo which we be added/updated to our db.
     * @return id of the the todo
     */
    public long addOrUpdateTodo(Todo todo)
    {
        SQLiteDatabase db = getWritableDatabase();
        long todoId = -1;

        db.beginTransaction();
        try {
            // Saving key, value infomation for the todo create/update query
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_ID, todo.id);
            values.put(KEY_TODO_LABEL, todo.label);
            values.put(KEY_TODO_CHECKED, todo.checked);

            // First try to update the todo in case the todo already exists in the database
            // This assumes todo's id are unique
            int rows = db.update(TABLE_TODOS, values, KEY_TODO_ID + "= ?", new String[]{todo.id.toString()});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the todo we just updated
                String todosSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_TODO_ID, TABLE_TODOS, KEY_TODO_ID);
                Cursor cursor = db.rawQuery(todosSelectQuery, new String[]{String.valueOf(todo.id)});
                try {
                    if (cursor.moveToFirst()) {
                        todoId = cursor.getLong(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // todo with this that id did not already exist, so insert new todo
                todoId = db.insertOrThrow(TABLE_TODOS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG_DATABASEHELPER, "Error while trying to add or update todo");
        } finally {
            db.endTransaction();
        }
        return todoId;
    }

    /**
     *  Get a single Todo object by his id.
     * @param id the id of the todo
     * @return the todo by his id.
     */
    public Todo getById(long id)
    {
        Todo newTodo = null;
        // The query to get the todo from db.
        String TODO_SELECT_QUERY =
                String.format("SELECT * FROM %s where %s = ?",
                        TABLE_TODOS,
                        KEY_TODO_ID);


        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODO_SELECT_QUERY, new String[] {String.valueOf(id)});
        
        // Extracting the the todo from the result of the query.
        try {
            if (cursor.moveToFirst()) {
                newTodo = new Todo();
                newTodo.id = cursor.getLong(cursor.getColumnIndex(KEY_TODO_ID));
                newTodo.label = cursor.getString(cursor.getColumnIndex(KEY_TODO_LABEL));
                newTodo.checked = cursor.getInt(cursor.getColumnIndex(KEY_TODO_CHECKED)) == 1;
            }
        } catch (Exception e) {
            Log.d(TAG_DATABASEHELPER, "Error while trying to get single todo from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return newTodo;
    }

    /**
     * 
     * @return All the todos from our db
     */
    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();

        String TODOS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_TODOS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODOS_SELECT_QUERY, null);

        // Going over the results and add them to the list of todos
        try {
            if (cursor.moveToFirst()) {
                do {
                    Todo newTodo = new Todo();
                    newTodo.id = cursor.getLong(cursor.getColumnIndex(KEY_TODO_ID));
                    newTodo.label = cursor.getString(cursor.getColumnIndex(KEY_TODO_LABEL));
                    newTodo.checked = cursor.getInt(cursor.getColumnIndex(KEY_TODO_CHECKED)) == 1;

                    todos.add(newTodo);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG_DATABASEHELPER, "Error while trying to get all todo from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return todos;
    }

    /**
     * Delete a todo from our db by his id
     * @param id of the todo
     */
    public void deleteTodo(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_TODOS, KEY_TODO_ID+"=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG_DATABASEHELPER, "Error while trying to delete todo");
        } finally {
            db.endTransaction();
        }
    }
}

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

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    private final String TAG_DATABASEHELPER = "DatabaseHelper";
    // Database Info
    private static final String DATABASE_NAME = "todosDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TODOS = "todos";

    // Todos Table Columns
    private static final String KEY_TODO_ID = "id";
    private static final String KEY_TODO_LABEL = "label";
    private static final String KEY_TODO_CHECKED = "checked";

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

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
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

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
            onCreate(db);
        }
    }

    public long addOrUpdateTodo(Todo todo)
    {
        SQLiteDatabase db = getWritableDatabase();
        long todoId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_ID, todo.id);
            values.put(KEY_TODO_LABEL, todo.label);
            values.put(KEY_TODO_CHECKED, todo.checked);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_TODOS, values, KEY_TODO_ID + "= ?", new String[]{todo.id.toString()});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
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
                // user with this userName did not already exist, so insert new user
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

    public Todo getById(long id)
    {
        Todo newTodo = null;
        String TODO_SELECT_QUERY =
                String.format("SELECT * FROM %s where %s = ?",
                        TABLE_TODOS,
                        KEY_TODO_ID);


        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODO_SELECT_QUERY, new String[] {String.valueOf(id)});

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

    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String TODOS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_TODOS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODOS_SELECT_QUERY, null);
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

package com.example.mw5.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DbAdapter {
    private static final String DEBUG_TAG = "SqLiteTodoManager";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "database.db";
    private static final String DB_TODO_TABLE = "todo";

    //id
    public static final String KEY_ID = "_id";
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;

    //description
    public static final String KEY_DESCRIPTION = "description";
    public static final String DESCRIPTION_OPTIONS = "TEXT NOT NULL";
    public static final int DESCRIPTION_COLUMN = 1;

    //completed
    public static final String KEY_COMPLETED = "completed";
    public static final String COMPLETED_OPTIONS = "INTEGER DEFAULT 0";
    public static final int COMPLETED_COLUMN = 2;

    //created at
    public static final String KEY_CREATED_AT = "created_at";
    public static final String CREATED_AT_OPTIONS = "DATETIME DEFAULT CURRENT_TIMESTAMP";
    public static final int CREATED_AT_COLUMN = 3;

    //due
    public static final String KEY_DUE = "due";
    public static final String DUE_OPTIONS = "DATETIME NOT NULL";
    public static final int DUE_COLUMN = 4;

    //priority
    public static final String KEY_PRIORITY = "priority";
    public static final String PRIORITY_OPTIONS = "INTEGER NOT NULL";
    public static final int PRIORITY_COLUMN = 5;



    private static final String DB_CREATE_TODO_TABLE =
            "CREATE TABLE " + DB_TODO_TABLE + "( " +
                    KEY_ID + " " + ID_OPTIONS + ", " +
                    KEY_DESCRIPTION + " " + DESCRIPTION_OPTIONS + ", " +
                    KEY_COMPLETED + " " + COMPLETED_OPTIONS +
                    ");";
    private static final String DROP_TODO_TABLE =
            "DROP TABLE IF EXISTS " + DB_TODO_TABLE;

    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TODO_TABLE);

            Log.d(DEBUG_TAG, "Database creating...");
            Log.d(DEBUG_TAG, "Table " + DB_TODO_TABLE + " ver." + DB_VERSION + " created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TODO_TABLE);

            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + DB_TODO_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");

            onCreate(db);
        }
    }

    public DbAdapter(Context context) {
        this.context = context;
    }

    public DbAdapter open(){
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertTodo(String description) {
        ContentValues newTodoValues = new ContentValues();
        newTodoValues.put(KEY_DESCRIPTION, description);
        return db.insert(DB_TODO_TABLE, null, newTodoValues);
    }

    public boolean updateTodo(TodoTask task) {
        long id = task.getId();
        String description = task.getDescription();
        boolean completed = task.isCompleted();
        return updateTodo(id, description, completed);
    }

    public boolean updateTodo(long id, String description, boolean completed) {
        String where = KEY_ID + "=" + id;
        int completedTask = completed ? 1 : 0;
        ContentValues updateTodoValues = new ContentValues();
        updateTodoValues.put(KEY_DESCRIPTION, description);
        updateTodoValues.put(KEY_COMPLETED, completedTask);
        return db.update(DB_TODO_TABLE, updateTodoValues, where, null) > 0;
    }

    public boolean deleteTodo(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(DB_TODO_TABLE, where, null) > 0;
    }

    public Cursor getAllTodos() {
        String[] columns = {KEY_ID, KEY_DESCRIPTION, KEY_COMPLETED};
        return db.query(DB_TODO_TABLE, columns, null, null, null, null, null);
    }

    public TodoTask getTodo(long id) {
        String[] columns = {KEY_ID, KEY_DESCRIPTION, KEY_COMPLETED};
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(DB_TODO_TABLE, columns, where, null, null, null, null);
        TodoTask task = null;
        if(cursor != null && cursor.moveToFirst()) {
            String description = cursor.getString(DESCRIPTION_COLUMN);
            boolean completed = cursor.getInt(COMPLETED_COLUMN) > 0 ? true : false;
            task = new TodoTask(id, description, completed);
        }
        return task;
    }
}

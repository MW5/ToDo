package com.example.mw5.todolist;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton fab;
    ListView taskList;

    private DbAdapter dBAdapter;
    private Cursor todoCursor;
    private List<TodoTask> tasks;
    private TodoTasksAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //floating action bar
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //maka data form here

                //for testing purposes only sets due to current time
                Long date = System.currentTimeMillis();
                dBAdapter.insertTodo("bagno2",date, date, 0);
                updateListViewData();
            }
        });

        //task list
        taskList = (ListView) findViewById(R.id.taskList);
        initListView();

    }

    //top right dropdown
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "dupa", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //top right dropdown end

    private void initListView() {
        fillListViewData();
    }

    private void updateListViewData() {
        todoCursor.requery();
        tasks.clear();
        updateTaskList();
        listAdapter.notifyDataSetChanged();
    }

    private void fillListViewData() {
        dBAdapter = new DbAdapter(getApplicationContext());
        dBAdapter.open();
        getAllTasks();
        listAdapter = new TodoTasksAdapter(this, tasks, dBAdapter);
        taskList.setAdapter(listAdapter);
    }

    private void getAllTasks() {
        tasks = new ArrayList<TodoTask>();
        todoCursor = getAllEntriesFromDb();
        updateTaskList();
    }

    private Cursor getAllEntriesFromDb() {
        todoCursor = dBAdapter.getAllTodos();
        if(todoCursor != null) {
            startManagingCursor(todoCursor);
            todoCursor.moveToFirst();
        }
        return todoCursor;
    }

    private void updateTaskList() {
        if(todoCursor != null && todoCursor.moveToFirst()) {
            do {
                long id = todoCursor.getLong(dBAdapter.ID_COLUMN);
                String description = todoCursor.getString(dBAdapter.DESCRIPTION_COLUMN);
                boolean completed = todoCursor.getInt(dBAdapter.COMPLETED_COLUMN) > 0 ? true : false;
                long createdAt = todoCursor.getLong(dBAdapter.CREATED_AT_COLUMN);
                long due = todoCursor.getLong(dBAdapter.DUE_COLUMN);
                int priority = todoCursor.getInt(dBAdapter.PRIORITY_COLUMN);
                tasks.add(new TodoTask(id, description, completed, createdAt, due, priority));
            } while(todoCursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        if(dBAdapter != null)
            dBAdapter.close();
        super.onDestroy();
    }
}

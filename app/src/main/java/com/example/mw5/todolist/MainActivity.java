package com.example.mw5.todolist;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton fab;
    ListView taskList;
    ArrayAdapter<String> aa;
    ArrayList<String> arrayList;

    private DbAdapter DbAdapter;
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

        //fab
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //test add
                DbAdapter.insertTodo("bagno2");
                updateListViewData();
            }
        });

        //test list
        taskList = (ListView) findViewById(R.id.taskList);

        arrayList = new ArrayList<String>();
        arrayList.add("test");
        arrayList.add("test2");



        //taskList.setAdapter(aa);
        //CustomAdapter adapter = new CustomAdapter(arrayList, this);

        //handle listview and assign adapter
        //taskList.setAdapter(adapter);

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
        DbAdapter = new DbAdapter(getApplicationContext());
        DbAdapter.open();
        getAllTasks();
        listAdapter = new TodoTasksAdapter(this, tasks);
        taskList.setAdapter(listAdapter);
    }

    private void getAllTasks() {
        tasks = new ArrayList<TodoTask>();
        todoCursor = getAllEntriesFromDb();
        updateTaskList();
    }

    private Cursor getAllEntriesFromDb() {
        todoCursor = DbAdapter.getAllTodos();
        if(todoCursor != null) {
            startManagingCursor(todoCursor);
            todoCursor.moveToFirst();
        }
        return todoCursor;
    }

    private void updateTaskList() {
        if(todoCursor != null && todoCursor.moveToFirst()) {
            do {
                long id = todoCursor.getLong(DbAdapter.ID_COLUMN);
                String description = todoCursor.getString(DbAdapter.DESCRIPTION_COLUMN);
                boolean completed = todoCursor.getInt(DbAdapter.COMPLETED_COLUMN) > 0 ? true : false;
                tasks.add(new TodoTask(id, description, completed));
            } while(todoCursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        if(DbAdapter != null)
            DbAdapter.close();
        super.onDestroy();
    }
}

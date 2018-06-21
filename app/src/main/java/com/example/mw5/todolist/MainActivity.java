package com.example.mw5.todolist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton fabAddTask;
    ListView taskList;
    LinearLayout addTaskForm;

    //add form
    EditText addDescriptionEt;
    CalendarView addCalendar;
    RadioGroup addRadioGroup;
    Button confirmAddBtn;
    Button rejectAddBtn;

    private DbAdapter dBAdapter;
    private Cursor todoCursor;
    private List<TodoTask> tasks;
    private TodoTasksAdapter listAdapter;

    long due;

    private boolean sortByCreatedAtAsc;
    private boolean sortByDescriptionAsc;
    private boolean sortByPriorityAsc;
    private boolean sortByDueAsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sortByCreatedAtAsc = false;
        sortByDescriptionAsc = false;
        sortByPriorityAsc = false;
        sortByDueAsc = false;

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //add task form
        addTaskForm = (LinearLayout) findViewById(R.id.addTaskForm);

        //add task form content
        addDescriptionEt = (EditText) findViewById(R.id.addDescriptionEt);
        addCalendar = (CalendarView) findViewById(R.id.addCalendar);
        addRadioGroup = (RadioGroup) findViewById(R.id.addRadioGroup);
        confirmAddBtn = (Button) findViewById(R.id.confirmAddBtn);
        rejectAddBtn = (Button) findViewById(R.id.rejectAddBtn);

        //floating action bar
        fabAddTask = (FloatingActionButton) findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabAddTask.setVisibility(View.INVISIBLE);
                addTaskForm.setVisibility(View.VISIBLE);
            }
        });

        //set default due
        due = System.currentTimeMillis();
        addCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                Date dueDate = new Date(year-1900,month,dayOfMonth);
                due = dueDate.getTime();
            }
        });

        //confirm add form
        confirmAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //description
                String descriptionText = addDescriptionEt.getText().toString();
                //created at
                Long currDate = System.currentTimeMillis();
                //priority
                int radioButtonID = addRadioGroup.getCheckedRadioButtonId();
                View radioButton = addRadioGroup.findViewById(radioButtonID);
                int selectedPriority = addRadioGroup.indexOfChild(radioButton);

                //database insert
                if (descriptionText.length()>0 && descriptionText.length()<=20) {
                    if (dBAdapter.insertTodo(descriptionText, currDate, due, selectedPriority) > 0) {
                        updateListViewData();
                        addTaskForm.setVisibility(View.INVISIBLE);
                        fabAddTask.setVisibility(View.VISIBLE);
                        addDescriptionEt.setText("");
                        ((RadioButton) addRadioGroup.getChildAt(1)).setChecked(true);
                    } else {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Nazwa zadania musi mieć od 1-20 znaków",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        rejectAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskForm.setVisibility(View.INVISIBLE);
                fabAddTask.setVisibility(View.VISIBLE);
            }
        });

        //task list
        taskList = (ListView) findViewById(R.id.taskList);
        initListView();
    }

    //top right dropdown
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sortByCreatedAt) {
            if (!sortByCreatedAtAsc) {
                updateListViewDataBy("created_at", "ASC");
                sortByCreatedAtAsc = true;
            } else {
                updateListViewDataBy("created_at", "DESC");
                sortByCreatedAtAsc = false;
            }
            return true;
        }
        if (id == R.id.sortByDescription) {
            if (!sortByDescriptionAsc) {
                updateListViewDataBy("description", "ASC");
                sortByDescriptionAsc = true;
            } else {
                updateListViewDataBy("description", "DESC");
                sortByDescriptionAsc = false;
            }
            return true;
        }
        if (id == R.id.sortByPriority) {
            if (!sortByPriorityAsc) {
                updateListViewDataBy("priority", "ASC");
                sortByPriorityAsc = true;
            } else {
                updateListViewDataBy("priority", "DESC");
                sortByPriorityAsc = false;
            }
            return true;
        }
        if (id == R.id.sortByDue) {
            if (!sortByDueAsc) {
                updateListViewDataBy("due", "ASC");
                sortByDueAsc = true;
            } else {
                updateListViewDataBy("due", "DESC");
                sortByDueAsc = false;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initListView() {
        fillListViewData();
    }

    private void updateListViewData() {
        todoCursor.requery();
        tasks.clear();
        updateTaskList();
        listAdapter.notifyDataSetChanged();
    }

    private void updateListViewDataBy(String criteria, String order) {
        todoCursor = dBAdapter.getAllTodosByOrder(criteria, order);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                updateListViewData();
            }
        }
    }

}

package com.example.mw5.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private Calendar newDayCalendar;
    private Timer notificationTimer;
    long due;

    private boolean sortByCreatedAtAsc;
    private boolean sortByDescriptionAsc;
    private boolean sortByPriorityAsc;
    private boolean sortByDueAsc;

    private boolean pastDueNotificationSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set sort flags
        sortByCreatedAtAsc = false;
        sortByDescriptionAsc = false;
        sortByPriorityAsc = false;
        sortByDueAsc = false;

        //set past due notification flag
        pastDueNotificationSent = false;

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

        //checks for a new day every minute
        final Handler checkNewDayHandler = new Handler();
        checkNewDayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkForNewDay()) {
                    updateTaskList();
                };
                checkNewDayHandler.postDelayed(this, 60000);
            }
        }, 10000);

        //set default due
        due = System.currentTimeMillis();
        //calendar handler
        addCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                Date dueDate = new Date(year-1900,month,dayOfMonth);
                due = dueDate.getTime();
            }
        });

        //confirm add form
        //confirm add btn handler
        confirmAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //prepare description
                String descriptionText = addDescriptionEt.getText().toString();
                //prepare created at
                Long currDate = System.currentTimeMillis();
                //prepare priority
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
                        Toast.makeText(MainActivity.this, R.string.sth_went_wrong, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.description_too_short,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //reject btn handler
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
                if (due < System.currentTimeMillis() && !completed && !pastDueNotificationSent) {
                    sendPastDueNotification();
                    pastDueNotificationSent = true;
                }
                int priority = todoCursor.getInt(dBAdapter.PRIORITY_COLUMN);
                tasks.add(new TodoTask(id, description, completed, createdAt, due, priority));
            } while(todoCursor.moveToNext());
        }
    }

    //check if new day
    private boolean checkForNewDay() {
        newDayCalendar = Calendar.getInstance();
        int thisDay = newDayCalendar.get(Calendar.DAY_OF_YEAR);
        long todayMillis = newDayCalendar.getTimeInMillis();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        long last = prefs.getLong("date", System.currentTimeMillis());

        newDayCalendar.setTimeInMillis(last);

        int lastDay = newDayCalendar.get(Calendar.DAY_OF_YEAR);
        if (lastDay == thisDay) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putLong("date", todayMillis + 86400000);
            edit.commit();
            return true;
        } else {
            return false;
        }
    }

    public void sendPastDueNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(r.getString(R.string.past_due_notification_title))
                .setContentText(r.getString(R.string.past_due_notification_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    @Override
    protected void onDestroy() {
        if(dBAdapter != null)
            dBAdapter.close();
        super.onDestroy();
    }

    //update list view on EditActivity triggered by ToDoTaskAdapter row click handler intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                updateListViewData();
            }
        }
    }
}

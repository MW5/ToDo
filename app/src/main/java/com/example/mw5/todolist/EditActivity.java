package com.example.mw5.todolist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.Calendar;

public class EditActivity extends AppCompatActivity {
    private DbAdapter dBAdapter;
    private MainActivity mainActivity;

    LinearLayout editTaskForm;
    //edit form
    TextView createdAtText;
    Switch doneSwitch;
    EditText editDescriptionEt;
    CalendarView editCalendar;
    RadioGroup editRadioGroup;
    Button confirmEditBtn;
    Button rejectEditBtn;

    Intent intent;
    TodoTask task;

    long due;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //edit task form
        editTaskForm = (LinearLayout) findViewById(R.id.editTaskForm);

        //edit task form content
        createdAtText = (TextView) findViewById(R.id.createdAtText);
        doneSwitch = (Switch) findViewById(R.id.doneSwitch);
        editDescriptionEt = (EditText) findViewById(R.id.editDescriptionEt);
        editCalendar = (CalendarView) findViewById(R.id.editCalendar);
        editRadioGroup = (RadioGroup) findViewById(R.id.editRadioGroup);
        confirmEditBtn = (Button) findViewById(R.id.confirmEditBtn);
        rejectEditBtn = (Button) findViewById(R.id.rejectEditBtn);

        //DB connection
        dBAdapter = new DbAdapter(getApplicationContext());
        dBAdapter.open();

        //get task id from intent
        intent = getIntent();
        task = dBAdapter.getTodo(Long.valueOf(intent.getStringExtra("EXTRA_TASK_ID")));

        //calendar
        due = task.getDue();
        editCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                Date dueDate = new Date(year-1900,month,dayOfMonth);
                due = dueDate.getTime();
            }
        });

        //calendar data formatter
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getCreated_at());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        int mSecond = calendar.get(Calendar.SECOND);
        String month = String.valueOf(mMonth);
        String day = String.valueOf(mDay);
        String hour = String.valueOf(mHour);
        String minute = String.valueOf(mMinute);
        String second = String.valueOf(mSecond);

        if (mMonth<10) {
            month = "0"+month;
        }
        if (mDay<10) {
            day = "0"+day;
        }
        if (mHour<10) {
            hour = "0"+hour;
        }
        if (mMinute<10) {
            minute = "0"+minute;
        }
        if (mSecond<10) {
            second = "0"+second;
        }

        //set values
        createdAtText.setText("Utworzono: "+day+"-"+month+"-"+mYear+" "+hour+":"+minute+":"+second);
        if(task.isCompleted()) {
            doneSwitch.setChecked(true);
        }
        editDescriptionEt.setText(task.getDescription());
        editCalendar.setDate(due);
        switch(task.getPriority()) {
            case 0:
                ((RadioButton) editRadioGroup.getChildAt(0)).setChecked(true);
                break;
            case 1:
                ((RadioButton) editRadioGroup.getChildAt(1)).setChecked(true);
                break;
            case 2:
                ((RadioButton) editRadioGroup.getChildAt(2)).setChecked(true);
                break;
        }

        //confirm edit form
        confirmEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //description
                String descriptionText = editDescriptionEt.getText().toString();
                //created at
                Long currDate = System.currentTimeMillis();
                //due set on calendar listener
                //priority
                int radioButtonID = editRadioGroup.getCheckedRadioButtonId();
                View radioButton = editRadioGroup.findViewById(radioButtonID);
                int selectedPriority = editRadioGroup.indexOfChild(radioButton);
                boolean completed = doneSwitch.isChecked();

                //database insert
                //(long id, String description, boolean completed, Long due, int priority)
                if (descriptionText.length()>0 && descriptionText.length()<=20) {
                    if (dBAdapter.updateTodo(task.getId(), descriptionText, completed,
                            due, selectedPriority)) {
                        Intent returnIntent = new Intent();
                        setResult(EditActivity.RESULT_OK,returnIntent);
                        finish();
                    } else {
                        Toast.makeText(EditActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditActivity.this, "Nazwa zadania musi mieć od 1-20 znaków",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        rejectEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                setResult(EditActivity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

    }
}

package com.example.mw5.todolist;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TodoTasksAdapter extends ArrayAdapter<TodoTask> {
    private Activity context;
    private List<TodoTask> tasks;
    public DbAdapter dBAdapter;

    LinearLayout editTaskForm;
    //edit form
    EditText editDescriptionEt;
    CalendarView editCalendar;
    RadioGroup editRadioGroup;
    Button confirmEditBtn;
    Button rejectEditBtn;

    public TodoTasksAdapter(Activity context, List<TodoTask> tasks, DbAdapter dBAdapter) {
        super(context, R.layout.task_list_row, tasks);
        this.dBAdapter = dBAdapter;
        this.context = context;
        this.tasks = tasks;
    }

    //test
    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public TodoTask getItem(int pos) {
        return tasks.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return tasks.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    static class ViewHolder {
        public TextView toDoText;
        public TextView toDoText2;
        public int id;
    }
    //test end

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater layoutInflater = context.getLayoutInflater();
            rowView = layoutInflater.inflate(R.layout.task_list_row, null, true);
            viewHolder = new ViewHolder();
            viewHolder.toDoText = (TextView) rowView.findViewById(R.id.list_item_string);
            viewHolder.toDoText2 = (TextView) rowView.findViewById(R.id.list_item_string_2);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Button setDoneBtn = (Button) rowView.findViewById(R.id.set_done_btn);
        Button deleteBtn = (Button) rowView.findViewById(R.id.delete_btn);
        //edit task form
        editTaskForm = (LinearLayout) rowView.findViewById(R.id.editTaskForm);

        //edit task form content
        editDescriptionEt = (EditText) rowView.findViewById(R.id.editDescriptionEt);
        editCalendar = (CalendarView) rowView.findViewById(R.id.editCalendar);
        editRadioGroup = (RadioGroup) rowView.findViewById(R.id.editRadioGroup);
        confirmEditBtn = (Button) rowView.findViewById(R.id.confirmEditBtn);
        rejectEditBtn = (Button) rowView.findViewById(R.id.rejectEditBtn);

        TodoTask task = tasks.get(position);
        viewHolder.toDoText.setText(task.getDescription());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getDue());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        String month = String.valueOf(mMonth);
        String day = String.valueOf(mDay);

        if (mMonth<10) {
            month = "0"+month;
        }
        if (mDay<10) {
            day = "0"+day;
        }

        viewHolder.toDoText2.setText("Termin: "+day+"-"+month+"-"+mYear);

        viewHolder.id = (int) task.getId();

        //priority coloring?

        //completed task coloring
        if(task.isCompleted()) {
            rowView.setBackgroundColor(Color.GREEN);
            setDoneBtn.setVisibility(View.GONE);
        } else {
            rowView.setBackgroundColor(Color.TRANSPARENT);
            setDoneBtn.setVisibility(View.VISIBLE);
        }

        //delete btn handler
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (dBAdapter.deleteTodo(viewHolder.id)) {
                    tasks.remove(position);
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                notifyDataSetChanged();
            }
        });

        setDoneBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //to verify on edit
                if (dBAdapter.updateTodo(viewHolder.id, tasks.get(position).getDescription(), true,
                        tasks.get(position).getDue(), tasks.get(position).getPriority())) {
                    tasks.get(position).setCompleted(true);
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                notifyDataSetChanged();
            }
        });

        //confirm edit form
        confirmEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //description

                String descriptionText = editDescriptionEt.getText().toString();
                //created at
                Long currDate = System.currentTimeMillis();
                //due
                Long due = editCalendar.getDate();
                //priority
                int radioButtonID = editRadioGroup.getCheckedRadioButtonId();
                View radioButton = editRadioGroup.findViewById(radioButtonID);
                int selectedPriority = editRadioGroup.indexOfChild(radioButton);

                //database insert
//                if (descriptionText.length()>0 && descriptionText.length()<=20) {
//                    if (dBAdapter.insertTodo(descriptionText, currDate, due, selectedPriority) > 0) {
//                        updateListViewData();
//                        addTaskForm.setVisibility(View.INVISIBLE);
//                        fabAddTask.setVisibility(View.VISIBLE);
//                        addDescriptionEt.setText("");
//                        ((RadioButton) addRadioGroup.getChildAt(1)).setChecked(true);
//                    } else {
//                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "Nazwa zadania musi mieć od 1-20 znaków",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });
        rejectEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskForm.setVisibility(View.GONE);
                Toast.makeText(context, "shit", Toast.LENGTH_SHORT).show();
            }
        });

        //row click handler
//////////zrobic edit osobnym activity bo i tak tam będę dorzucał załączniki
        rowView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(editTaskForm.getVisibility() != View.VISIBLE) {
                    editTaskForm.setVisibility(View.VISIBLE);
                }
            }
        });

        return rowView;
    }


}
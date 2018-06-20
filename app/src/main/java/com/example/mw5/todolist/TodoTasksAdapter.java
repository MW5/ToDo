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
import android.widget.TextView;
import android.widget.Toast;

public class TodoTasksAdapter extends ArrayAdapter<TodoTask> {
    private Activity context;
    private List<TodoTask> tasks;
    public DbAdapter dBAdapter;

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

        TodoTask task = tasks.get(position);
        viewHolder.toDoText.setText(task.getDescription()+task.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getDue());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        String month = String.valueOf(mMonth);
        String day = String.valueOf(mDay);
        String hour = String.valueOf(mHour);
        String minute = String.valueOf(mMinute);

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


        viewHolder.toDoText2.setText("Termin: "+hour+":"+minute+" "+mDay+"-"+mMonth+"-"+mYear);

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

        //done btn handler
        setDoneBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (dBAdapter.updateTodo(viewHolder.id, tasks.get(position).getDescription(), true,
                        tasks.get(position).getDue(), tasks.get(position).getPriority())) {
                    tasks.get(position).setCompleted(true);
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                notifyDataSetChanged();
            }
        });

        //task click

        return rowView;
    }


}
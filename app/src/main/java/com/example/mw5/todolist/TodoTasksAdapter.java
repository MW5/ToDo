package com.example.mw5.todolist;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
    public TodoTask task;
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
        public TextView listItemDescription;
        public TextView listItemDue;
        public TextView listItemPriority;
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
            viewHolder.listItemDescription = (TextView) rowView.findViewById(R.id.listItemDescription);
            viewHolder.listItemDue = (TextView) rowView.findViewById(R.id.listItemDue);
            viewHolder.listItemPriority = (TextView) rowView.findViewById(R.id.listItemPriority);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Button setDoneBtn = (Button) rowView.findViewById(R.id.setDoneBtn);
        Button deleteBtn = (Button) rowView.findViewById(R.id.deleteBtn);

        task = tasks.get(position);
        viewHolder.listItemDescription.setText(task.getDescription());

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


        //priority tranlator
        String priority = "";
        switch (task.getPriority()) {
            case 0: priority = "Priorytet: wysoki";
                break;
            case 1: priority = "Priorytet: średni";
                break;
            case 2: priority = "Priorytet: niski";
                break;
        }
        viewHolder.listItemDue.setText("Termin: "+day+"-"+month+"-"+mYear);
        viewHolder.listItemPriority.setText(priority);

        viewHolder.id = (int) task.getId();

        //colors for priority bg`s
        GradientDrawable highPriorityBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x44FF3300,0xFFFFFFFF});
        highPriorityBg.setCornerRadius(0f);

        GradientDrawable mediumPriorityBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x88FFFF99,0xFFFFFFFF});
        highPriorityBg.setCornerRadius(0f);

        GradientDrawable lowPriorityBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x336699FF,0xFFFFFFFF});
        highPriorityBg.setCornerRadius(0f);

        //completed task coloring
        if(task.isCompleted()) {
            rowView.setBackgroundColor(Color.GREEN);
            setDoneBtn.setVisibility(View.GONE);
        } else {
            switch (task.getPriority()) {
                case 0:
                    rowView.setBackground(highPriorityBg);
                    break;
                case 1:
                    rowView.setBackground(mediumPriorityBg);
                    break;
                case 2:
                    rowView.setBackground(lowPriorityBg);
                    break;
            }
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

        //set done btn handler
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

        //row click handler
        rowView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, EditActivity.class);
                intent.putExtra("EXTRA_TASK_ID", String.valueOf(viewHolder.id));
                context.startActivityForResult(intent, 1);
            }
        });

        return rowView;
    }
}
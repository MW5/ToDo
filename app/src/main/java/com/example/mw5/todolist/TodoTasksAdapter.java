package com.example.mw5.todolist;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
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
        public TextView ToDoText;
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
            viewHolder.ToDoText = (TextView) rowView.findViewById(R.id.list_item_string);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Button setDoneBtn = (Button) rowView.findViewById(R.id.set_done_btn);
        Button deleteBtn = (Button) rowView.findViewById(R.id.delete_btn);

        TodoTask task = tasks.get(position);
        viewHolder.ToDoText.setText(task.getDescription()+task.getId());
        viewHolder.id = (int) task.getId();

        //completed task coloring
        if(task.isCompleted()) {
            rowView.setBackgroundColor(Color.GREEN);
            setDoneBtn.setVisibility(View.GONE);
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
                if (dBAdapter.updateTodo(viewHolder.id, tasks.get(position).getDescription(), true)) {
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
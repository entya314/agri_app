package com.entya.agri_work_app;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHolder> {

    private ArrayList<WorkItem> list;

    public WorkAdapter(ArrayList<WorkItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WorkItem item = list.get(position);

        holder.textDate.setText(item.date);
        holder.textJob.setText(item.job);
        holder.textMemo.setText(item.memo);
        holder.imageView.setImageURI(Uri.parse(item.image_path));
        holder.textStatus.setText(item.status);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDate;
        TextView textJob;
        TextView textMemo;
        TextView textStatus;

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            textDate = itemView.findViewById(R.id.t_date_i);
            textMemo = itemView.findViewById(R.id.t_memo_i);
            textJob = itemView.findViewById(R.id.t_job_i);
            textStatus = itemView.findViewById(R.id.t_status_i);
            imageView = itemView.findViewById(R.id.t_image);
        }
    }
}
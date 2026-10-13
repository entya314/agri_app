package com.entya.agri_work_app.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.entya.agri_work_app.R;
import com.entya.agri_work_app.model.WorkHistory;

import java.util.ArrayList;

public class WorkHistoryAdapter extends RecyclerView.Adapter<WorkHistoryAdapter.ViewHolder> {

    private ArrayList<WorkHistory> list;

    public WorkHistoryAdapter(ArrayList<WorkHistory> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WorkHistory item = list.get(position);

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
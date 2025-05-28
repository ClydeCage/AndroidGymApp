package com.example.hadonggymapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutScheduleAdapter extends RecyclerView.Adapter<WorkoutScheduleAdapter.ViewHolder> {
    private List<WorkoutSchedule> schedules;
    private SimpleDateFormat dateFormat;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WorkoutSchedule schedule);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public WorkoutScheduleAdapter(List<WorkoutSchedule> schedules) {
        this.schedules = schedules;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSchedule schedule = schedules.get(position);
        holder.textViewGymName.setText(schedule.getGymName());
        holder.textViewTrainer.setText("HLV: " + schedule.getTrainerName());
        holder.textViewDateTime.setText(String.format("Ngày: %s - Giờ: %s",
                dateFormat.format(schedule.getDate()),
                schedule.getTime()));
        
        String status = schedule.getStatus();
        holder.textViewStatus.setText(status);
        
        // Đổi màu background tùy theo trạng thái
        int backgroundColor;
        switch (status.toLowerCase()) {
            case "pending":
                backgroundColor = android.graphics.Color.parseColor("#FFA500"); // Orange
                break;
            case "confirmed":
                backgroundColor = android.graphics.Color.parseColor("#4CAF50"); // Green
                break;
            case "cancelled":
                backgroundColor = android.graphics.Color.parseColor("#F44336"); // Red
                break;
            default:
                backgroundColor = android.graphics.Color.parseColor("#2196F3"); // Blue
        }
        holder.textViewStatus.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void updateSchedules(List<WorkoutSchedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGymName;
        TextView textViewTrainer;
        TextView textViewDateTime;
        TextView textViewStatus;

        ViewHolder(View view) {
            super(view);
            textViewGymName = view.findViewById(R.id.textViewGymName);
            textViewTrainer = view.findViewById(R.id.textViewTrainer);
            textViewDateTime = view.findViewById(R.id.textViewDateTime);
            textViewStatus = view.findViewById(R.id.textViewStatus);

            // Add click listener to the item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(schedules.get(position));
                }
            });
        }
    }
} 
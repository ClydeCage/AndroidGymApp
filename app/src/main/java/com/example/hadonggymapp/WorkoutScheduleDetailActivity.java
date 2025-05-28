package com.example.hadonggymapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.firebase.firestore.FirebaseFirestore;

public class WorkoutScheduleDetailActivity extends AppCompatActivity {

    private TextView textViewGymName, textViewTrainer, textViewDateTime, textViewStatus, textViewNotes;
    private Button buttonContactGym;
    private Button buttonCancelSchedule;
    private FirebaseFirestore db;
    private Gym currentGym;
    private WorkoutSchedule currentSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_schedule_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết lịch tập");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textViewGymName = findViewById(R.id.textViewDetailGymName);
        textViewTrainer = findViewById(R.id.textViewDetailTrainer);
        textViewDateTime = findViewById(R.id.textViewDetailDateTime);
        textViewStatus = findViewById(R.id.textViewDetailStatus);
        textViewNotes = findViewById(R.id.textViewDetailNotes);
        buttonContactGym = findViewById(R.id.buttonContactGym);
        buttonCancelSchedule = findViewById(R.id.buttonCancelSchedule);

        db = FirebaseFirestore.getInstance();

        WorkoutSchedule schedule = (WorkoutSchedule) getIntent().getSerializableExtra("workout_schedule");

        if (schedule != null) {
            currentSchedule = schedule;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewGymName.setText("Phòng tập: " + schedule.getGymName());
            textViewTrainer.setText("Huấn luyện viên: " + schedule.getTrainerName());
            textViewDateTime.setText(String.format("Thời gian: %s - %s",
                    dateFormat.format(schedule.getDate()),
                    schedule.getTime()));
            textViewStatus.setText("Trạng thái: " + schedule.getStatus());
            textViewNotes.setText("Ghi chú: " + schedule.getNotes());

            // Load gym details to get phone number
            loadGymDetails(schedule.getGymId());
            setupCancelButton();
        }
    }

    private void loadGymDetails(String gymId) {
        db.collection("gyms").document(gymId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentGym = documentSnapshot.toObject(Gym.class);
                        if (currentGym != null && currentGym.getPhone() != null && !currentGym.getPhone().isEmpty()) {
                            buttonContactGym.setVisibility(View.VISIBLE);
                            buttonContactGym.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + currentGym.getPhone()));
                                startActivity(intent);
                            });
                        } else {
                            buttonContactGym.setVisibility(View.GONE);
                        }
                    } else {
                        buttonContactGym.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkoutScheduleDetail", "Error loading gym details", e);
                    buttonContactGym.setVisibility(View.GONE);
                });
    }

    private void setupCancelButton() {
        if (currentSchedule != null && currentSchedule.getStatus().equals("pending")) {
            buttonCancelSchedule.setVisibility(View.VISIBLE);
            buttonCancelSchedule.setOnClickListener(v -> {
                // Show confirmation dialog before canceling
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận hủy")
                        .setMessage("Bạn có chắc chắn muốn hủy lịch tập này không?")
                        .setPositiveButton("Có", (dialog, which) -> cancelWorkout())
                        .setNegativeButton("Không", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            });
        } else {
            buttonCancelSchedule.setVisibility(View.GONE);
        }
    }

    private void cancelWorkout() {
        if (currentSchedule != null && currentSchedule.getId() != null) {
            db.collection("workout_schedules").document(currentSchedule.getId())
                    .update("status", "cancelled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(WorkoutScheduleDetailActivity.this, "Đã hủy lịch tập.", Toast.LENGTH_SHORT).show();
                        // Update the status text and hide the cancel button
                        textViewStatus.setText("Trạng thái: cancelled");
                        buttonCancelSchedule.setVisibility(View.GONE);
                        // Optionally, finish the activity or update the list in the previous activity
                        // finish(); // uncomment if you want to close the detail screen after canceling
                    })
                    .addOnFailureListener(e -> {
                        Log.e("WorkoutScheduleDetail", "Error canceling workout", e);
                        Toast.makeText(WorkoutScheduleDetailActivity.this, "Lỗi khi hủy lịch tập.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
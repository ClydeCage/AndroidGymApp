package com.example.hadonggymapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class ScheduleWorkoutActivity extends AppCompatActivity {
    private TextInputEditText editTextGymName, editTextTrainer, editTextDate, editTextTime, editTextNotes;
    private MaterialButton buttonSchedule;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar selectedDate;
    private String selectedTime;
    private Gym selectedGym;
    private String selectedTrainerId;
    private List<Trainer> trainerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_workout);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Get gym data from intent
        if (getIntent().hasExtra("gym")) {
            selectedGym = (Gym) getIntent().getSerializableExtra("gym");
            if (selectedGym != null) {
                editTextGymName.setText(selectedGym.getName());
                loadTrainersForGym(selectedGym.getId());
            }
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        editTextGymName = findViewById(R.id.editTextGymName);
        editTextTrainer = findViewById(R.id.editTextTrainer);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonSchedule = findViewById(R.id.buttonSchedule);
    }

    private void setupClickListeners() {
        editTextDate.setOnClickListener(v -> showDatePicker());
        editTextTime.setOnClickListener(v -> showTimePicker());
        editTextTrainer.setOnClickListener(v -> showTrainerSelection());
        buttonSchedule.setOnClickListener(v -> scheduleWorkout());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editTextDate.setText(dateFormat.format(selectedDate.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editTextTime.setText(selectedTime);
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    private void loadTrainersForGym(String gymId) {
        android.util.Log.d("DEBUG", "Loading trainers for gymId: " + gymId);
        db.collection("trainers")
            .whereEqualTo("gymId", gymId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                trainerList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    Trainer trainer = doc.toObject(Trainer.class);
                    android.util.Log.d("DEBUG", "Trainer loaded: " + trainer.getName() + " - " + trainer.getGymId());
                    if (trainer.getId() == null || trainer.getId().isEmpty()) {
                        trainer.setId(doc.getId());
                    }
                    trainerList.add(trainer);
                }
                android.util.Log.d("DEBUG", "Total trainers loaded: " + trainerList.size());
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("DEBUG", "Error loading trainers: " + e.getMessage());
                Toast.makeText(this, "Không tải được danh sách huấn luyện viên", Toast.LENGTH_SHORT).show();
            });
    }

    private void showTrainerSelection() {
        if (trainerList.isEmpty()) {
            Toast.makeText(this, "Không có huấn luyện viên nào cho phòng tập này!", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] trainerNames = new String[trainerList.size()];
        for (int i = 0; i < trainerList.size(); i++) {
            trainerNames[i] = trainerList.get(i).getName();
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn huấn luyện viên")
            .setItems(trainerNames, (dialog, which) -> {
                Trainer selected = trainerList.get(which);
                editTextTrainer.setText(selected.getName());
                selectedTrainerId = selected.getId();
            })
            .show();
    }

    private void scheduleWorkout() {
        if (!validateInputs()) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        WorkoutSchedule schedule = new WorkoutSchedule(
            UUID.randomUUID().toString(),
            userId,
            selectedGym.getId(),
            selectedTrainerId,
            selectedDate.getTime(),
            selectedTime,
            "pending"
        );
        schedule.setNotes(editTextNotes.getText().toString());

        // Set gym and trainer names before saving
        if (selectedGym != null) {
            schedule.setGymName(selectedGym.getName());
        }
        // Find the selected trainer by ID to get their name
        Trainer selectedTrainer = null;
        for (Trainer trainer : trainerList) {
            if (trainer.getId().equals(selectedTrainerId)) {
                selectedTrainer = trainer;
                break;
            }
        }
        if (selectedTrainer != null) {
            schedule.setTrainerName(selectedTrainer.getName());
        }

        db.collection("workout_schedules")
            .document(schedule.getId())
            .set(schedule)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi đặt lịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private boolean validateInputs() {
        if (selectedGym == null) {
            Toast.makeText(this, "Vui lòng chọn phòng tập", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTrainerId == null) {
            Toast.makeText(this, "Vui lòng chọn huấn luyện viên", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày tập", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTime == null) {
            Toast.makeText(this, "Vui lòng chọn giờ tập", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
} 
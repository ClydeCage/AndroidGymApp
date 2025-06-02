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
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
    private static final List<Trainer> SAMPLE_TRAINERS = new ArrayList<>();

    // Xóa hoặc comment khối static này nếu không dùng SAMPLE_TRAINERS nữa
    /*
    static {
        SAMPLE_TRAINERS.add(new Trainer("Nguyễn Văn A"));
        SAMPLE_TRAINERS.get(0).setSpecialization("PT giảm cân, tăng cơ");
        SAMPLE_TRAINERS.get(0).setDescription("Chuyên gia thể hình với 5 năm kinh nghiệm.");
        SAMPLE_TRAINERS.get(0).setPhone("0912345678");
        SAMPLE_TRAINERS.get(0).setImageUrl("https://randomuser.me/api/portraits/men/1.jpg");
        SAMPLE_TRAINERS.add(new Trainer("Trần Thị B"));
        SAMPLE_TRAINERS.get(1).setSpecialization("Yoga, Pilates");
        SAMPLE_TRAINERS.get(1).setDescription("HLV Yoga quốc tế, tận tâm với học viên.");
        SAMPLE_TRAINERS.get(1).setPhone("0987654321");
        SAMPLE_TRAINERS.get(1).setImageUrl("https://randomuser.me/api/portraits/women/2.jpg");
    }
    */

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
                loadTrainersForGym(selectedGym);
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

    private void loadTrainersForGym(Gym gym) {
        trainerList.clear();
        if (gym != null && gym.getServices() != null) {
            android.util.Log.d("DEBUG", "Gym services: " + gym.getServices().toString());
            for (String service : gym.getServices()) {
                android.util.Log.d("DEBUG", "Service: [" + service + "]");
                if (service != null && service.trim().equalsIgnoreCase("Huấn luyện viên cá nhân")) {
                    // Nếu có dịch vụ PT, lấy danh sách trainer từ gym
                    if (gym.getTrainers() != null) {
                         trainerList.addAll(gym.getTrainers());
                    } else {
                        android.util.Log.d("DEBUG", "Gym has PT service but no trainers listed.");
                    }
                    break;
                }
            }
        } else {
             android.util.Log.d("DEBUG", "Gym or services list is null.");
        }
    }

    private void showTrainerSelection() {
        if (trainerList.isEmpty()) {
            // Nếu không có huấn luyện viên, cho phép đặt lịch không có HLV
            editTextTrainer.setText("Không có huấn luyện viên");
            selectedTrainerId = null;
            return;
        }
        String[] trainerNames = new String[trainerList.size() + 1];
        trainerNames[0] = "Không có huấn luyện viên";
        for (int i = 0; i < trainerList.size(); i++) {
            trainerNames[i + 1] = trainerList.get(i).getName();
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn huấn luyện viên")
            .setItems(trainerNames, (dialog, which) -> {
                if (which == 0) {
                    editTextTrainer.setText("Không có huấn luyện viên");
                    selectedTrainerId = null;
                } else {
                    Trainer selected = trainerList.get(which - 1);
                    editTextTrainer.setText(selected.getName());
                    selectedTrainerId = selected.getName();
                }
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
            if (trainer.getName().equals(selectedTrainerId)) {
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
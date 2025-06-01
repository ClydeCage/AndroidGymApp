package com.example.hadonggymapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTrainerActivity extends AppCompatActivity {

    private static final String TAG = "EditTrainerActivity";
    private static final String DEFAULT_TRAINER_IMAGE = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y";

    private ImageView imageViewTrainer;
    private TextInputEditText editTextTrainerName;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextSpecialization;
    private TextInputEditText editTextDescription;
    private Button buttonSaveTrainer;

    private FirebaseFirestore db;
    private String trainerId = null; // null nếu thêm mới, có giá trị nếu sửa
    private String selectedImageUrl = DEFAULT_TRAINER_IMAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trainer);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm huấn luyện viên"); // Tiêu đề mặc định
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ views
        imageViewTrainer = findViewById(R.id.imageViewTrainer);
        editTextTrainerName = findViewById(R.id.editTextTrainerName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextSpecialization = findViewById(R.id.editTextSpecialization);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSaveTrainer = findViewById(R.id.buttonSaveTrainer);

        // Load ảnh mặc định
        Glide.with(this)
            .load(DEFAULT_TRAINER_IMAGE)
            .centerCrop()
            .into(imageViewTrainer);

        // Kiểm tra nếu đang ở chế độ sửa
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("trainer_id")) {
            trainerId = extras.getString("trainer_id");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Sửa huấn luyện viên"); // Đổi tiêu đề nếu sửa
            }
            loadTrainerData(trainerId);
        }

        // Thiết lập sự kiện click cho nút Save
        buttonSaveTrainer.setOnClickListener(v -> saveTrainer());
    }

    private void loadTrainerData(String id) {
        db.collection("trainers").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Trainer trainer = documentSnapshot.toObject(Trainer.class);
                        if (trainer != null) {
                            editTextTrainerName.setText(trainer.getName());
                            editTextPhone.setText(trainer.getPhone());
                            editTextSpecialization.setText(trainer.getSpecialization());
                            editTextDescription.setText(trainer.getDescription());

                            // Load ảnh nếu có
                            if (trainer.getImageUrl() != null && !trainer.getImageUrl().isEmpty()) {
                                selectedImageUrl = trainer.getImageUrl();
                                Glide.with(this)
                                    .load(trainer.getImageUrl())
                                    .centerCrop()
                                    .into(imageViewTrainer);
                            }
                        }
                    } else {
                        Toast.makeText(EditTrainerActivity.this, "Không tìm thấy huấn luyện viên để sửa", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity nếu không tìm thấy
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditTrainerActivity.this, "Lỗi khi tải dữ liệu huấn luyện viên", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng activity nếu có lỗi
                });
    }

    private void saveTrainer() {
        String name = editTextTrainerName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String specialization = editTextSpecialization.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextTrainerName.setError("Tên huấn luyện viên không được để trống");
            return;
        }

        // Tạo Map dữ liệu để lưu vào Firestore
        Map<String, Object> trainerData = new HashMap<>();
        trainerData.put("name", name);
        trainerData.put("phone", phone);
        trainerData.put("specialization", specialization);
        trainerData.put("description", description);
        trainerData.put("imageUrl", selectedImageUrl);
        trainerData.put("rating", 0.0f);
        trainerData.put("totalRatings", 0);

        if (trainerId == null) {
            // Thêm mới huấn luyện viên
            db.collection("trainers")
                    .add(trainerData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EditTrainerActivity.this, "Đã thêm huấn luyện viên thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditTrainerActivity.this, "Lỗi khi thêm huấn luyện viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Cập nhật huấn luyện viên hiện có
            db.collection("trainers").document(trainerId)
                    .update(trainerData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditTrainerActivity.this, "Đã cập nhật huấn luyện viên thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditTrainerActivity.this, "Lỗi khi cập nhật huấn luyện viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
package com.example.hadonggymapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTrainerActivity extends AppCompatActivity {

    private static final String TAG = "EditTrainerActivity";

    private TextInputEditText editTextTrainerName;
    private Button buttonSaveTrainer;

    private FirebaseFirestore db;
    private String trainerId = null; // null nếu thêm mới, có giá trị nếu sửa

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
        editTextTrainerName = findViewById(R.id.editTextTrainerName);
        buttonSaveTrainer = findViewById(R.id.buttonSaveTrainer);

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

        if (TextUtils.isEmpty(name)) {
            editTextTrainerName.setError("Tên huấn luyện viên không được để trống");
            return;
        }

        // Tạo Map dữ liệu để lưu vào Firestore
        Map<String, Object> trainerData = new HashMap<>();
        trainerData.put("name", name);
        // Có thể thêm các trường dữ liệu khác nếu có

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
                    .set(trainerData) // set() sẽ ghi đè toàn bộ document
                    // Hoặc dùng update(trainerData) nếu chỉ muốn cập nhật các trường cụ thể
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
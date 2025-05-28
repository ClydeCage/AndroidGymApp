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

public class EditAmenityActivity extends AppCompatActivity {

    private static final String TAG = "EditAmenityActivity";

    private TextInputEditText editTextAmenityName;
    private Button buttonSaveAmenity;

    private FirebaseFirestore db;
    private String amenityId = null; // null nếu thêm mới, có giá trị nếu sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_amenity);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm tiện ích"); // Tiêu đề mặc định
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ views
        editTextAmenityName = findViewById(R.id.editTextAmenityName);
        buttonSaveAmenity = findViewById(R.id.buttonSaveAmenity);

        // Kiểm tra nếu đang ở chế độ sửa
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("amenity_id")) {
            amenityId = extras.getString("amenity_id");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Sửa tiện ích"); // Đổi tiêu đề nếu sửa
            }
            loadAmenityData(amenityId);
        }

        // Thiết lập sự kiện click cho nút Save
        buttonSaveAmenity.setOnClickListener(v -> saveAmenity());
    }

    private void loadAmenityData(String id) {
        db.collection("amenities").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Amenity amenity = documentSnapshot.toObject(Amenity.class);
                        if (amenity != null) {
                            editTextAmenityName.setText(amenity.getName());
                        }
                    } else {
                        Toast.makeText(EditAmenityActivity.this, "Không tìm thấy tiện ích để sửa", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity nếu không tìm thấy
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditAmenityActivity.this, "Lỗi khi tải dữ liệu tiện ích", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng activity nếu có lỗi
                });
    }

    private void saveAmenity() {
        String name = editTextAmenityName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextAmenityName.setError("Tên tiện ích không được để trống");
            return;
        }

        // Tạo Map dữ liệu để lưu vào Firestore
        Map<String, Object> amenityData = new HashMap<>();
        amenityData.put("name", name);
        // Có thể thêm các trường dữ liệu khác nếu có

        if (amenityId == null) {
            // Thêm mới tiện ích
            db.collection("amenities")
                    .add(amenityData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EditAmenityActivity.this, "Đã thêm tiện ích thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditAmenityActivity.this, "Lỗi khi thêm tiện ích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Cập nhật tiện ích hiện có
            db.collection("amenities").document(amenityId)
                    .set(amenityData) // set() sẽ ghi đè toàn bộ document
                    // Hoặc dùng update(amenityData) nếu chỉ muốn cập nhật các trường cụ thể
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditAmenityActivity.this, "Đã cập nhật tiện ích thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditAmenityActivity.this, "Lỗi khi cập nhật tiện ích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
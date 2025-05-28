package com.example.hadonggymapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditServiceActivity extends AppCompatActivity {

    private TextInputEditText editTextServiceName;
    private Button buttonSaveService;
    private FirebaseFirestore db;
    private String originalServiceName = null; // To store the name if editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm Dịch vụ"); // Default title
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ views
        editTextServiceName = findViewById(R.id.editTextServiceName);
        buttonSaveService = findViewById(R.id.buttonSaveService);

        // Kiểm tra nếu đang chỉnh sửa dịch vụ
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("service_name")) {
            originalServiceName = extras.getString("service_name");
            editTextServiceName.setText(originalServiceName);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chỉnh sửa Dịch vụ"); // Update title
            }
        }

        // Thiết lập sự kiện click cho nút Lưu
        buttonSaveService.setOnClickListener(v -> saveService());
    }

    private void saveService() {
        String serviceName = editTextServiceName.getText().toString().trim();

        if (serviceName.isEmpty()) {
            editTextServiceName.setError("Tên dịch vụ không được để trống");
            return;
        }

        // Create a new service map
        Map<String, Object> service = new HashMap<>();
        service.put("name", serviceName);

        if (originalServiceName != null) {
            // Editing existing service
            // Need to find the document ID by the original name first
            db.collection("services")
                    .whereEqualTo("name", originalServiceName)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentIdToUpdate = queryDocumentSnapshots.getDocuments().get(0).getId();
                            db.collection("services").document(documentIdToUpdate)
                                    .update(service) // Use update for specific fields or set(service) for overwriting
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Đã cập nhật dịch vụ", Toast.LENGTH_SHORT).show();
                                        finish(); // Go back to AdminServicesActivity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi khi cập nhật dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Không tìm thấy dịch vụ để cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tìm dịch vụ để cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Adding new service
            db.collection("services")
                    .add(service)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã thêm dịch vụ mới", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to AdminServicesActivity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thêm dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
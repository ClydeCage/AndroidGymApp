package com.example.hadonggymapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class AddGymActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutGymName, textInputLayoutGymAddress, textInputLayoutGymPhone, textInputLayoutGymDescription;
    private TextInputEditText editTextGymName, editTextGymAddress, editTextGymPhone, editTextGymDescription;
    private Button buttonSaveGym;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gym);

        // Check if the user is an admin
        boolean isAdmin = isUserAdmin();
        Log.d("AddGymActivity", "isUserAdmin: " + isAdmin);
        if (!isAdmin) {
            Toast.makeText(this, "Bạn không có quyền truy cập màn hình này.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm phòng gym mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSaveButton();
    }

    private void initializeViews() {
        textInputLayoutGymName = findViewById(R.id.textInputLayoutGymName);
        textInputLayoutGymAddress = findViewById(R.id.textInputLayoutGymAddress);
        textInputLayoutGymPhone = findViewById(R.id.textInputLayoutGymPhone);
        textInputLayoutGymDescription = findViewById(R.id.textInputLayoutGymDescription);

        editTextGymName = findViewById(R.id.editTextGymName);
        editTextGymAddress = findViewById(R.id.editTextGymAddress);
        editTextGymPhone = findViewById(R.id.editTextGymPhone);
        editTextGymDescription = findViewById(R.id.editTextGymDescription);

        buttonSaveGym = findViewById(R.id.buttonSaveGym);
    }

    private void setupSaveButton() {
        buttonSaveGym.setOnClickListener(v -> {
            saveGym();
        });
    }

    private void saveGym() {
        String name = editTextGymName.getText().toString().trim();
        String address = editTextGymAddress.getText().toString().trim();
        String phone = editTextGymPhone.getText().toString().trim();
        String description = editTextGymDescription.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Map để lưu vào Firestore
        Map<String, Object> gym = new HashMap<>();
        gym.put("name", name);
        gym.put("address", address);
        gym.put("phone", phone);
        gym.put("description", description);
        // Thêm các trường khác nếu có (imageUrl, latitude, longitude, services, amenities)

        // Sử dụng UUID để tạo ID document ngẫu nhiên
        String gymId = UUID.randomUUID().toString();

        db.collection("gyms").document(gymId).set(gym)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddGymActivity.this, "Thêm phòng gym thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình sau khi lưu thành công
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddGymActivity.this, "Lỗi khi thêm phòng gym: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isUserAdmin() {
        final boolean[] isAdmin = {false};
        final CountDownLatch latch = new CountDownLatch(1);

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Query Firestore to check if the user is an admin
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean adminStatus = documentSnapshot.getBoolean("isAdmin");
                        isAdmin[0] = adminStatus != null && adminStatus;
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(AddGymActivity.this, "Lỗi khi kiểm tra quyền admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    latch.countDown();
                });

        try {
            latch.await(); // Wait for the query to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isAdmin[0];
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
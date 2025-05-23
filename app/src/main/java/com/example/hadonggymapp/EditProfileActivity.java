package com.example.hadonggymapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutPhone;
    private CircleImageView imageViewProfile;
    private Button buttonSave;
    private Button buttonChangePassword;
    private Uri selectedImageUri;
    private String currentPhotoUrl;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imageViewProfile.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Ánh xạ view
        textInputLayoutName = findViewById(R.id.textInputLayoutName);
        textInputLayoutPhone = findViewById(R.id.textInputLayoutPhone);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa thông tin");
        }

        // Load thông tin người dùng
        loadUserInfo();

        // Xử lý sự kiện click
        imageViewProfile.setOnClickListener(v -> checkPermissionAndPickImage());
        buttonSave.setOnClickListener(v -> saveProfile());
        buttonChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Cần quyền truy cập để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserInfo() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");
                            String photoUrl = documentSnapshot.getString("photoUrl");

                            textInputLayoutName.getEditText().setText(name);
                            textInputLayoutPhone.getEditText().setText(phone);
                            currentPhotoUrl = photoUrl;

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_person)
                                        .error(R.drawable.ic_person)
                                        .into(imageViewProfile);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user info", e);
                        Toast.makeText(EditProfileActivity.this,
                                "Lỗi khi tải thông tin người dùng",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveProfile() {
        String name = textInputLayoutName.getEditText().getText().toString().trim();
        String phone = textInputLayoutPhone.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            textInputLayoutName.setError("Vui lòng nhập họ tên");
            return;
        }

        if (phone.isEmpty()) {
            textInputLayoutPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }

        buttonSave.setEnabled(false);
        buttonSave.setText("Đang lưu...");

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(name, phone);
        } else {
            saveProfileToFirestore(name, phone, currentPhotoUrl);
        }
    }

    private void uploadImageAndSaveProfile(String name, String phone) {
        String imageFileName = "profile_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageFileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String newPhotoUrl = uri.toString();
                                saveProfileToFirestore(name, phone, newPhotoUrl);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL", e);
                                Toast.makeText(EditProfileActivity.this,
                                        "Lỗi khi tải ảnh lên",
                                        Toast.LENGTH_SHORT).show();
                                resetSaveButton();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading image", e);
                    Toast.makeText(EditProfileActivity.this,
                            "Lỗi khi tải ảnh lên",
                            Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
    }

    private void saveProfileToFirestore(String name, String phone, String photoUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        if (photoUrl != null) {
            userData.put("photoUrl", photoUrl);
        }

        db.collection("users").document(currentUser.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this,
                            "Cập nhật thông tin thành công",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile", e);
                    Toast.makeText(EditProfileActivity.this,
                            "Lỗi khi cập nhật thông tin: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    resetSaveButton();
                });
    }

    private void resetSaveButton() {
        buttonSave.setEnabled(true);
        buttonSave.setText("Lưu thông tin");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
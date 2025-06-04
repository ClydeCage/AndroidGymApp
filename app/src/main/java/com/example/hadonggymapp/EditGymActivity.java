package com.example.hadonggymapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditGymActivity extends AppCompatActivity {
    private static final String TAG = "EditGymActivity";
    public static final String EXTRA_GYM_ID = "gym_id";

    private TextInputEditText editTextGymName, editTextGymAddress, editTextGymPhone,
            editTextGymHours, editTextGymDescription, editTextGymImageUrl,
            editTextGymLatitude, editTextGymLongitude, editTextGymServices, editTextGymAmenities;
    private com.google.android.material.chip.ChipGroup chipGroupGymServices, chipGroupGymAmenities;
    private Button buttonSaveGym;
    private FirebaseFirestore db;
    private String gymId = null;
    private boolean isEditMode = false;

    private List<String> selectedServices = new ArrayList<>();
    private List<String> selectedAmenities = new ArrayList<>();

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> servicesSelectionLauncher;
    private ActivityResultLauncher<Intent> amenitiesSelectionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gym);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (isEditMode) {
                getSupportActionBar().setTitle("Chỉnh sửa phòng gym");
            } else {
                getSupportActionBar().setTitle("Thêm phòng gym mới");
            }
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Lấy gymId từ Intent nếu có (chế độ sửa)
        gymId = getIntent().getStringExtra(EXTRA_GYM_ID);
        isEditMode = gymId != null && !gymId.isEmpty();

        // Ánh xạ views
        initializeViews();

        // Đăng ký Activity Result Launchers
        registerActivityResults();

        // Thiết lập sự kiện click cho EditTexts
        setupClickListeners();

        // Nếu là chế độ sửa, load dữ liệu phòng gym
        if (isEditMode) {
            loadGymData(gymId);
        }

        // Thiết lập sự kiện click cho nút lưu
        buttonSaveGym.setOnClickListener(v -> saveGym());

        // Hiển thị dữ liệu đã lưu nếu quay lại từ màn hình chọn
        if (savedInstanceState != null) {
             if (savedInstanceState.containsKey("selectedServices")) {
                selectedServices = savedInstanceState.getStringArrayList("selectedServices");
                updateChipGroup(chipGroupGymServices, selectedServices);
            }
             if (savedInstanceState.containsKey("selectedAmenities")) {
                selectedAmenities = savedInstanceState.getStringArrayList("selectedAmenities");
                updateChipGroup(chipGroupGymAmenities, selectedAmenities);
            }
        }
    }

    private void initializeViews() {
        editTextGymName = findViewById(R.id.editTextGymName);
        editTextGymAddress = findViewById(R.id.editTextGymAddress);
        editTextGymPhone = findViewById(R.id.editTextGymPhone);
        editTextGymHours = findViewById(R.id.editTextGymHours);
        editTextGymDescription = findViewById(R.id.editTextGymDescription);
        editTextGymImageUrl = findViewById(R.id.editTextGymImageUrl);
        editTextGymLatitude = findViewById(R.id.editTextGymLatitude);
        editTextGymLongitude = findViewById(R.id.editTextGymLongitude);
        editTextGymServices = findViewById(R.id.editTextGymServices);
        editTextGymAmenities = findViewById(R.id.editTextGymAmenities);
        chipGroupGymServices = findViewById(R.id.chipGroupGymServices);
        chipGroupGymAmenities = findViewById(R.id.chipGroupGymAmenities);
        buttonSaveGym = findViewById(R.id.buttonSaveGym);
    }

    private void registerActivityResults() {
        servicesSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedServices = result.getData().getStringArrayListExtra(SelectItemsActivity.RESULT_SELECTED_ITEMS);
                        updateChipGroup(chipGroupGymServices, selectedServices);
                    }
                });

        amenitiesSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedAmenities = result.getData().getStringArrayListExtra(SelectItemsActivity.RESULT_SELECTED_ITEMS);
                        updateChipGroup(chipGroupGymAmenities, selectedAmenities);
                    }
                });
    }

    private void setupClickListeners() {
        // Click vào EditText để mở màn hình chọn
        editTextGymServices.setOnClickListener(v -> openSelectItemsActivity("services", selectedServices, servicesSelectionLauncher));
        editTextGymAmenities.setOnClickListener(v -> openSelectItemsActivity("amenities", selectedAmenities, amenitiesSelectionLauncher));

        // Click vào TextInputLayout cũng mở màn hình chọn
         findViewById(R.id.textInputLayoutGymServices).setOnClickListener(v -> openSelectItemsActivity("services", selectedServices, servicesSelectionLauncher));
         findViewById(R.id.textInputLayoutGymAmenities).setOnClickListener(v -> openSelectItemsActivity("amenities", selectedAmenities, amenitiesSelectionLauncher));
    }

    private void openSelectItemsActivity(String itemType, List<String> currentSelectedItems, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(this, SelectItemsActivity.class);
        intent.putExtra(SelectItemsActivity.EXTRA_ITEM_TYPE, itemType);
        intent.putStringArrayListExtra(SelectItemsActivity.EXTRA_SELECTED_ITEMS, new ArrayList<>(currentSelectedItems));
        launcher.launch(intent);
    }

    private void loadGymData(String id) {
        db.collection("gyms").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Gym gym = documentSnapshot.toObject(Gym.class);
                        if (gym != null) {
                            editTextGymName.setText(gym.getName());
                            editTextGymAddress.setText(gym.getAddress());
                            editTextGymPhone.setText(gym.getPhone());
                            editTextGymHours.setText(gym.getHours());
                            editTextGymDescription.setText(gym.getDescription());
                            editTextGymImageUrl.setText(gym.getImageUrl());
                            if (gym.getLatitude() != null) {
                                editTextGymLatitude.setText(String.valueOf(gym.getLatitude()));
                            }
                            if (gym.getLongitude() != null) {
                                editTextGymLongitude.setText(String.valueOf(gym.getLongitude()));
                            }
                            // Load và hiển thị dịch vụ và tiện ích đã lưu dưới dạng chips
                            if (gym.getServices() != null) {
                                selectedServices = new ArrayList<>(gym.getServices());
                                updateChipGroup(chipGroupGymServices, selectedServices);
                            }
                            if (gym.getAmenities() != null) {
                                selectedAmenities = new ArrayList<>(gym.getAmenities());
                                updateChipGroup(chipGroupGymAmenities, selectedAmenities);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin phòng tập", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải dữ liệu phòng tập", e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu phòng tập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveGym() {
        // Kiểm tra quyền admin trước khi thực hiện
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra role admin
        db.collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                Boolean isAdminBool = documentSnapshot.getBoolean("isAdmin");
                boolean isAdmin = "admin".equals(role) || (isAdminBool != null && isAdminBool);
                
                if (!isAdmin) {
                    Toast.makeText(EditGymActivity.this, "Bạn không có quyền thực hiện thao tác này", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Nếu là admin, tiếp tục lưu dữ liệu
                saveGymData();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking admin role", e);
                Toast.makeText(EditGymActivity.this, "Lỗi khi kiểm tra quyền", Toast.LENGTH_SHORT).show();
            });
    }

    private void saveGymData() {
        // Validate input
        String name = editTextGymName.getText().toString().trim();
        String address = editTextGymAddress.getText().toString().trim();
        String phone = editTextGymPhone.getText().toString().trim();
        String hours = editTextGymHours.getText().toString().trim();
        String description = editTextGymDescription.getText().toString().trim();
        String imageUrl = editTextGymImageUrl.getText().toString().trim();
        String latitudeStr = editTextGymLatitude.getText().toString().trim();
        String longitudeStr = editTextGymLongitude.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextGymName.setError("Vui lòng nhập tên phòng gym");
            return;
        }

        // Create gym data map
        Map<String, Object> gymData = new HashMap<>();
        gymData.put("name", name);
        gymData.put("address", address);
        gymData.put("phone", phone);
        gymData.put("hours", hours);
        gymData.put("description", description);
        gymData.put("imageUrl", imageUrl);
        gymData.put("services", selectedServices);
        gymData.put("amenities", selectedAmenities);

        // Add coordinates if provided
        if (!TextUtils.isEmpty(latitudeStr)) {
            try {
                double latitude = Double.parseDouble(latitudeStr);
                gymData.put("latitude", latitude);
            } catch (NumberFormatException e) {
                editTextGymLatitude.setError("Vĩ độ không hợp lệ");
                return;
            }
        }

        if (!TextUtils.isEmpty(longitudeStr)) {
            try {
                double longitude = Double.parseDouble(longitudeStr);
                gymData.put("longitude", longitude);
            } catch (NumberFormatException e) {
                editTextGymLongitude.setError("Kinh độ không hợp lệ");
                return;
            }
        }

        // Lưu vào Firestore
        if (isEditMode) {
            // Cập nhật phòng gym hiện có
            db.collection("gyms").document(gymId)
                    .update(gymData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditGymActivity.this, "Cập nhật phòng gym thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating gym", e);
                        Toast.makeText(EditGymActivity.this, "Lỗi khi cập nhật phòng gym: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Thêm phòng gym mới
            db.collection("gyms")
                    .add(gymData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EditGymActivity.this, "Thêm phòng gym thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding gym", e);
                        Toast.makeText(EditGymActivity.this, "Lỗi khi thêm phòng gym: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Hàm helper để cập nhật ChipGroup hiển thị các mục đã chọn
    private void updateChipGroup(ChipGroup chipGroup, List<String> items) {
        chipGroup.removeAllViews(); // Xóa các chip cũ
        if (items != null) {
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setCloseIconVisible(true); // Cho phép xóa chip
                chip.setOnCloseIconClickListener(v -> {
                    // Xử lý khi click vào icon xóa chip
                    if (chipGroup.getId() == R.id.chipGroupGymServices) {
                        selectedServices.remove(item);
                         updateChipGroup(chipGroupGymServices, selectedServices);
                    } else if (chipGroup.getId() == R.id.chipGroupGymAmenities) {
                        selectedAmenities.remove(item);
                         updateChipGroup(chipGroupGymAmenities, selectedAmenities);
                    }
                });
                chipGroup.addView(chip);
            }
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

    // Lưu trạng thái các danh sách đã chọn khi xoay màn hình
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("selectedServices", new ArrayList<>(selectedServices));
        outState.putStringArrayList("selectedAmenities", new ArrayList<>(selectedAmenities));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("selectedServices")) {
            selectedServices = savedInstanceState.getStringArrayList("selectedServices");
        }
         if (savedInstanceState.containsKey("selectedAmenities")) {
            selectedAmenities = savedInstanceState.getStringArrayList("selectedAmenities");
        }
        // Cập nhật ChipGroup sau khi khôi phục trạng thái
        updateChipGroup(chipGroupGymServices, selectedServices);
        updateChipGroup(chipGroupGymAmenities, selectedAmenities);
    }
} 
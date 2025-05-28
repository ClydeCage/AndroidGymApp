package com.example.hadonggymapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAmenitiesActivity extends AppCompatActivity implements AmenityAdapter.OnDeleteClickListener {
    private static final String TAG = "AdminAmenitiesActivity";

    private RecyclerView recyclerViewAmenitiesAdmin;
    private AmenityAdapter amenityAdapter;
    private List<Amenity> amenityList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FloatingActionButton fabAddAmenity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_amenities);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Tiện ích");
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra quyền admin (tùy chọn, có thể bỏ qua ở màn admin nếu đã kiểm tra trước đó)
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ views
        recyclerViewAmenitiesAdmin = findViewById(R.id.recyclerViewAmenitiesAdmin);
        fabAddAmenity = findViewById(R.id.fabAddAmenity);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click cho FAB
        fabAddAmenity.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAmenitiesActivity.this, EditAmenityActivity.class);
            startActivity(intent);
        });

        // Tải dữ liệu tiện ích
        loadAmenities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi Activity trở lại foreground
        loadAmenities();
    }

    private void setupRecyclerView() {
        recyclerViewAmenitiesAdmin.setHasFixedSize(true);
        recyclerViewAmenitiesAdmin.setLayoutManager(new LinearLayoutManager(this));

        amenityAdapter = new AmenityAdapter(this, amenityList);
        recyclerViewAmenitiesAdmin.setAdapter(amenityAdapter);

        // Thêm sự kiện click cho item (để sửa)
        amenityAdapter.setOnItemClickListener(amenity -> {
            Intent intent = new Intent(AdminAmenitiesActivity.this, EditAmenityActivity.class);
            intent.putExtra("amenity_id", amenity.getId());
            startActivity(intent);
        });

        // Thiết lập listener cho nút xóa trên item
        amenityAdapter.setOnDeleteClickListener(this); // Gán activity làm listener
    }

    private void loadAmenities() {
        db.collection("amenities")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    amenityList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Amenity amenity = document.toObject(Amenity.class);
                        if (amenity != null) {
                             amenity.setId(document.getId()); // Gán ID tài liệu Firestore vào đối tượng
                            amenityList.add(amenity);
                        }
                    }
                    amenityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminAmenitiesActivity.this, "Lỗi khi tải danh sách tiện ích", Toast.LENGTH_SHORT).show();
                });
    }

    // Triển khai phương thức onDeleteClick từ interface AmenityAdapter.OnDeleteClickListener
    @Override
    public void onDeleteClick(Amenity amenity) {
        showDeleteDialog(amenity);
    }

    private void showDeleteDialog(Amenity amenity) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa tiện ích")
                .setMessage("Bạn có chắc chắn muốn xóa tiện ích \"" + amenity.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAmenity(amenity)) // Gọi hàm xóa khi nhấn OK
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAmenity(Amenity amenity) {
        db.collection("amenities").document(amenity.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa tiện ích: " + amenity.getName(), Toast.LENGTH_SHORT).show();
                    loadAmenities(); // Tải lại danh sách sau khi xóa
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa tiện ích: " + amenity.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
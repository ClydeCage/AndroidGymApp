package com.example.hadonggymapp;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminGymActivity extends AppCompatActivity implements GymAdapter.OnDeleteClickListener {
    private static final String TAG = "AdminGymActivity";

    private RecyclerView recyclerViewGymsAdmin;
    private GymAdapter gymAdapter;
    private List<Gym> gymList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FloatingActionButton fabAddGym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_gym);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Phòng tập");
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra quyền admin
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ views
        recyclerViewGymsAdmin = findViewById(R.id.recyclerViewGymsAdmin);
        fabAddGym = findViewById(R.id.fabAddGym);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click cho FAB
        fabAddGym.setOnClickListener(v -> {
            Intent intent = new Intent(AdminGymActivity.this, AdminOptionsActivity.class);
            startActivity(intent);
        });

        // Tải dữ liệu phòng tập
        loadGyms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi Activity trở lại foreground
        loadGyms();
    }

    private void setupRecyclerView() {
        recyclerViewGymsAdmin.setHasFixedSize(true);
        recyclerViewGymsAdmin.setLayoutManager(new LinearLayoutManager(this));

        gymAdapter = new GymAdapter(this, gymList, true);
        recyclerViewGymsAdmin.setAdapter(gymAdapter);

        // Thêm sự kiện click cho item (để sửa)
        gymAdapter.setOnItemClickListener(gym -> {
            Intent intent = new Intent(AdminGymActivity.this, EditGymActivity.class);
            intent.putExtra("gym_id", gym.getId());
            startActivity(intent);
        });

        // Xóa sự kiện long click cho item (để xóa)
        // gymAdapter.setOnItemLongClickListener(gym -> showDeleteDialog(gym));

        // Thiết lập listener cho nút xóa trên item
        gymAdapter.setOnDeleteClickListener(this); // Gán activity làm listener
    }

    private void loadGyms() {
        db.collection("gyms")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    gymList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Gym gym = document.toObject(Gym.class);
                        gym.setId(document.getId());
                        gymList.add(gym);
                    }
                    gymAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminGymActivity.this, "Lỗi khi tải danh sách phòng tập", Toast.LENGTH_SHORT).show();
                });
    }

    // Triển khai phương thức onDeleteClick từ interface GymAdapter.OnDeleteClickListener
    @Override
    public void onDeleteClick(Gym gym) {
        showDeleteDialog(gym);
    }

    private void showDeleteDialog(Gym gym) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa phòng tập")
                .setMessage("Bạn có chắc chắn muốn xóa phòng tập \"" + gym.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteGym(gym)) // Gọi hàm xóa khi nhấn OK
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteGym(Gym gym) {
        db.collection("gyms").document(gym.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa phòng tập: " + gym.getName(), Toast.LENGTH_SHORT).show();
                    loadGyms(); // Tải lại danh sách sau khi xóa
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa phòng tập: " + gym.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
package com.example.hadonggymapp;

import android.app.AlertDialog;
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

public class AdminTrainersActivity extends AppCompatActivity implements TrainerAdapter.OnDeleteClickListener {
    private static final String TAG = "AdminTrainersActivity";

    private RecyclerView recyclerViewTrainersAdmin;
    private TrainerAdapter trainerAdapter;
    private List<Trainer> trainerList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FloatingActionButton fabAddTrainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_trainers);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Huấn luyện viên");
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
        recyclerViewTrainersAdmin = findViewById(R.id.recyclerViewTrainersAdmin);
        fabAddTrainer = findViewById(R.id.fabAddTrainer);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click cho FAB
        fabAddTrainer.setOnClickListener(v -> {
            // TODO: Navigate to EditTrainerActivity for adding new trainer
            // Intent intent = new Intent(AdminTrainersActivity.this, EditTrainerActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Thêm huấn luyện viên (chưa triển khai)", Toast.LENGTH_SHORT).show(); // Placeholder
        });

        // Tải dữ liệu huấn luyện viên
        loadTrainers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi Activity trở lại foreground
        loadTrainers();
    }

    private void setupRecyclerView() {
        recyclerViewTrainersAdmin.setHasFixedSize(true);
        recyclerViewTrainersAdmin.setLayoutManager(new LinearLayoutManager(this));

        trainerAdapter = new TrainerAdapter(this, trainerList);
        recyclerViewTrainersAdmin.setAdapter(trainerAdapter);

        // Thêm sự kiện click cho item (để sửa)
        trainerAdapter.setOnItemClickListener(trainer -> {
            // TODO: Navigate to EditTrainerActivity for editing trainer
            // Intent intent = new Intent(AdminTrainersActivity.this, EditTrainerActivity.class);
            // intent.putExtra("trainer_id", trainer.getId());
            // startActivity(intent);
            Toast.makeText(this, "Sửa huấn luyện viên (chưa triển khai): " + trainer.getName(), Toast.LENGTH_SHORT).show(); // Placeholder
        });

        // Thiết lập listener cho nút xóa trên item
        trainerAdapter.setOnDeleteClickListener(this); // Gán activity làm listener
    }

    private void loadTrainers() {
        db.collection("trainers")
                .orderBy("name", Query.Direction.ASCENDING) // Sắp xếp theo tên
                .get() // Lấy dữ liệu một lần
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trainerList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Trainer trainer = document.toObject(Trainer.class);
                        if (trainer != null) {
                            trainer.setId(document.getId()); // Gán ID tài liệu Firestore vào đối tượng
                            trainerList.add(trainer);
                        }
                    }
                    trainerAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminTrainersActivity.this, "Lỗi khi tải danh sách huấn luyện viên", Toast.LENGTH_SHORT).show();
                });
    }

    // Triển khai phương thức onDeleteClick từ interface TrainerAdapter.OnDeleteClickListener
    @Override
    public void onDeleteClick(Trainer trainer) {
        showDeleteDialog(trainer);
    }

    private void showDeleteDialog(Trainer trainer) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa huấn luyện viên")
                .setMessage("Bạn có chắc chắn muốn xóa huấn luyện viên \"" + trainer.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTrainer(trainer)) // Gọi hàm xóa khi nhấn OK
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTrainer(Trainer trainer) {
        db.collection("trainers").document(trainer.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa huấn luyện viên: " + trainer.getName(), Toast.LENGTH_SHORT).show();
                    loadTrainers(); // Tải lại danh sách sau khi xóa
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa huấn luyện viên: " + trainer.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
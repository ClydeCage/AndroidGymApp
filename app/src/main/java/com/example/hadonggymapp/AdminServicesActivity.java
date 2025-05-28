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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminServicesActivity extends AppCompatActivity implements ServiceAdapter.OnDeleteClickListener {
    private static final String TAG = "AdminServicesActivity";

    private RecyclerView recyclerViewServicesAdmin;
    private ServiceAdapter serviceAdapter;
    private List<String> serviceList = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Dịch vụ");
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ views
        recyclerViewServicesAdmin = findViewById(R.id.recyclerViewServicesAdmin);
        fabAddService = findViewById(R.id.fabAddService);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click cho FAB
        fabAddService.setOnClickListener(v -> {
            Intent intent = new Intent(AdminServicesActivity.this, EditServiceActivity.class);
            startActivity(intent);
        });

        // Tải dữ liệu dịch vụ
        loadServices();
    }

     @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi Activity trở lại foreground
        loadServices();
    }

    private void setupRecyclerView() {
        recyclerViewServicesAdmin.setHasFixedSize(true);
        recyclerViewServicesAdmin.setLayoutManager(new LinearLayoutManager(this));

        serviceAdapter = new ServiceAdapter(serviceList);
        recyclerViewServicesAdmin.setAdapter(serviceAdapter);

        // Thêm sự kiện click cho item (để sửa)
        serviceAdapter.setOnItemClickListener(serviceName -> {
             Intent intent = new Intent(AdminServicesActivity.this, EditServiceActivity.class);
             intent.putExtra("service_name", serviceName);
             startActivity(intent);
        });

        // Xóa sự kiện long click cho item (để xóa)
        // serviceAdapter.setOnItemLongClickListener(serviceName -> showDeleteDialog(serviceName));

        // Thiết lập listener cho nút xóa trên item
        serviceAdapter.setOnDeleteClickListener(this); // Gán activity làm listener
    }

    private void loadServices() {
        db.collection("services")
                .orderBy("name", Query.Direction.ASCENDING) // Sắp xếp theo tên
                .get() // Lấy dữ liệu một lần
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String serviceName = document.getString("name");
                        if (serviceName != null) {
                            serviceList.add(serviceName); // Chỉ thêm tên vào danh sách
                        }
                    }
                    serviceAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading services", e);
                    Toast.makeText(AdminServicesActivity.this, "Lỗi khi tải danh sách dịch vụ", Toast.LENGTH_SHORT).show();
                });
    }

    // Triển khai phương thức onDeleteClick từ interface ServiceAdapter.OnDeleteClickListener
    @Override
    public void onDeleteClick(String serviceName) {
        showDeleteDialog(serviceName);
    }

    private void showDeleteDialog(String serviceName) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa dịch vụ")
                .setMessage("Bạn có chắc chắn muốn xóa dịch vụ \"" + serviceName + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteService(serviceName)) // Gọi hàm xóa khi nhấn OK
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteService(String serviceName) {
        // Để xóa document theo tên, cần tìm documentId trước
        db.collection("services")
                .whereEqualTo("name", serviceName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy document đầu tiên tìm được (giả sử tên dịch vụ là duy nhất)
                        String documentIdToDelete = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("services").document(documentIdToDelete)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đã xóa dịch vụ: " + serviceName, Toast.LENGTH_SHORT).show();
                                    loadServices(); // Tải lại danh sách sau khi xóa
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting service " + serviceName, e);
                                    Toast.makeText(this, "Lỗi khi xóa dịch vụ: " + serviceName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                         Toast.makeText(this, "Không tìm thấy dịch vụ để xóa: " + serviceName, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding service to delete " + serviceName, e);
                    Toast.makeText(this, "Lỗi khi tìm dịch vụ để xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Quay lại màn hình trước
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
package com.example.hadonggymapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectItemsActivity extends AppCompatActivity {
    private static final String TAG = "SelectItemsActivity";
    public static final String EXTRA_ITEM_TYPE = "extra_item_type";
    public static final String EXTRA_SELECTED_ITEMS = "extra_selected_items";
    public static final String RESULT_SELECTED_ITEMS = "result_selected_items";

    private RecyclerView recyclerViewItems;
    private Button buttonSaveSelection;
    private SelectableItemsAdapter adapter;
    private List<String> allItems = new ArrayList<>();
    private List<String> initialSelectedItems = new ArrayList<>();
    private String itemType;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Lấy loại item (services/amenities) và danh sách đã chọn từ Intent
        itemType = getIntent().getStringExtra(EXTRA_ITEM_TYPE);
        initialSelectedItems = getIntent().getStringArrayListExtra(EXTRA_SELECTED_ITEMS);

        if (itemType == null) {
            Toast.makeText(this, "Lỗi: Không rõ loại mục cần chọn.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
             if (itemType.equals("services")) {
                 getSupportActionBar().setTitle("Chọn Dịch vụ");
             } else if (itemType.equals("amenities")) {
                 getSupportActionBar().setTitle("Chọn Tiện ích");
             }
        }

        // Ánh xạ views
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        buttonSaveSelection = findViewById(R.id.buttonSaveSelection);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải dữ liệu từ Firestore
        loadItems(itemType);

        // Thiết lập sự kiện click cho nút lưu
        buttonSaveSelection.setOnClickListener(v -> saveSelection());
    }

    private void setupRecyclerView() {
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectableItemsAdapter(allItems, initialSelectedItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void loadItems(String collectionName) {
        db.collection(collectionName)
                .orderBy("name") // Sắp xếp theo tên
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemName = document.getString("name");
                        if (itemName != null) {
                            allItems.add(itemName);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading items from " + collectionName, e);
                    Toast.makeText(SelectItemsActivity.this, "Lỗi khi tải danh sách", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSelection() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(RESULT_SELECTED_ITEMS, new ArrayList<>(adapter.getSelectedItems()));
        setResult(RESULT_OK, resultIntent);
        finish();
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
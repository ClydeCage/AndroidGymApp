package com.example.hadonggymapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoriteGymsActivity extends AppCompatActivity {
    private static final String TAG = "FavoriteGymsActivity";

    private RecyclerView recyclerViewFavoriteGyms;
    private GymAdapter gymAdapter;
    private List<Gym> favoriteGymList;
    private ProgressBar progressBar;
    private TextView textViewEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FavoriteGymManager favoriteGymManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_gyms);

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Phòng tập yêu thích");
        }

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        favoriteGymManager = new FavoriteGymManager();

        // Ánh xạ views
        recyclerViewFavoriteGyms = findViewById(R.id.recyclerViewFavoriteGyms);
        progressBar = findViewById(R.id.progressBar);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập SwipeRefreshLayout
        setupSwipeRefreshLayout();

        // Kiểm tra đăng nhập và tải dữ liệu
        checkLoginAndLoadData();
    }

    private void setupRecyclerView() {
        recyclerViewFavoriteGyms.setHasFixedSize(true);
        recyclerViewFavoriteGyms.setLayoutManager(new LinearLayoutManager(this));

        favoriteGymList = new ArrayList<>();
        gymAdapter = new GymAdapter(this, favoriteGymList, false);
        recyclerViewFavoriteGyms.setAdapter(gymAdapter);

        // Thiết lập click listener
        gymAdapter.setOnItemClickListener(gym -> {
            // Mở màn hình chi tiết phòng gym
            Intent intent = new Intent(this, GymDetailActivity.class);
            intent.putExtra(GymDetailActivity.EXTRA_GYM_OBJECT, gym);
            startActivity(intent);
        });
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::loadFavoriteGyms);
    }

    private void checkLoginAndLoadData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Chưa đăng nhập
            showEmptyState("Vui lòng đăng nhập để xem danh sách yêu thích");
            return;
        }
        loadFavoriteGyms();
    }

    private void loadFavoriteGyms() {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerViewFavoriteGyms.setVisibility(View.GONE);
        textViewEmptyState.setVisibility(View.GONE);

        // Lấy danh sách gymId yêu thích từ local
        java.util.Set<String> favoriteIds = FavoriteLocalManager.getFavorites(this);
        // Lấy danh sách phòng gym từ Firestore (hoặc local)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("gyms").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Gym> allGyms = new ArrayList<>();
            for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Gym gym = document.toObject(Gym.class);
                if (gym != null) {
                    gym.setId(document.getId());
                    // Lấy thủ công averageRating và reviewCount nếu có
                    if (document.contains("averageRating")) {
                        Double avg = document.getDouble("averageRating");
                        gym.setAverageRating(avg != null ? avg.floatValue() : 0f);
                    }
                    if (document.contains("reviewCount")) {
                        Long count = document.getLong("reviewCount");
                        gym.setReviewCount(count != null ? count.intValue() : 0);
                    }
                    allGyms.add(gym);
                }
            }
            // Lọc các gym đã yêu thích
            favoriteGymList.clear();
            for (Gym gym : allGyms) {
                if (favoriteIds.contains(gym.getId())) {
                    favoriteGymList.add(gym);
                }
            }
            gymAdapter.notifyDataSetChanged();
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            progressBar.setVisibility(View.GONE);
            if (favoriteGymList.isEmpty()) {
                showEmptyState("Bạn chưa có phòng tập yêu thích nào");
            } else {
                recyclerViewFavoriteGyms.setVisibility(View.VISIBLE);
                textViewEmptyState.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            progressBar.setVisibility(View.GONE);
            showEmptyState("Lỗi khi tải danh sách phòng gym");
        });
    }

    private void showEmptyState(String message) {
        recyclerViewFavoriteGyms.setVisibility(View.GONE);
        textViewEmptyState.setVisibility(View.VISIBLE);
        textViewEmptyState.setText(message);
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
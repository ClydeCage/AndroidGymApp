package com.example.hadonggymapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CircleImageView imageViewUserProfile;
    private TextView textViewUserName, textViewUserEmail;
    private Button buttonChangePassword, buttonLogoutProfile, buttonEditProfile;
    private RecyclerView recyclerViewWorkoutSchedules;
    private WorkoutScheduleAdapter scheduleAdapter;
    private List<WorkoutSchedule> scheduleList;
    private Button buttonViewCancelledSchedules;
    private boolean showingCancelledSchedules = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông tin Tài khoản");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng không tồn tại.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();
        setupButtonListeners();
        setupRecyclerView();
        loadWorkoutSchedules();
    }

    private void initializeViews() {
        imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonLogoutProfile = findViewById(R.id.buttonLogoutProfile);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        recyclerViewWorkoutSchedules = findViewById(R.id.recyclerViewWorkoutSchedules);
        buttonViewCancelledSchedules = findViewById(R.id.buttonViewCancelledSchedules);
    }

    private void setupRecyclerView() {
        scheduleList = new ArrayList<>();
        scheduleAdapter = new WorkoutScheduleAdapter(scheduleList);
        recyclerViewWorkoutSchedules.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkoutSchedules.setAdapter(scheduleAdapter);

        scheduleAdapter.setOnItemClickListener(schedule -> {
            Intent intent = new Intent(UserProfileActivity.this, WorkoutScheduleDetailActivity.class);
            intent.putExtra("workout_schedule", schedule);
            startActivity(intent);
        });
    }

    private void loadWorkoutSchedules() {
        if (currentUser != null) {
            com.google.firebase.firestore.Query query = db.collection("workout_schedules")
                    .whereEqualTo("userId", currentUser.getUid());

            if (showingCancelledSchedules) {
                query = query.whereEqualTo("status", "cancelled");
            } else {
                query = query.whereNotEqualTo("status", "cancelled");
            }

            query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WorkoutSchedule schedule = document.toObject(WorkoutSchedule.class);
                        scheduleList.add(schedule);
                    }
                    scheduleAdapter.updateSchedules(scheduleList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading workout schedules", e);
                    Toast.makeText(UserProfileActivity.this,
                            "Lỗi khi tải lịch tập",
                            Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            if (displayName != null && !displayName.isEmpty()) {
                textViewUserName.setText(displayName);
            } else {
                if (email != null && email.contains("@")) {
                    textViewUserName.setText(email.substring(0, email.indexOf('@')));
                } else {
                    textViewUserName.setText("Người dùng");
                }
            }
            textViewUserEmail.setText(email);

            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(imageViewUserProfile);
            } else {
                imageViewUserProfile.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void setupButtonListeners() {
        buttonLogoutProfile.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(UserProfileActivity.this, "Đã đăng xuất.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
        });

        buttonChangePassword.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getEmail() != null) {
                String email = currentUser.getEmail();
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this, 
                                "Link đổi mật khẩu đã được gửi đến " + email, 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, 
                                "Lỗi gửi link đổi mật khẩu.", 
                                Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error sending password reset email", task.getException());
                        }
                    });
            } else {
                Toast.makeText(UserProfileActivity.this, 
                    "Không thể lấy thông tin email.", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, 1);
        });

        buttonViewCancelledSchedules.setOnClickListener(v -> {
            showingCancelledSchedules = !showingCancelledSchedules;
            if (showingCancelledSchedules) {
                buttonViewCancelledSchedules.setText("Xem lịch tập hiện tại");
            } else {
                buttonViewCancelledSchedules.setText("Xem lịch sử hủy");
            }
            loadWorkoutSchedules(); // Reload data based on the new state
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh profile data
            currentUser = mAuth.getCurrentUser();
            loadUserProfile();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkoutSchedules(); // Refresh schedules when returning to this activity
    }
}
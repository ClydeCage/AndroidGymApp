package com.example.hadonggymapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.ArrayList;

import com.example.hadonggymapp.FavoriteLocalManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class GymDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GYM_OBJECT = "extra_gym_object";
    private static final String GOOGLE_STATIC_MAPS_API_KEY = "AIzaSyCNjS903Rm1M9vDcZ_lL-xCWDQRWwlv1bQ"; // <-- Thay YOUR_API_KEY bằng API Key của bạn

    // Khai báo các View từ layout activity_gym_detail.xml
    private ViewPager2 viewPagerGymImages;
    private TextView textViewGymName;
    private TextView textViewGymAddress;
    private TextView textViewGymPhone;
    private TextView textViewGymHours;
    private TextView textViewGymDescription;
    private ChipGroup chipGroupServices;
    private ChipGroup chipGroupAmenities;
    private ImageView imageViewStaticMap; // Thêm ImageView cho static map
    private Button buttonCall;
    private Button buttonDirection;
    private RecyclerView recyclerViewReviews;
    private Button buttonAddReview;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private TextView textViewAverageRating;
    private RatingBar ratingBarAverage;

    private Gym currentGym;
    private String currentUserId = null;
    private String currentUserName = null;
    private Review myReview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.gym_detail_activity_title));
        }

        // Ánh xạ Views
        viewPagerGymImages = findViewById(R.id.viewPagerGymImages);
        textViewGymName = findViewById(R.id.textViewGymName);
        textViewGymAddress = findViewById(R.id.textViewGymAddress);
        textViewGymPhone = findViewById(R.id.textViewGymPhone);
        textViewGymHours = findViewById(R.id.textViewGymHours);
        textViewGymDescription = findViewById(R.id.textViewGymDescription);
        chipGroupServices = findViewById(R.id.chipGroupServices);
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities);
        imageViewStaticMap = findViewById(R.id.imageViewStaticMap); // Ánh xạ ImageView
        buttonCall = findViewById(R.id.buttonCall);
        buttonDirection = findViewById(R.id.buttonDirection);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        buttonAddReview = findViewById(R.id.buttonAddReview);
        textViewAverageRating = findViewById(R.id.textViewAverageRating);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);

        // Lấy dữ liệu Gym object từ Intent
        currentGym = (Gym) getIntent().getSerializableExtra(EXTRA_GYM_OBJECT);

        // Lấy userId và userName thực tế từ FirebaseAuth
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            // Lấy tên từ profile hoặc Firestore
            String displayName = firebaseUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                currentUserName = displayName;
            } else {
                // Nếu chưa có displayName, lấy từ Firestore
                FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                currentUserName = name;
                            } else if (firebaseUser.getEmail() != null) {
                                currentUserName = firebaseUser.getEmail();
                            } else {
                                currentUserName = "Người dùng";
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (firebaseUser.getEmail() != null) {
                                currentUserName = firebaseUser.getEmail();
                            } else {
                                currentUserName = "Người dùng";
                            }
                        });
            }
        } else {
            // Nếu chưa đăng nhập, không cho đánh giá
            currentUserId = null;
            currentUserName = null;
        }

        if (currentGym != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentGym.getName());
            }
            displayGymDetails(currentGym);
            setupButtonListeners(currentGym);
            setupUI();

            // Xử lý click vào ảnh map tĩnh để mở chỉ đường
            imageViewStaticMap.setOnClickListener(v -> openGoogleMapsDirection(currentGym));

            recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
            reviewAdapter = new ReviewAdapter(this, reviewList);
            recyclerViewReviews.setAdapter(reviewAdapter);
            loadReviews();
            checkMyReview();
            buttonAddReview.setOnClickListener(v -> {
                showReviewDialog();
            });

        } else {
            Toast.makeText(this, getString(R.string.error_loading_gym_details), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayGymDetails(Gym gym) {
        // Hiển thị ảnh bằng ViewPager2
        if (gym.getImageUrls() != null && !gym.getImageUrls().isEmpty()) {
            GymImageAdapter imageAdapter = new GymImageAdapter(gym.getImageUrls());
            viewPagerGymImages.setAdapter(imageAdapter);
            viewPagerGymImages.setVisibility(View.VISIBLE);
        } else if (gym.getImageUrl() != null && !gym.getImageUrl().isEmpty()) {
             // Nếu chỉ có imageUrl chính, tạo list 1 ảnh và hiển thị
             List<String> imageUrls = new java.util.ArrayList<>();
             imageUrls.add(gym.getImageUrl());
             GymImageAdapter imageAdapter = new GymImageAdapter(imageUrls);
             viewPagerGymImages.setAdapter(imageAdapter);
             viewPagerGymImages.setVisibility(View.VISIBLE);
        }
        else {
            viewPagerGymImages.setVisibility(View.GONE);
        }

        // Hiển thị các thông tin khác
        textViewGymName.setText(gym.getName());
        textViewGymAddress.setText("Địa chỉ: " + (gym.getAddress() != null ? gym.getAddress() : "Đang cập nhật"));

        if (gym.getPhone() != null && !gym.getPhone().isEmpty()) {
            textViewGymPhone.setText("Điện thoại: " + gym.getPhone());
            textViewGymPhone.setVisibility(View.VISIBLE);
        } else {
            textViewGymPhone.setVisibility(View.GONE);
        }

        textViewGymHours.setText("Giờ mở cửa: " + (gym.getHours() != null ? gym.getHours() : "Đang cập nhật"));
        textViewGymDescription.setText(gym.getDescription() != null ? gym.getDescription() : "Đang cập nhật mô tả...");

        // Hiển thị danh sách dịch vụ bằng ChipGroup
        if (gym.getServices() != null && !gym.getServices().isEmpty()) {
            chipGroupServices.setVisibility(View.VISIBLE);
            addChipsToChipGroup(chipGroupServices, gym.getServices());
        } else {
            chipGroupServices.setVisibility(View.GONE);
        }

        // Hiển thị danh sách tiện ích bằng ChipGroup
        if (gym.getAmenities() != null && !gym.getAmenities().isEmpty()) {
            chipGroupAmenities.setVisibility(View.VISIBLE);
            addChipsToChipGroup(chipGroupAmenities, gym.getAmenities());
        } else {
            chipGroupAmenities.setVisibility(View.GONE);
        }

        // Hiển thị Static Map Image nếu có tọa độ hợp lệ
        if (gym.getLatitude() != null && gym.getLongitude() != null && gym.getLatitude() != 0.0 && gym.getLongitude() != 0.0) {
            String staticMapUrl = getStaticMapUrl(gym.getLatitude(), gym.getLongitude());
            Log.d("StaticMapDebug", "Static Map URL: " + staticMapUrl);
            Glide.with(this)
                    .load(staticMapUrl)
                    .placeholder(R.drawable.ic_person) // Placeholder tạm
                    .error(R.drawable.ic_person) // Ảnh lỗi tạm
                    .into(imageViewStaticMap);
            imageViewStaticMap.setVisibility(View.VISIBLE);
        } else {
            imageViewStaticMap.setVisibility(View.GONE);
            // Hiển thị lại nút Chỉ đường nếu không có tọa độ để hiển thị map tĩnh
            if (gym.getAddress() != null && !gym.getAddress().isEmpty()) {
                 buttonDirection.setVisibility(View.VISIBLE);
            }
        }
    }

    // Hàm helper để thêm Chips vào ChipGroup
    private void addChipsToChipGroup(ChipGroup chipGroup, List<String> items) {
        chipGroup.removeAllViews();
        for (String item : items) {
            Chip chip = new Chip(this);
            chip.setText(item);
            chip.setClickable(false);
            chipGroup.addView(chip);
        }
    }

    private void setupButtonListeners(Gym gym) {
        // Nút Gọi điện
        if (gym.getPhone() != null && !gym.getPhone().isEmpty()) {
            buttonCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + gym.getPhone()));
                startActivity(intent);
            });
        } else {
            buttonCall.setVisibility(View.GONE);
        }

        // Nút Chỉ đường (Hiển thị nếu không có tọa độ để hiển thị map tĩnh)
        if (gym.getAddress() != null && !gym.getAddress().isEmpty()) {
            buttonDirection.setOnClickListener(v -> {
                openGoogleMapsDirection(gym);
            });
        } else {
            buttonDirection.setVisibility(View.GONE);
        }

        // TODO: Add listeners for other buttons like Register Trial, Rate, etc.
    }

    private void setupUI() {
        // Add Schedule Button
        MaterialButton buttonSchedule = findViewById(R.id.buttonSchedule);
        buttonSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(GymDetailActivity.this, ScheduleWorkoutActivity.class);
            intent.putExtra("gym", currentGym);
            startActivity(intent);
        });
    }

    // Hàm tạo URL cho Google Static Maps API
    private String getStaticMapUrl(Double latitude, Double longitude) {
        // Kiểm tra null trước khi sử dụng giá trị
        if (latitude == null || longitude == null) {
            return null; // Trả về null nếu tọa độ không hợp lệ
        }
        // Kích thước ảnh (widthxheight), zoom level (15), loại map (roadmap), marker (tọa độ, màu đỏ)
        String size = "600x300";
        String zoom = "15";
        String maptype = "roadmap";
        String markers = "color:red%7Clabel:G%7C" + latitude + "," + longitude;
        String center = latitude + "," + longitude;

        // Xây dựng URL hoàn chỉnh
        String url = "https://maps.googleapis.com/maps/api/staticmap"
                + "?center=" + center
                + "&zoom=" + zoom
                + "&size=" + size
                + "&maptype=" + maptype
                + "&markers=" + markers
                + "&key=" + GOOGLE_STATIC_MAPS_API_KEY;

        return url;
    }

    // Hàm mở Google Maps chỉ đường (Tái sử dụng)
    private void openGoogleMapsDirection(Gym gym) {
        if (gym != null && gym.getAddress() != null && !gym.getAddress().isEmpty()) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(gym.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Không tìm thấy ứng dụng bản đồ.", Toast.LENGTH_SHORT).show();
            }
        } else {
             Toast.makeText(this, "Không có địa chỉ để chỉ đường.", Toast.LENGTH_SHORT).show();
        }
    }

    // Xử lý sự kiện khi nhấn nút "Back" trên ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Adapter cho ViewPager2
    private class GymImageAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<GymImageAdapter.ImageViewHolder> {
        private List<String> imageUrls;

        public GymImageAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

    private void loadReviews() {
        if (currentGym == null || currentGym.getId() == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("gyms")
            .document(currentGym.getId())
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                reviewList.clear();
                float totalRating = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Review review = doc.toObject(Review.class);
                    review.setId(doc.getId());
                    reviewList.add(review);
                    totalRating += review.getRating();
                }
                reviewAdapter.setReviewList(reviewList);
                // Tính điểm trung bình
                if (reviewList.size() > 0) {
                    float avg = totalRating / reviewList.size();
                    textViewAverageRating.setText(String.format("★ %.1f (%d đánh giá)", avg, reviewList.size()));
                    // Cập nhật trường trung bình lên Firestore
                    db.collection("gyms").document(currentGym.getId())
                        .update("averageRating", avg, "reviewCount", reviewList.size());
                    currentGym.setAverageRating(avg);
                    currentGym.setReviewCount(reviewList.size());
                    if (ratingBarAverage != null) ratingBarAverage.setRating(avg);
                } else {
                    textViewAverageRating.setText("Chưa có đánh giá");
                    db.collection("gyms").document(currentGym.getId())
                        .update("averageRating", 0, "reviewCount", 0);
                    currentGym.setAverageRating(0);
                    currentGym.setReviewCount(0);
                    if (ratingBarAverage != null) ratingBarAverage.setRating(0);
                }
            });
    }

    private void checkMyReview() {
        if (currentGym == null || currentGym.getId() == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("gyms")
            .document(currentGym.getId())
            .collection("reviews")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    myReview = queryDocumentSnapshots.getDocuments().get(0).toObject(Review.class);
                    myReview.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                    buttonAddReview.setText("Sửa/Xóa đánh giá của bạn");
                } else {
                    myReview = null;
                    buttonAddReview.setText("Đánh giá phòng gym này");
                }
            });
    }

    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarInput);
        EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        Button buttonDelete = new Button(this);
        buttonDelete.setText("Xóa đánh giá");
        if (myReview != null) {
            ratingBar.setRating(myReview.getRating());
            editTextComment.setText(myReview.getComment());
            // Thêm nút xóa nếu đã có review
            ((LinearLayout) dialogView).addView(buttonDelete);
        }
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.buttonSave).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = editTextComment.getText().toString().trim();
            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }
            saveOrUpdateReview(rating, comment);
            dialog.dismiss();
        });
        buttonDelete.setOnClickListener(v -> {
            deleteMyReview();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void saveOrUpdateReview(float rating, String comment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String gymId = currentGym.getId();
        long now = System.currentTimeMillis();
        if (myReview == null) {
            // Thêm mới
            Review review = new Review(null, currentUserId, currentUserName, gymId, rating, comment, now);
            db.collection("gyms").document(gymId).collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    checkMyReview();
                });
        } else {
            // Sửa
            db.collection("gyms").document(gymId).collection("reviews").document(myReview.getId())
                .set(new Review(myReview.getId(), currentUserId, currentUserName, gymId, rating, comment, now))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật đánh giá!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    checkMyReview();
                });
        }
    }

    private void deleteMyReview() {
        if (myReview == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("gyms").document(currentGym.getId()).collection("reviews").document(myReview.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã xóa đánh giá!", Toast.LENGTH_SHORT).show();
                loadReviews();
                checkMyReview();
            });
    }
}
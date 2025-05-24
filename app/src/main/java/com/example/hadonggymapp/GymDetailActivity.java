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

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

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

    private Gym currentGym;

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

        // Lấy dữ liệu Gym object từ Intent
        currentGym = (Gym) getIntent().getSerializableExtra(EXTRA_GYM_OBJECT);

        if (currentGym != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentGym.getName());
            }
            displayGymDetails(currentGym);
            setupButtonListeners(currentGym);

            // Xử lý click vào ảnh map tĩnh để mở chỉ đường
            imageViewStaticMap.setOnClickListener(v -> openGoogleMapsDirection(currentGym));

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
        textViewGymAddress.setText("Địa chỉ: " + gym.getAddress());

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

        // Hiển thị Static Map Image nếu có tọa độ
        if (gym.getLatitude() != 0.0 && gym.getLongitude() != 0.0) {
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

    // Hàm tạo URL cho Google Static Maps API
    private String getStaticMapUrl(double latitude, double longitude) {
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
}
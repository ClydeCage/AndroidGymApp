package com.example.hadonggymapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // Cho nút Back trên ActionBar
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class GymDetailActivity extends AppCompatActivity {

    // Khai báo các View từ layout activity_gym_detail.xml
    private ImageView imageViewDetailGym;
    private TextView textViewDetailGymName;
    private TextView textViewDetailGymAddress;
    private TextView textViewDetailGymPhone;
    private TextView textViewDetailDescription; // Nếu bạn có TextView này

    // Định nghĩa các key để nhận dữ liệu (nên giống với key đã gửi từ MainActivity)
    public static final String EXTRA_GYM_OBJECT = "GYM_OBJECT_KEY";
    // public static final String EXTRA_GYM_DESCRIPTION = "GYM_DESCRIPTION_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_detail);

        // Thêm nút "Back" (mũi tên quay lại) trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.gym_detail_activity_title)); // Dòng mới
        }

        // Ánh xạ các View
        imageViewDetailGym = findViewById(R.id.imageViewDetailGym);
        textViewDetailGymName = findViewById(R.id.textViewDetailGymName);
        textViewDetailGymAddress = findViewById(R.id.textViewDetailGymAddress);
        textViewDetailGymPhone = findViewById(R.id.textViewDetailGymPhone);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription); // Ánh xạ nếu có

        // Nhận Intent đã khởi chạy Activity này
        Intent intent = getIntent();

        // Kiểm tra xem Intent có dữ liệu không (để tránh NullPointerException)
        if (intent != null&& intent.hasExtra(EXTRA_GYM_OBJECT)) {
            // Nhận đối tượng Gym từ Intent
            Gym selectedGym = (Gym) intent.getSerializableExtra(EXTRA_GYM_OBJECT);

            if (selectedGym != null) {
                // Hiển thị dữ liệu từ đối tượng selectedGym
                if (selectedGym.getName() != null) {
                    textViewDetailGymName.setText(selectedGym.getName());
                    // Nếu muốn đặt tiêu đề ActionBar bằng tên gym
                    // if (getSupportActionBar() != null) {
                    //    getSupportActionBar().setTitle(selectedGym.getName());
                    // }
                }

                if (selectedGym.getAddress() != null) {
                    textViewDetailGymAddress.setText(getString(R.string.label_address_prefix) + selectedGym.getAddress());
                }

                if (selectedGym.getPhone() != null && !selectedGym.getPhone().isEmpty()) {
                    textViewDetailGymPhone.setText(getString(R.string.label_phone_prefix) + selectedGym.getPhone());
                    textViewDetailGymPhone.setVisibility(android.view.View.VISIBLE);
                } else {
                    textViewDetailGymPhone.setText(getString(R.string.gym_phone_not_available));
                    textViewDetailGymPhone.setVisibility(android.view.View.VISIBLE);
                    // Hoặc: textViewDetailGymPhone.setVisibility(android.view.View.GONE);
                }

                // SỬ DỤNG GLIDE ĐỂ TẢI ẢNH TỪ URL
                if (selectedGym.getImageUrl() != null && !selectedGym.getImageUrl().isEmpty()) {
                    Glide.with(this) // 'this' ở đây là Context của GymDetailActivity
                            .load(selectedGym.getImageUrl())
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .centerCrop() // Hoặc .fitCenter()
                            .into(imageViewDetailGym);
                } else {
                    imageViewDetailGym.setImageResource(R.mipmap.ic_launcher);
                }

                // Tương tự cho mô tả nếu bạn có trường description trong Gym.java
                if (selectedGym.getDescription() != null && !selectedGym.getDescription().isEmpty()) {
                    textViewDetailDescription.setText(getString(R.string.label_description_prefix) + selectedGym.getDescription());
                } else {
                    // Hiển thị mô tả mặc định nếu không có mô tả từ đối tượng Gym
                    textViewDetailDescription.setText(getString(R.string.label_description_prefix) + getString(R.string.default_description_gym));
                    // Hoặc bạn có thể ẩn TextView này nếu không có mô tả:
                    // textViewDetailDescription.setVisibility(View.GONE);
                }

            } else {
                // selectedGym là null sau khi ép kiểu, xử lý lỗi
                Toast.makeText(this, getString(R.string.error_loading_gym_details), Toast.LENGTH_SHORT).show();
                finish(); // Đóng activity nếu không có dữ liệu hợp lệ
            }
        } else {
            // Intent không có key EXTRA_GYM_OBJECT hoặc intent là null
            Toast.makeText(this, getString(R.string.error_loading_gym_details), Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity
        }
    }

    // Xử lý sự kiện khi nhấn nút "Back" trên ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Kết thúc Activity hiện tại và quay lại Activity trước đó (MainActivity)
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
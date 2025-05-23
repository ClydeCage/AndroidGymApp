package com.example.hadonggymapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đổi mật khẩu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
} 
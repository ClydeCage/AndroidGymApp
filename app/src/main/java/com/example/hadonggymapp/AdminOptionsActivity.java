package com.example.hadonggymapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AdminOptionsActivity extends AppCompatActivity {

    private Button buttonAddGym;
    private Button buttonManageServices;
    private Button buttonManageAmenities;
    private Button buttonManageTrainers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_options);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tùy chọn Admin");
        }

        buttonAddGym = findViewById(R.id.buttonAddGym);
        buttonManageServices = findViewById(R.id.buttonManageServices);
        buttonManageAmenities = findViewById(R.id.buttonManageAmenities);
        buttonManageTrainers = findViewById(R.id.buttonManageTrainers);

        buttonAddGym.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOptionsActivity.this, EditGymActivity.class);
            startActivity(intent);
        });

        buttonManageServices.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOptionsActivity.this, AdminServicesActivity.class);
            startActivity(intent);
        });

        buttonManageAmenities.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOptionsActivity.this, AdminAmenitiesActivity.class);
            startActivity(intent);
        });

        buttonManageTrainers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOptionsActivity.this, AdminTrainersActivity.class);
            startActivity(intent);
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
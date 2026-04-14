package com.example.admin_app.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.admin_app.Fragment.ProfileFragment;
import com.example.admin_app.Fragment.OrdersFragment;
import com.example.admin_app.Fragment.FlightsFragment;
import com.example.admin_app.Fragment.DashboardFragment;


import com.example.admin_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ XML
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load trang mặc định khi vừa mở App
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new DashboardFragment())
                    .commit();
        }


        // Bắt sự kiện khi click vào các nút trên thanh Nav
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Lấy ID của item người dùng vừa bấm (ID này nằm trong file bottom_nav_menu.xml)
            int itemId = item.getItemId();

            //
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_flight) {
                selectedFragment = new FlightsFragment();
            } else if (itemId == R.id.nav_order) {
                selectedFragment = new OrdersFragment();
            }
            else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // Thực hiện việc tráo đổi màn hình
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();

                return true;
            }

            return false;
        });
    }

}
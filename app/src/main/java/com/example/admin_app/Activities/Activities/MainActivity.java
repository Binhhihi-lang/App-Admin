package com.example.admin_app.Activities.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.admin_app.Activities.DashboardFragment;
import com.example.admin_app.Activities.FlightsFragment;
import com.example.admin_app.Activities.OrdersFragment;
import com.example.admin_app.Activities.ProfileFragment;
import com.example.admin_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
package com.example.admin_app.Activities.Activities;

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

import com.example.admin_app.Activities.Fragment.DashboardFragment;
import com.example.admin_app.Activities.Fragment.FlightsFragment;
import com.example.admin_app.Activities.Fragment.OrdersFragment;
import com.example.admin_app.Activities.Fragment.ProfileFragment;
import com.example.admin_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int MY_REQUEST_CODE = 10;
    private final AdminEditProfileActivity adminEditProfileActivity = new AdminEditProfileActivity();

    // chọn ảnh từ gallery
    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        // xử lý kết quả trả về
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK){
                Intent intent = result.getData();
                if (intent == null){
                    return;
                }
                Uri uri = intent.getData();
                adminEditProfileActivity.setMuri(uri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    adminEditProfileActivity.setBitmapImageView(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == MY_REQUEST_CODE){
            // lấy phần tử đầu tiên thỏa mãn điều kiện
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
}
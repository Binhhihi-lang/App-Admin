package com.example.admin_app.Activities;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.admin_app.Models.User;
import com.example.admin_app.Fragment.ProfileFragment;
import com.example.admin_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;

public class AdminEditProfileActivity extends AppCompatActivity {

    MaterialToolbar toolbar ;
    EditText edtFullName, etdEmail, edtPhone, edtGender, edtDob, edtCccd;
    ImageView imgAvatar, imgSmallAvatar,imgchangeAvatar;
    Button btnSaveChanges;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    private Uri muri;


    public void setMuri(Uri muri) {
        this.muri = muri;
    }

    // xin quyền mở ảnh
    public static final int MY_REQUEST_CODE = 10;
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
                        setMuri(uri);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            setBitmapImageView(bitmap);
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
        setContentView(R.layout.activity_admin_edit_profile);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbarEditProfile);
        edtFullName = findViewById(R.id.edtFullName);
        etdEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtGender = findViewById(R.id.edtGender);
        edtDob = findViewById(R.id.edtDob);
        edtCccd = findViewById(R.id.edtCccd);
        imgAvatar = findViewById(R.id.imgEditAvatar);
        imgSmallAvatar = findViewById(R.id.imgSmallAvatar);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);




        // nút back lại
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed()
        );


        // date
        edtDob.setOnClickListener(v -> {
            // Lấy ngày tháng năm hiện tại để hiển thị mặc định trên Lịch
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Bật hộp thoại Lịch
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdminEditProfileActivity.this,
                    (view, yearSelected, monthOfYear, dayOfMonth) -> {
                        // Khi người dùng chọn xong, format lại chuỗi ngày và gán vào EditText
                        // Cộng 1 vào monthOfYear vì tháng trong Java bắt đầu từ số 0
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                        edtDob.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // giới tính
        edtGender.setOnClickListener(v -> {
            // Tạo mảng các lựa chọn
            String[] genders = {"Male", "Female", "Other"};

            // Bật hộp thoại chọn
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditProfileActivity.this);
            builder.setTitle("Select Gender");
            builder.setItems(genders, (dialog, which) -> {
                // which là vị trí index mà người dùng đã click (0, 1 hoặc 2)
                edtGender.setText(genders[which]);
            });
            builder.show();
        });

        // lấy thông tin người dùng show lên
        setUserInfomation();

        initListener();

    }

    public void setUserInfomation(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            return;
        }

        // Lấy UID của user đang đăng nhập
        String uid = currentUser.getUid();

        // Truy cập vào nhánh "Users"
        mDatabase.child("Users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Ép kiểu dữ liệu lấy về thành class User
                User userProfile = task.getResult().getValue(User.class);

                if (userProfile != null) {
                    // Đổ dữ liệu từ model User lên EditText
                    edtFullName.setText(userProfile.getFullName());
                    etdEmail.setText(userProfile.getEmail());
                    edtPhone.setText(userProfile.getPhoneNumber());
                    edtGender.setText(userProfile.getGender());
                    edtDob.setText(userProfile.getDob());
                    edtCccd.setText(userProfile.getCccd());
                    // Kiểm tra và load ảnh bằng Glide
                    if (!userProfile.getAvatar().isEmpty()) {
                        Glide.with(this)
                                .load(userProfile.getAvatar())
                                .placeholder(R.drawable.avatar_default) // Ảnh hiện trong lúc chờ tải
                                .error(R.drawable.ic_nav_profile)         // Ảnh hiện nếu bị lỗi
                                .into(imgAvatar);

                        // ảnh small
                        Glide.with(this)
                                .load(userProfile.getAvatar())
                                .placeholder(R.drawable.avatar_default) // Ảnh hiện trong lúc chờ tải
                                .error(R.drawable.ic_nav_profile)         // Ảnh hiện nếu bị lỗi
                                .into(imgSmallAvatar);
                    }
                }
            } else {
                // Xử lý lỗi nếu không tìm thấy dữ liệu
                Toast.makeText(this, "Không thể tải dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //
    private void initListener(){
        // sự kiện lưu ảnh
        imgchangeAvatar.setOnClickListener(v -> {
            onClickRequestPermission();
        });

        // sk lưu thông tin
        btnSaveChanges.setOnClickListener(view -> {
//            onClickUpdateProfile();

        });

    }

    // Hàm kiểm tra và xin quyền mở nơi chứa ảnh
    private void onClickRequestPermission() {
        // Nếu chạy trên Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES};
                requestPermissions(permissions, MY_REQUEST_CODE);
            }
        }
        // Nếu chạy trên Android 6.0 đến Android 12
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, MY_REQUEST_CODE);
            }
        }
        // Android dưới 6.0
        else {
            openGallery();
        }
    }

    // nhận kết quả
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng vừa bấm "Cho phép"
                openGallery();
            } else {
                // Người dùng bấm "Từ chối"
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    // set bitmap
    public void setBitmapImageView(Bitmap bitmapImageView){
        imgSmallAvatar.setImageBitmap(bitmapImageView);
    }

    // update profile trên trang firebase
//    public void onClickUpdateProfile() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            return;
//        }
//
//        String strFullName = edtFullName.getText().toString().trim();
//        String strPhone = edtPhone.getText().toString().trim();
//        String strGender = edtGender.getText().toString().trim();
//        String strDob = edtDob.getText().toString().trim();
//        String strCccd = edtCccd.getText().toString().trim();
//
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(strFullName)
//                .setDisplayName(strPhone)
//                .setDisplayName(strGender)
//                .setDisplayName(strDob)
//                .setDisplayName(strCccd)
//                .setPhotoUri(muri)
//                .build();
//
//        user.updateProfile(profileUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//                });
//    }



}
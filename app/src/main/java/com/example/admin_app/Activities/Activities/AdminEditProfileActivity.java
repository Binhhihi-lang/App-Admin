package com.example.admin_app.Activities.Activities;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;
import static com.example.admin_app.Activities.Activities.MainActivity.MY_REQUEST_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.admin_app.Activities.Models.User;
import com.example.admin_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

public class AdminEditProfileActivity extends AppCompatActivity {

    MaterialToolbar toolbar ;
    EditText edtFullName, etdEmail, edtPhone, edtGender, edtDob, edtCccd;
    ImageView imgAvatar, imgSmallAvatar,imgchangeAvatar;
    Button btnSaveChanges;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    private Uri muri;
    private MainActivity mainActivity;

    public void setMuri(Uri muri) {
        this.muri = muri;
    }

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

        mainActivity = (MainActivity) getActivity() ;


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

    private void setUserInfomation(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

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
        imgSmallAvatar.setOnClickListener(v -> {
            onClickRequestPermission();
        });

        // sk lưu thông tin
        btnSaveChanges.setOnClickListener(view -> {
            onClickUpdateProfile();

        });

    }

    private void onClickRequestPermission() {
        if (mainActivity == null) {
            return;
        }

        if(Build.VERSION.SDK_INT < Build.VERSION.CODES.M){
            mainActivity.openGallery();
            return ;
        }

        // hỏi người dùng mở quyền vào thư mục ảnh
        if(this.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mainActivity.openGallery();

        } else {
            String [] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, MY_REQUEST_CODE);
        }
    }

    // set bitmap
    public void setBitmapImageView(Bitmap bitmapImageView){
        imgSmallAvatar.setImageBitmap(bitmapImageView);
    }

    // update profile trên trang firebase
    public void onClickUpdateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String strFullName = edtFullName.getText().toString().trim();
        // lấy thông tin người dùng)

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(strFullName)
                .setPhotoUri(muri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            mainActivity.sho
                        }
                    }
                });
    }

}
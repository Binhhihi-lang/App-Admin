package com.example.admin_app.Activities.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.admin_app.Activities.Models.User;
import com.example.admin_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private TextView tvGoToSignUp ;
    private EditText etEmail, etPassword;
    private Button btnLogin;

    private TextView tvForgotPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        MaterialButton btnGoogle = findViewById(R.id.btn_google_signin);
//         //Tải icon Google glide
//        Glide.with(this)
//                .load("https://img.icons8.com/color/48/000000/google-logo.png")
//                .into(new CustomTarget<Drawable>() { // xử lý
//                    // khi tải xong
//                    @Override
//                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                        // Thiết kế kích thước icon (tùy chọn)
//                        resource.setBounds(0, 0, 60, 60);
//                        btnGoogle.setIcon(resource);
//                    }
//                    //
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                        btnGoogle.setIcon(null);
//                    }
//                });

        tvGoToSignUp = findViewById(R.id.tv_create_account);
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Ánh xạ UI
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Kiểm tra xem người dùng đã đăng nhập trước đó chưa
//        if (mAuth.getCurrentUser() != null) {
//            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//            finish();
//        }

        btnLogin.setOnClickListener(v-> loginUser());

        // Xử lý khi người dùng quên mật khẩu
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(v->{
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });


    }
    private void loginUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Firebase Auth để kiểm tra
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 1. Đăng nhập thành công
                        String uid = mAuth.getCurrentUser().getUid();

                        mDatabase.child("Users").child(uid).get().addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                User user = dbTask.getResult().getValue(User.class);

                                if (user != null && user.role == 1) {
                                    // Vào trang quản trị
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish(); // Chỉ đóng trang Login khi chuyển trang thành công
                                } else {
                                    // LÀ CLIENT -> Báo lỗi quyền hạn
                                    Toast.makeText(LoginActivity.this, "Bạn không có quyền truy cập Admin!", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    } else {
                        //Đăng nhập thất bại (Sai email, sai pass, hoặc mất mạng)
                        //  task.getException() không bị null
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại";
                        Toast.makeText(LoginActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
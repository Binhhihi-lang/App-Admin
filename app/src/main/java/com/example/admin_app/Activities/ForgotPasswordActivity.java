package com.example.admin_app.Activities;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.admin_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvCreateAccount;
    private FirebaseAuth mAuth;
    private EditText etEmail;
    private MaterialButton btnPasswordReset;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->{
            finish();
        });
        tvCreateAccount = findViewById(R.id.tv_create_account);
        tvCreateAccount.setOnClickListener(v ->{
            Intent intent = new Intent(ForgotPasswordActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        etEmail = findViewById(R.id.edtEmailForgot);
        btnPasswordReset = findViewById(R.id.btnReset);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        btnPasswordReset.setOnClickListener(v -> resetPassword());
    }

    // Hàm xử lý logic đặt lại mật khẩu
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        // Kiểm tra xem người dùng đã nhập email chưa
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem định dạng email có hợp lệ không
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Gọi Firebase gửi email reset
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Đã gửi link đặt lại mật khẩu! Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Email sent.");
                        finish();

                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

        });
    }

}
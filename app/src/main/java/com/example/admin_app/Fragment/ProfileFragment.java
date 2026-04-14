package com.example.admin_app.Fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.admin_app.Activities.AdminEditProfileActivity;
import com.example.admin_app.Activities.LoginActivity;
import com.example.admin_app.Models.User;
import com.example.admin_app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {
    TextView tvFullName,tvEmail,tvPhone;
    ImageView imgAvatar, imgSmallAvatar, imgUpdateView;
    MaterialToolbar toolbar ;
    MaterialButton btnLogout;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;
    private ValueEventListener userListener; // hủy lắng nghe khi thoát Fragment


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // sang trang update
        imgUpdateView = view.findViewById(R.id.imgEditProfile);
        toolbar = view.findViewById(R.id.toolbarInfoProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFullName = view.findViewById(R.id.tvNameDisplay);
        tvEmail = view.findViewById(R.id.tvEmailDisplay);
        tvPhone = view.findViewById(R.id.tvPhoneDisplay);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgSmallAvatar = view .findViewById(R.id.imgSmallAvatar);


        // sang trang chỉnh sửa thông tin
        imgUpdateView.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), AdminEditProfileActivity.class);
            startActivity(intent);

        });


        // trở về trang chủ
        toolbar.setNavigationOnClickListener(view1 ->{
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        });

        // Cấu hình Google Sign-In , requireContext thay cho .this
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        gClient = GoogleSignIn.getClient(requireContext(), gOptions);

        // Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Hiển thị thông tin người dùng
        loadUserData();
        return view;

    }

    private void showLogoutDialog() {
        // Dùng getActivity() hoặc requireContext() làm context cho Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        // Nút Đăng xuất
        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            performLogout();
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performLogout() {
        // Đăng xuất Firebase
        FirebaseAuth.getInstance().signOut();

        // Đăng xuất Google
        gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (isAdded()) { // Kiểm tra xem Fragment còn gắn với Activity không để tránh crash
                    Toast.makeText(getActivity(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    // Xóa sạch Stack để không quay lại được trang Admin
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Fragment gọi finish thông qua Activity
                    requireActivity().finish();
                }
            }
        });
    }

    public void loadUserData(){
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
                    tvFullName.setText(userProfile.getFullName());
                    tvEmail.setText(userProfile.getEmail());
                    // Kiểm tra và load ảnh bằng Glide
                    if (userProfile.getAvatar() == null && !userProfile.getAvatar().isEmpty()) {
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
                Toast.makeText(getActivity(), "Không thể tải dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
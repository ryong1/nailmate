package com.example.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MyPageFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView tvUserName;
    private TextView tvMyReview;
    private TextView tvSetProfile;
    private ImageView ivProfileImage;
    private static final int PROFILE_IMAGE_REQUEST_CODE = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvMyReview = view.findViewById(R.id.tvReview);
        ivProfileImage = view.findViewById(R.id.ivProfileImage); // 추가된 코드

        FirebaseUser user = mAuth.getCurrentUser();

        // 사용자 이름 표시 (이메일이나 이름 중에 하나를 선택)
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName + "님");
            } else {
                tvUserName.setText(user.getEmail() + "님");
            }

            // 프로필 이미지 표시
            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {
                Glide.with(requireContext())
                        .load(photoUri)
                        .into(ivProfileImage);
            }
        }

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                // 홈 화면으로 이동
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        });

        tvMyReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tvUserName에서 사용자 ID를 가져옴
                String userId = user != null ? user.getEmail() : "";

                // UserReviewsFragment로 이동하면서 사용자 ID를 전달
                UserReviewsFragment userReviewsFragment = new UserReviewsFragment(user.getDisplayName());
                requireFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, userReviewsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // 프로필 설정 텍스트뷰에 대한 클릭 이벤트 처리
        tvSetProfile = view.findViewById(R.id.tvSetProfile);
        tvSetProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 프로필 설정 화면으로 이동
                ProfileFragment profileFragment = new ProfileFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .commit();
            }
        });

        return view;
    }
}

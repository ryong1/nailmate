package com.example.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PROFILE_IMAGE_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private EditText etDisplayName;
    private ImageView ivProfileImage;
    private Uri selectedImageUri; // 이미지 URI를 저장할 변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etDisplayName = view.findViewById(R.id.etDisplayName);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);

        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileImagePicker();
            }
        });

        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        // 사용자의 프로필 정보 불러와서 화면에 표시
        loadUserProfile();

        return view;
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PROFILE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfileImage.setImageURI(selectedImageUri);
        }
    }

    private void saveUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = etDisplayName.getText().toString().trim();

            // 이미지가 선택되었는지 확인
            if (selectedImageUri != null) {
                // 이미지가 선택된 경우, Firebase Storage에 업로드 후 URL을 가져와서 사용자 프로필 업데이트
                uploadImageToFirebaseStorage(selectedImageUri);
            } else {
                // 이미지가 선택되지 않은 경우, 기존 이미지를 가져와서 사용자 프로필 업데이트
                Uri currentPhotoUri = user.getPhotoUrl();
                updateUserProfile(displayName, currentPhotoUri);
            }
        }
    }


    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // 이미지 업로드 경로를 정의 (예: "profile_images/사용자UID/이미지파일이름")
        String storagePath = "profile_images/" + mAuth.getCurrentUser().getUid() + "/" + imageUri.getLastPathSegment();

        // Firebase Storage에 이미지 업로드
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(storagePath);
        UploadTask uploadTask = storageReference.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // 업로드 성공
            getDownloadUrlFromFirebaseStorage(storageReference);
        }).addOnFailureListener(e -> {
            // 업로드 실패
            Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
        });
    }

    private void getDownloadUrlFromFirebaseStorage(StorageReference storageReference) {
        storageReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // 업로드된 이미지의 다운로드 URL을 가져와서 사용자 프로필 업데이트
                    updateUserProfile(etDisplayName.getText().toString().trim(), uri);
                })
                .addOnFailureListener(e -> {
                    // 다운로드 URL 가져오기 실패
                    Toast.makeText(requireContext(), "이미지 다운로드 URL 가져오기 실패", Toast.LENGTH_SHORT).show();
                });
    }
    private void updateUserProfile(String displayName, Uri photoUri) {
        FirebaseUser user = mAuth.getCurrentUser();

        // 이미지를 변경하지 않고 닉네임만 업데이트
        UserProfileChangeRequest profileUpdates;
        if (photoUri != null) {
            // 이미지가 변경된 경우
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(photoUri)
                    .build();
        } else {
            // 이미지가 변경되지 않은 경우
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
        }

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // 프로필 업데이트 성공
                        Toast.makeText(requireContext(), "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                        // 업데이트된 프로필을 다시 불러와서 표시
                        loadUserProfile();
                    } else {
                        // 프로필 업데이트 실패
                        Toast.makeText(requireContext(), "프로필 업데이트 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Display nickname
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                etDisplayName.setText(displayName);
            }

            // Display profile image using Glide
            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {
                Glide.with(requireContext()) // 수정: Glide.with(this) 대신 Glide.with(requireContext()) 사용
                        .load(photoUri)
                        .into(ivProfileImage);
            }
        }
    }


}

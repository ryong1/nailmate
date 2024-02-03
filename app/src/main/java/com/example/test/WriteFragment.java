package com.example.test;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteFragment extends Fragment {

    private DatabaseReference mDatabase;
    private static final int PICK_IMAGE_REQUEST = 4;
    private static final int MAX_IMAGE_COUNT = 4;

    private Button postButton;
    private Button searchButton;
    private String reviewIdToUpdate; // 수정할 리뷰의 ID

    private List<Uri> selectedImages = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private RatingBar ratingBar;
    private EditText reviewEditText;
    private CheckBox simpleCategoryCheckBox;
    private CheckBox graCategoryCheckBox;
    private CheckBox drawCategoryCheckBox;
    private CheckBox glamCategoryCheckBox;
    private CheckBox uniqueCategoryCheckBox;
    private CheckBox y2kCategoryCheckBox;
    private CheckBox themeCategoryCheckBox;
    private CheckBox romanticCategoryCheckBox;
    private CheckBox patternCategoryCheckBox;
    private TextView shopNameTextView;
    private TextView shopAddressTextView;
    private TextView characterCountTextView;
    private SeekBar priceSeekBar;
    private TextView priceTextView;
    private TextView Name;
    private ShopInfo selectedShopInfo;
    private int maxCharacterCount = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);

        reviewEditText = view.findViewById(R.id.reviewEditText);
        glamCategoryCheckBox = view.findViewById(R.id.glamCategoryButton);
        simpleCategoryCheckBox = view.findViewById(R.id.simpleCategoryButton);
        uniqueCategoryCheckBox = view.findViewById(R.id.uniqueCategoryButton);
        y2kCategoryCheckBox = view.findViewById(R.id.y2kCategoryButton);
        themeCategoryCheckBox = view.findViewById(R.id.themeCategoryButton);
        romanticCategoryCheckBox = view.findViewById(R.id.romanticCategoryButton);
        drawCategoryCheckBox = view.findViewById(R.id.drawCategoryButton);
        patternCategoryCheckBox = view.findViewById(R.id.patternCategoryButton);
        graCategoryCheckBox = view.findViewById(R.id.graCategoryButton);
        ratingBar = view.findViewById(R.id.ratingBar);
        shopNameTextView = view.findViewById(R.id.shopNameTextView);
        shopAddressTextView = view.findViewById(R.id.addressTextView);
        Name = view.findViewById(R.id.Name);
        characterCountTextView = view.findViewById(R.id.characterCountTextView);

        // 이미지뷰 초기화
        imageViews.add((ImageView) view.findViewById(R.id.attachedImageView1));
        imageViews.add((ImageView) view.findViewById(R.id.attachedImageView2));
        imageViews.add((ImageView) view.findViewById(R.id.attachedImageView3));
        imageViews.add((ImageView) view.findViewById(R.id.attachedImageView4));

        // EditText에 텍스트 변경 감지를 위한 TextWatcher 추가
        reviewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 텍스트 변경 전
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 텍스트 변경 중
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 텍스트 변경 후
                updateCharacterCount(editable.toString());
            }
        });

    Bundle args = getArguments();
        if (args != null) {
            selectedShopInfo = getArguments().getParcelable("selectedShopInfo");
            if (selectedShopInfo != null) {
                shopNameTextView.setText(selectedShopInfo.getBusinessName());
                shopAddressTextView.setText(selectedShopInfo.getRoadAddress());
            }
            reviewIdToUpdate = args.getString("reviewIdToUpdate");
            if (reviewIdToUpdate != null) {
                // 리뷰 수정 모드로 진입

                loadReviewDataForUpdate();
            }else {
            }

        }

        searchButton = view.findViewById(R.id.searchButton);
        Button photoAttachmentButton = view.findViewById(R.id.photoAttachmentButton);
        postButton = view.findViewById(R.id.postButton);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // SearchFragment로 이동하는 코드
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        for (int i = 0; i < imageViews.size(); i++) {
            final ImageView imageView = imageViews.get(i);
            imageView.setImageResource(0); // 이미지뷰 초기화
            imageView.setVisibility(View.GONE); // 이미지뷰 숨기기

            // X 표시를 이미지뷰 위에 추가
            ImageView deleteIcon = new ImageView(requireContext());
            deleteIcon.setImageResource(R.drawable.remove); // X 아이콘 이미지를 drawable 폴더에 추가
            deleteIcon.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            deleteIcon.setPadding(5, 5, 5, 5);
            // X 표시를 이미지뷰 안의 오른쪽 상단에 추가
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.gravity = Gravity.END | Gravity.TOP;
            deleteIcon.setLayoutParams(layoutParams);
            deleteIcon.setVisibility(View.GONE);
            // X 표시를 클릭하면 이미지 삭제
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 이미지뷰에서 이미지 삭제
                    imageView.setImageResource(0);
                    imageView.setVisibility(View.GONE);

                    // 선택된 이미지 리스트에서도 삭제
                    selectedImages.remove(imageView.getTag());

                    // X 아이콘 숨김
                    deleteIcon.setVisibility(View.GONE);
                }
            });

            // 이미지뷰를 클릭하면 X 아이콘을 보여줌
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteIcon.setVisibility(View.VISIBLE);
                }
            });

            // 이미지뷰의 부모로부터 이미지뷰 제거
            ViewGroup parentView = (ViewGroup) imageView.getParent();
            if (parentView != null) {
                parentView.removeView(imageView);
            }

            // X 표시를 이미지뷰와 함께 같은 FrameLayout에 추가
            FrameLayout frameLayout = new FrameLayout(requireContext());
            frameLayout.addView(imageView);
            frameLayout.addView(deleteIcon);

            // FrameLayout을 부모 뷰에 추가
            if (parentView != null) {
                parentView.addView(frameLayout);
            }
        }

        // 이미지 추가 버튼 클릭 이벤트 처리
        photoAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ExecutorService를 생성하고 백그라운드에서 이미지 선택 처리
                ExecutorService executorService = Executors.newCachedThreadPool();
                executorService.submit(new ImageSelectionTask());
            }
        });

        // WriteFragment에서 가게 정보를 처리하는 코드
        getParentFragmentManager().setFragmentResultListener("selectedShopInfoResult", this, (requestKey, result) -> {
            ShopInfo selectedShopInfo = result.getParcelable("selectedShopInfo");
            if (selectedShopInfo != null) {
                String selectedShopName = selectedShopInfo.getBusinessName();
                String selectedShopAddress = selectedShopInfo.getRoadAddress();

                // 가게 이름과 주소 정보를 화면에 표시
                shopNameTextView.setText(selectedShopName);
                shopAddressTextView.setText(selectedShopAddress);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reviewIdToUpdate != null) {
                    // 리뷰 수정 모드에서 버튼을 클릭했을 때 실행되는 코드
                    updateReview();
                } else {
                    // 리뷰 작성 모드에서 버튼을 클릭했을 때 실행되는 코드
                    saveNewReview();
                }
            }
        });

        priceSeekBar = view.findViewById(R.id.priceSeekBar);
        priceTextView = view.findViewById(R.id.priceTextView);

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int price = progress;
                priceTextView.setText(price + "만원");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return view;
    }
    private void updateCharacterCount(String text) {
        int currentCharacterCount = text.length();
        String countText = "(" + currentCharacterCount + "/" + maxCharacterCount + ")";
        characterCountTextView.setText(countText);
    }
    private void loadReviewDataForUpdate() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews").child(reviewIdToUpdate);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 리뷰 내용을 불러와서 화면에 표시
                    String storeName = dataSnapshot.child("storeName").getValue(String.class);
                    String storeAddress = dataSnapshot.child("storeAddress").getValue(String.class);
                    String review = dataSnapshot.child("review").getValue(String.class);
                    float rating = dataSnapshot.child("rating").getValue(Float.class);
                    int price = dataSnapshot.child("price").getValue(Integer.class); // 가격 데이터 가져오기
                    String existingImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // 화면에 표시
                    shopNameTextView.setText(storeName);
                    shopAddressTextView.setText(storeAddress);
                    reviewEditText.setText(review);
                    ratingBar.setRating(rating);
                    priceSeekBar.setProgress(price);
                    priceTextView.setText(price + "만원");
                    Name.setText("후기 수정");

                    // 체크박스 상태 설정
                    glamCategoryCheckBox.setChecked(dataSnapshot.child("glamCategory").getValue(Boolean.class));
                    simpleCategoryCheckBox.setChecked(dataSnapshot.child("simpleCategory").getValue(Boolean.class));
                    uniqueCategoryCheckBox.setChecked(dataSnapshot.child("uniqueCategory").getValue(Boolean.class));
                    y2kCategoryCheckBox.setChecked(dataSnapshot.child("y2kCategory").getValue(Boolean.class));
                    themeCategoryCheckBox.setChecked(dataSnapshot.child("themeCategory").getValue(Boolean.class));
                    romanticCategoryCheckBox.setChecked(dataSnapshot.child("romanticCategory").getValue(Boolean.class));
                    drawCategoryCheckBox.setChecked(dataSnapshot.child("drawCategory").getValue(Boolean.class));
                    patternCategoryCheckBox.setChecked(dataSnapshot.child("patternCategory").getValue(Boolean.class));
                    graCategoryCheckBox.setChecked(dataSnapshot.child("graCategory").getValue(Boolean.class));

                    //샵 이름 수정 불가
                    searchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 수정 모드에서는 샵 이름을 수정할 수 없으므로 경고 메시지를 표시합니다.
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setMessage("샵은 수정이 불가능합니다.")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // 사용자가 확인 버튼을 클릭하면 다이얼로그를 닫습니다.
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                    // SearchFragment로 이동하지 못하도록 막기
                    searchButton.setClickable(true);
                    searchButton.setFocusable(false);
                    searchButton.setFocusableInTouchMode(false);

                    // 이미지 URL을 Firebase Storage에서 가져와서 이미지뷰에 표시 (위에서 이미지 로딩 코드 추가 필요)
                    if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                        // 이미지뷰를 대상으로 이미지를 로드
                        loadImageFromFirebaseStorage(reviewIdToUpdate, imageViews.get(0), "image1.jpg");
                        // 여러 이미지가 있다면 추가로 로드하도록 반복
                        loadImageFromFirebaseStorage(reviewIdToUpdate, imageViews.get(1), "image2.jpg");
                        loadImageFromFirebaseStorage(reviewIdToUpdate, imageViews.get(2), "image3.jpg");
                        loadImageFromFirebaseStorage(reviewIdToUpdate, imageViews.get(3), "image4.jpg");
                    }
                    // 수정 모드에서는 postButton의 텍스트를 '수정하기'로 변경
                    postButton.setText("수정하기");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 불러오기 실패 처리
            }
        });
    }
    private class ImageSelectionTask implements Runnable {
        @Override
        public void run() {
            // 이미 선택된 이미지 개수 계산
            int selectedImageCount = selectedImages.size();
            int remainingImageCount = MAX_IMAGE_COUNT - selectedImageCount;

            if (selectedImageCount >= MAX_IMAGE_COUNT) {
                // 이미지가 4장일 경우
                getActivity().runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("최대 4장까지 선택할 수 있습니다. 다시 선택하시겠습니까?");
                    builder.setPositiveButton("다시 선택", (dialog, which) -> {
                        // 기존 이미지 모두 삭제
                        selectedImages.clear();

                        // 갤러리에서 이미지를 선택할 수 있는 인텐트 생성
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        if (remainingImageCount > 1) {
                            // 여러 이미지 선택을 허용
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }

                        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE_REQUEST);
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                });
            } else {
                // 이미지가 4장 미만인 경우, 갤러리에서 이미지를 선택할 수 있는 인텐트 생성
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                if (remainingImageCount > 1) {
                    // 여러 이미지 선택을 허용
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }

                startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE_REQUEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    // 갤러리에서 여러 개의 이미지를 선택한 경우
                    int selectedImageCount = clipData.getItemCount();

                    for (int i = 0; i < selectedImageCount && i < MAX_IMAGE_COUNT; i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();

                        // 이미지 URI를 리스트에 추가
                        selectedImages.add(imageUri);
                    }
                } else {
                    // 갤러리에서 하나의 이미지만 선택한 경우
                    Uri imageUri = data.getData();
                    selectedImages.add(imageUri);
                }

                // UI 업데이트를 UI 스레드에서 수행
                getActivity().runOnUiThread(() -> {
                    // 이미지뷰에 이미지 설정 및 순차적으로 표시
                    for (int i = 0; i < selectedImages.size() && i < imageViews.size(); i++) {
                        Uri imageUri = selectedImages.get(i);
                        ImageView imageView = imageViews.get(i);

                        Glide.with(requireContext())
                                .load(imageUri)
                                .apply(new RequestOptions().centerCrop())
                                .into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
                // 이미지 로딩이 완료된 후에 Fragment를 닫도록 수정하면 메모리 누수 가능성이 줄어듭니다.
            }
        }
    }

    private void saveImageToFirebaseStorage(String reviewId, ImageView imageView, String imageName) {
        if (imageView.getDrawable() != null) {
            final Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler mainHandler = new Handler(Looper.getMainLooper()); // Create a Handler for the main thread

            executor.execute(() -> {
                try {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + reviewId + "/" + imageName);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    // Firebase Storage에 이미지 업로드
                    UploadTask uploadTask = storageRef.putBytes(imageData);
                    Tasks.await(uploadTask); // 업로드 완료를 기다림

                    // 이미지 업로드 후 URL 가져오기
                    Task<Uri> urlTask = storageRef.getDownloadUrl();
                    Uri downloadUrl = Tasks.await(urlTask);
                    String imageUrl = downloadUrl.toString();

                    mainHandler.post(() -> {
                        // UI-related code
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews");
                        databaseReference.child(reviewId).child("imageUrl").setValue(imageUrl);

                        // Handle UI updates and other tasks here
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> {
                        // Handle exceptions and UI updates here
                        Toast.makeText(getActivity(), "이미지 업로드 또는 URL 가져오기 실패", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // 새 리뷰 저장
    private void saveNewReview() {
        // 리뷰 정보를 데이터베이스에 저장하는 코드
        String storeName = shopNameTextView.getText().toString();
        String storeAddress = shopAddressTextView.getText().toString();
        String review = reviewEditText.getText().toString();
        float rating = ratingBar.getRating();
        boolean glamCategory = glamCategoryCheckBox.isChecked();
        boolean simpleCategory = simpleCategoryCheckBox.isChecked();
        boolean uniqueCategory = uniqueCategoryCheckBox.isChecked();
        boolean y2kCategory = y2kCategoryCheckBox.isChecked();
        boolean themeCategory = themeCategoryCheckBox.isChecked();
        boolean romanticCategory = romanticCategoryCheckBox.isChecked();
        boolean drawCategory = drawCategoryCheckBox.isChecked();
        boolean patternCategory = patternCategoryCheckBox.isChecked();
        boolean graCategory = graCategoryCheckBox.isChecked();
        int price = priceSeekBar.getProgress();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews");
        String reviewId = databaseReference.push().getKey();

        // 리뷰 객체 생성
        String userId = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String date = getCurrentDateTime();
        Review newReview = new Review(reviewId, userId, storeName,storeAddress, rating, review, glamCategory, simpleCategory, uniqueCategory, y2kCategory, themeCategory, romanticCategory, drawCategory, patternCategory, graCategory, price, date);

        // 이미지 URL을 저장
        newReview.setImageUrl(""); // 초기화, 이미지 업로드 후 URL을 설정합니다.

        // 데이터 저장
        databaseReference.child(reviewId).setValue(newReview);

        // Firebase Storage에 이미지 저장 (이미지를 저장하려면 Firebase Storage 설정이 필요)
        for (int i = 0; i < selectedImages.size(); i++) {
            saveImageToFirebaseStorage(reviewId, imageViews.get(i), "image" + (i + 1) + ".jpg");
        }

        // 데이터 완료 후 처리
        Toast.makeText(getActivity(), "리뷰가 저장되었습니다!", Toast.LENGTH_SHORT).show();

        // homeFragment로 이동하면서 reviewId를 전달
        Bundle bundle = new Bundle();
        bundle.putString("reviewId", reviewId); // reviewId를 전달

        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment) // 수정된 부분
                .addToBackStack(null)
                .commit();
    }

    // 리뷰 업데이트
    private void updateReview() {
        String reviewId = reviewIdToUpdate; // 수정할 리뷰의 ID

        if (reviewId != null) {
            // 리뷰 정보를 데이터베이스에 업데이트
            String storeName = shopNameTextView.getText().toString();
            String storeAddress= shopAddressTextView.getText().toString();
            String review = reviewEditText.getText().toString();
            float rating = ratingBar.getRating();
            boolean glamCategory = glamCategoryCheckBox.isChecked();
            boolean simpleCategory = simpleCategoryCheckBox.isChecked();
            boolean uniqueCategory = uniqueCategoryCheckBox.isChecked();
            boolean y2kCategory = y2kCategoryCheckBox.isChecked();
            boolean themeCategory = themeCategoryCheckBox.isChecked();
            boolean romanticCategory = romanticCategoryCheckBox.isChecked();
            boolean drawCategory = drawCategoryCheckBox.isChecked();
            boolean patternCategory = patternCategoryCheckBox.isChecked();
            boolean graCategory = graCategoryCheckBox.isChecked();
            int price = priceSeekBar.getProgress();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews").child(reviewId);

            // 리뷰 객체 생성
            String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String date = getCurrentDateTime() + "(수정됨)";
            Review updatedReview = new Review(reviewId, userId, storeName,storeAddress, rating, review, glamCategory, simpleCategory, uniqueCategory, y2kCategory, themeCategory, romanticCategory, drawCategory, patternCategory, graCategory, price, date);

            // 이미지 URL은 업데이트하지 않고 이전 URL을 그대로 유지
            String existingImageUrl = ""; // 여기에 이전 이미지 URL을 가져와서 설정

            if (isImageModified()) {
                // 이미지가 수정된 경우에만 이미지 URL 업데이트
                updatedReview.setImageUrl(existingImageUrl);

                // Firebase Storage에 이미지 업데이트 (기존 이미지 삭제 후 새 이미지 업로드 필요)
                for (int i = 0; i < selectedImages.size(); i++) {
                    String imageName = "image" + (i + 1) + ".jpg";
                    
                    // 새 이미지 업로드
                    saveImageToFirebaseStorage(reviewId, imageViews.get(i), imageName);
                }
            } else {
                // 이미지가 수정되지 않은 경우, 기존 이미지 URL을 그대로 유지
                updatedReview.setImageUrl(existingImageUrl);
            }

            // 데이터 업데이트
            databaseReference.setValue(updatedReview);

            // 데이터 업데이트 완료 후 처리
            Toast.makeText(getActivity(), "리뷰가 업데이트되었습니다!", Toast.LENGTH_SHORT).show();

            // ViewFragment로 이동
            Bundle bundle = new Bundle();
            bundle.putString("reviewId", reviewId); // reviewId를 전달

            ViewFragment viewFragment = new ViewFragment();
            viewFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, viewFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // 이미지가 수정되었는지 확인하는 함수
    private boolean isImageModified() {
        for (int i = 0; i < selectedImages.size(); i++) {
            ImageView imageView = imageViews.get(i);
            Uri currentImageUri = selectedImages.get(i);

            if (imageView.getDrawable() != null) {
                // 이미지뷰에 새 이미지가 설정된 경우, 이미지가 수정된 것으로 판단
                return true;
            }
        }
        // 이미지가 수정되지 않은 경우
        return false;
    }


    // Firebase Storage에서 이미지 삭제
    private void deleteImageFromFirebaseStorage(String reviewId, String imageName) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + reviewId + "/" + imageName);
        storageRef.delete().addOnSuccessListener(aVoid -> {
            // 이미지 삭제 성공
        }).addOnFailureListener(e -> {
            // 이미지 삭제 실패
            Toast.makeText(getActivity(), "이미지 삭제 실패", Toast.LENGTH_SHORT).show();
        });
    }

    //수정하기 -> 이미지뷰에 이미지 표시
    private void loadImageFromFirebaseStorage(String reviewId, ImageView imageView, String imageName) {
        // Firebase Storage에서 이미지 다운로드 URL 가져오기
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + reviewId + "/" + imageName);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            // 이미지 다운로드 URL을 사용하여 이미지를 로드하고 이미지뷰에 설정
            Glide.with(requireContext())
                    .load(imageUrl) // Glide나 Picasso 등 이미지 로딩 라이브러리를 사용하여 로드할 수 있습니다.
                    .into(imageView);
        }).addOnFailureListener(exception -> {
            // 이미지 다운로드 실패 처리
            Toast.makeText(getActivity(), "이미지 다운로드 실패", Toast.LENGTH_SHORT).show();
        });
    }
    private String getCurrentDateTime() {
        // 현재 시간을 가져와서 원하는 포맷의 문자열로 변환하는 코드
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}

package com.example.test;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewFragment extends Fragment {

    private TextView shopNameTextView;
    private RatingBar ratingBar;
    private TextView authorTextView;
    private TextView priceTextView;
    private TextView reviewContentTextView;
    private ViewPager imageViewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private String reviewId; // 리뷰 ID
    private String imageFolderPath; // 이미지 폴더 경로
    private TextView etcBtn;

    private TextView categoryTextView1;
    private TextView categoryTextView2;
    private TextView categoryTextView3;
    private TextView categoryTextView4;
    private TextView categoryTextView5;
    private TextView categoryTextView6;
    private TextView categoryTextView7;
    private TextView categoryTextView8;
    private TextView categoryTextView9;
    private TextView dateTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        // 뷰 요소 초기화
        shopNameTextView = view.findViewById(R.id.shopNameTextView);
        ratingBar = view.findViewById(R.id.ratingBar);
        authorTextView = view.findViewById(R.id.authorTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        reviewContentTextView = view.findViewById(R.id.reviewContentTextView);
        imageViewPager = view.findViewById(R.id.imageViewPager);
        etcBtn = view.findViewById(R.id.etcBtn);
        dateTextView = view.findViewById(R.id.dateTextView);

        // 카테고리 TextView 초기화
        categoryTextView1 = view.findViewById(R.id.categoryTextView1);
        categoryTextView2 = view.findViewById(R.id.categoryTextView2);
        categoryTextView3 = view.findViewById(R.id.categoryTextView3);
        categoryTextView4 = view.findViewById(R.id.categoryTextView4);
        categoryTextView5 = view.findViewById(R.id.categoryTextView5);
        categoryTextView6 = view.findViewById(R.id.categoryTextView6);
        categoryTextView7 = view.findViewById(R.id.categoryTextView7);
        categoryTextView8 = view.findViewById(R.id.categoryTextView8);
        categoryTextView9 = view.findViewById(R.id.categoryTextView9);

        etcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업 메뉴를 표시하는 코드
                PopupMenu popupMenu = new PopupMenu(requireContext(), etcBtn); // 또는 getActivity()를 사용할 수 있음
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // 팝업 메뉴 아이템 클릭 리스너 설정
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_edit:
                                openWriteFragmentForEdit();
                                return true;
                            case R.id.menu_delete:
                                showDeleteConfirmationDialog();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        // ViewFragment로 전달된 리뷰 ID와 이미지 폴더 경로를 가져옵니다.
        Bundle args = getArguments();
        if (args != null) {
            reviewId = args.getString("reviewId");
            imageFolderPath = args.getString("imageFolderPath");
        }

        // Firebase에서 리뷰 데이터를 가져와서 뷰에 표시합니다.
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews");
        databaseReference.child(reviewId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String shopName = dataSnapshot.child("storeName").getValue(String.class);
                    float rating = dataSnapshot.child("rating").getValue(Float.class);
                    int price = dataSnapshot.child("price").getValue(Integer.class);
                    String reviewContent = dataSnapshot.child("review").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);

                    // 데이터를 뷰에 설정합니다.
                    shopNameTextView.setText(shopName);
                    ratingBar.setRating(rating);
                    ratingBar.setIsIndicator(true); // 사용자 상호작용 비활성화
                    dateTextView.setText(date);

                    // 리뷰 객체에서 작성자 ID를 가져오기
                    String authorId = dataSnapshot.child("userId").getValue(String.class);

                    // 사용자 정보를 가져와서 표시
                    if (authorId != null) {
                        // 리뷰 객체에서 이미 작성자 ID가 있으므로 추가적인 데이터베이스 쿼리 없이 바로 사용자 ID를 표시
                        authorTextView.setText(authorId);
                    }

                    priceTextView.setText(getString(R.string.price_format, price));
                    reviewContentTextView.setText(reviewContent);

                    // etcBtn를 설정합니다.
                    setEtcButtonVisibility(authorId);

                    // 카테고리 데이터를 가져와서 각각의 TextView에 설정
                    setCategoryText(dataSnapshot);

                    // 이미지 표시를 위해 ImagePagerAdapter를 초기화하고 ViewPager에 연결합니다.
                    getImageUrlsFromStorage(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 리뷰 데이터 가져오기 실패 처리
            }
        });
        return view;
    }
    //수정버튼 클릭할 경우
    private void openWriteFragmentForEdit() {
        WriteFragment writeFragment = new WriteFragment();

        // 기존 리뷰의 리뷰 ID를 WriteFragment로 전달
        Bundle args = new Bundle();
        args.putString("reviewIdToUpdate", reviewId);
        writeFragment.setArguments(args);

        // WriteFragment로 이동
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, writeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 삭제 확인 팝업을 표시하는 메서드
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("삭제 확인")
                .setMessage("게시글을 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Firebase에서 게시글 및 이미지 삭제
                        deleteReviewData();

                        // 사용자에게 알림을 표시할 수도 있습니다.
                        Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        // HomeFragment로 이동
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new HomeFragment()); // 여기서 R.id.fragment_container는 HomeFragment가 표시될 레이아웃 컨테이너의 ID입니다.
                        transaction.addToBackStack(null); // 백 스택에 추가하여 뒤로 가기 동작 지원
                        transaction.commit();
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 아무 작업 없음
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Firebase에서 게시글 및 이미지를 삭제하는 메서드
    private void deleteReviewData() {
        // Firebase에서 게시글 삭제
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews");
        databaseReference.child(reviewId).removeValue();

        // Firebase Storage에서 이미지 폴더 삭제
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(reviewId); // 리뷰 ID를 폴더 경로로 사용

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            int totalItems = items.size();
            final int[] deletedItems = {0};

            for (StorageReference item : items) {
                item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 이미지 삭제 성공
                        deletedItems[0]++;

                        // 모든 이미지가 삭제되었을 때 처리
                        if (deletedItems[0] == totalItems) {
                            // 여기에서 필요한 추가 작업을 수행할 수 있습니다.
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 이미지 삭제 실패
                        e.printStackTrace();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 이미지 리스트 가져오기 실패 처리
                e.printStackTrace();
            }
        });
    }

    // Firebase Storage에서 이미지 URL 리스트를 가져오는 메서드
    private void getImageUrlsFromStorage(DataSnapshot dataSnapshot) {
        List<String> imageUrls = new ArrayList<>();
        try {
            String imageFolderPath = dataSnapshot.getKey(); // 리뷰 ID를 폴더 경로로 사용

            // 이미지 폴더 경로를 사용하여 스토리지에서 해당 폴더 아래의 이미지 URL을 가져옴
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("images")
                    .child(imageFolderPath);

            // 해당 폴더 아래의 모든 이미지를 나열하고 각 이미지의 URL을 가져와 리스트에 추가
            storageRef.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        imageUrls.add(imageUrl);

                        // 모든 이미지를 가져왔을 때 ViewPager를 설정
                        if (imageUrls.size() == listResult.getItems().size()) {
                            // 이미지를 ViewPager에 표시하기 위해 ImagePagerAdapter를 초기화하고 ViewPager에 연결합니다.
                            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(requireContext(), imageUrls);
                            imageViewPager.setAdapter(imagePagerAdapter);
                        }
                    }).addOnFailureListener(e -> {
                        // 이미지 URL 가져오기 실패 처리
                        e.printStackTrace();
                    });
                }
            }).addOnFailureListener(e -> {
                // 이미지 리스트 가져오기 실패 처리
                e.printStackTrace();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Firebase에서 리뷰의 카테고리 정보를 가져와 TextView에 설정합니다.
    private void setCategoryText(DataSnapshot dataSnapshot) {
        String[] categories = {
                dataSnapshot.child("glamCategory").getValue(Boolean.class) ? "화려" : "",
                dataSnapshot.child("simpleCategory").getValue(Boolean.class) ? "심플" : "",
                dataSnapshot.child("uniqueCategory").getValue(Boolean.class) ? "유니크" : "",
                dataSnapshot.child("graCategory").getValue(Boolean.class) ? "그라데이션" : "",
                dataSnapshot.child("drawCategory").getValue(Boolean.class) ? "드로우" : "",
                dataSnapshot.child("y2kCategory").getValue(Boolean.class) ? "y2k" : "",
                dataSnapshot.child("romanticCategory").getValue(Boolean.class) ? "로맨틱" : "",
                dataSnapshot.child("themeCategory").getValue(Boolean.class) ? "테마" : "",
                dataSnapshot.child("patternCategory").getValue(Boolean.class) ? "패턴" : ""
        };

        // 각 카테고리 TextView에 데이터 설정
        categoryTextView1.setText(categories[0]);
        categoryTextView2.setText(categories[1]);
        categoryTextView3.setText(categories[2]);
        categoryTextView4.setText(categories[3]);
        categoryTextView5.setText(categories[4]);
        categoryTextView6.setText(categories[5]);
        categoryTextView7.setText(categories[6]);
        categoryTextView8.setText(categories[7]);
        categoryTextView9.setText(categories[8]);

        // 빈 텍스트뷰 숨기기
        hideEmptyTextViews(categories);
    }

    // 빈 텍스트뷰 숨기는 메서드
    private void hideEmptyTextViews(String[] categories) {
        List<TextView> categoryTextViews = new ArrayList<>();
        categoryTextViews.add(categoryTextView1);
        categoryTextViews.add(categoryTextView2);
        categoryTextViews.add(categoryTextView3);
        categoryTextViews.add(categoryTextView4);
        categoryTextViews.add(categoryTextView5);
        categoryTextViews.add(categoryTextView6);
        categoryTextViews.add(categoryTextView7);
        categoryTextViews.add(categoryTextView8);
        categoryTextViews.add(categoryTextView9);

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].isEmpty()) {
                categoryTextViews.get(i).setVisibility(View.GONE);
            }
        }
    }

    // etcBtn의 가시성을 설정하는 메서드
    private void setEtcButtonVisibility(String authorId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && authorId != null && currentUser.getDisplayName().equals(authorId)) {
            // 현재 사용자가 게시글 작성자인 경우
            etcBtn.setVisibility(View.VISIBLE);
        } else {
            // 현재 사용자가 게시글 작성자가 아닌 경우
            etcBtn.setVisibility(View.GONE);
        }
    }
}


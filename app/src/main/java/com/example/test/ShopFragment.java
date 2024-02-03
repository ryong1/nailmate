package com.example.test;

import static android.view.LayoutInflater.from;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShopFragment extends Fragment {
    private TextView shopNameTextView;
    private TextView shopAddressTextView;
    private TextView shopGradeTextView;
    private ImageView naverIcon;
    private ImageView kakaoIcon;
    private Button WriteButton;
    private RecyclerView recyclerView;
    private ShopInfo selectedShopInfo;
    private double averageRating = 0.0;
    private int reviewCount = 0;

    private FirebaseRecyclerAdapter<Review, ReviewViewHolder> adapter;
    private Spinner sortSpinner;
    private String selectedSortOption = "최신순"; // 기본 정렬 옵션
    private List<Review> reviewsList = new ArrayList<>(); // 리뷰 목록 초기화

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        shopNameTextView = view.findViewById(R.id.ShopName);
        shopAddressTextView = view.findViewById(R.id.ShopAddress);
        shopGradeTextView = view.findViewById(R.id.ShopGrade);
        naverIcon = view.findViewById(R.id.NaverImg);
        kakaoIcon = view.findViewById(R.id.KaKaoImg);
        recyclerView = view.findViewById(R.id.recyclerView);
        WriteButton = view.findViewById(R.id.WriteButton);
        sortSpinner = view.findViewById(R.id.sortSpinner);

        WriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteFragment writeFragment = new WriteFragment();
                Bundle args = new Bundle();
                args.putParcelable("selectedShopInfo", selectedShopInfo);
                writeFragment.setArguments(args);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, writeFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            selectedShopInfo = args.getParcelable("selectedShopInfo");
            if (selectedShopInfo != null) {
                shopNameTextView.setText(selectedShopInfo.getBusinessName());
                shopAddressTextView.setText(selectedShopInfo.getRoadAddress());
            }
        }

        naverIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shopName = shopNameTextView.getText().toString();
                String naverMapUrl = "https://map.naver.com/v5/search/" + shopName;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(naverMapUrl));
                startActivity(intent);
            }
        });

        kakaoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shopName = shopNameTextView.getText().toString();
                String kakaoMapUrl = "https://place.map.kakao.com/?q=" + shopName;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kakaoMapUrl));
                startActivity(intent);
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedSortOption = sortSpinner.getSelectedItem().toString();
                sortReviews(selectedSortOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        fetchAndSortReviews(selectedShopInfo.getRoadAddress());

        return view;
    }

    private void fetchAndSortReviews(final String shopAddress) {
        reviewsList.clear();

        Query query = FirebaseDatabase.getInstance().getReference().child("reviews").orderByChild("storeAddress").equalTo(shopAddress);
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalRating = 0.0;
                int totalReviews = 0; // 총 리뷰 수를 추적

                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        reviewsList.add(review);
                        double rating = review.getRating();
                        totalRating += rating;
                        totalReviews++;
                    }
                }

                if (totalReviews > 0) { // 적어도 하나의 리뷰가 있는 경우에만 평점 표시
                    averageRating = totalRating / totalReviews;
                } else {
                    averageRating = 0.0;
                }

                reviewCount = totalReviews;
                shopGradeTextView.setText(String.format("%.1f (%d)", averageRating, reviewCount));

                sortReviews(selectedSortOption);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        adapter = new FirebaseRecyclerAdapter<Review, ReviewViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ReviewViewHolder holder, int position, Review model) {
                holder.bind(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int itemPosition = holder.getAdapterPosition();

                        if (itemPosition != RecyclerView.NO_POSITION) {
                            String reviewId = getRef(itemPosition).getKey();

                            Bundle bundle = new Bundle();
                            bundle.putString("reviewId", reviewId);

                            ViewFragment viewFragment = new ViewFragment();
                            viewFragment.setArguments(bundle);

                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, viewFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }
                });
            }

            @Override
            public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
                return new ReviewViewHolder(view, parent.getContext());
            }
        };

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        recyclerView.setAdapter(adapter);
    }

    private void sortReviews(String sortOption) {
        switch (sortOption) {
            case "최신순":
                Collections.sort(reviewsList, (review1, review2) -> review2.getDate().compareTo(review1.getDate()));
                break;
            case "평점 높은 순":
                Collections.sort(reviewsList, (review1, review2) -> Float.compare(review2.getRating(), review1.getRating()));
                break;
            case "평점 낮은 순":
                Collections.sort(reviewsList, (review1, review2) -> Float.compare(review1.getRating(), review2.getRating()));
                break;
        }

        recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}

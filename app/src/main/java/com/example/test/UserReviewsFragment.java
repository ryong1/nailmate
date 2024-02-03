package com.example.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class UserReviewsFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Review> userReviewsList = new ArrayList<>();
    private FirebaseRecyclerAdapter<Review, ReviewViewHolder> adapter;
    private String userId;

    public UserReviewsFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_reviews, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        // 여기에서 userId를 사용하여 사용자 리뷰 데이터를 가져오고 어댑터에 설정하는 로직을 추가합니다.
        fetchUserReviews(userId);
        return view;
    }
    private void fetchUserReviews(String userId) {
        userReviewsList.clear();

        // 사용자 ID를 기반으로 리뷰 데이터를 가져오는 Firebase Database 쿼리를 작성
        Query query = FirebaseDatabase.getInstance().getReference().child("reviews").orderByChild("userId").equalTo(userId);

        // FirebaseRecyclerOptions를 사용하여 리뷰를 가져옵니다.
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        // FirebaseRecyclerAdapter를 설정하여 리뷰 데이터를 리사이클러뷰에 표시
        adapter = new FirebaseRecyclerAdapter<Review, ReviewViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull Review model) {
                // 리뷰 데이터를 홀더에 바인딩하고 클릭 이벤트를 추가합니다.
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

            @NonNull
            @Override
            public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // 리뷰 아이템 뷰를 생성하고 홀더를 반환
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
                return new ReviewViewHolder(view, parent.getContext());
            }
        };

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        recyclerView.setAdapter(adapter);
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

    // 사용자 리뷰 정렬과 추가 설정을 구현

    // 나머지 메서드와 설정

}


package com.example.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<Review, ReviewViewHolder> adapter;
    private EditText SearchBar;
    private ImageView mapButton;
    private TextView ArtStyle;
    private TextView Array;


    // 선택한 카테고리를 저장하는 리스트
    private List<String> selectedCategories = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase 데이터베이스에서 리뷰 데이터를 가져올 데이터베이스 레퍼런스 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");

        // 리뷰 데이터를 쿼리할 때 정렬 순서 설정
        Query query = databaseReference.orderByChild("date");

        // FirebaseRecyclerOptions를 사용하여 리뷰 데이터 가져오기
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        // 리뷰 어댑터 초기화
        adapter = new FirebaseRecyclerAdapter<Review, ReviewViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReviewViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Review model) {
                // 리뷰 데이터를 뷰 홀더에 바인딩
                holder.bind(model);

                // 아이템 뷰 클릭 시 리뷰 ID를 ViewFragment로 전달하고 ViewFragment로 이동
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String reviewId = getRef(position).getKey(); // 리뷰의 키 (ID) 가져오기

                        // 리뷰 ID를 ViewFragment로 전달하고 ViewFragment로 이동
                        Bundle bundle = new Bundle();
                        bundle.putString("reviewId", reviewId);

                        ViewFragment viewFragment = new ViewFragment();
                        viewFragment.setArguments(bundle);

                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, viewFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            }
            @NonNull
            @Override
            public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // 리뷰 아이템의 뷰 홀더를 생성하고 반환
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
                return new ReviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false), parent.getContext());
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // 3개의 열로 그리드 레이아웃 설정
        recyclerView.setAdapter(adapter);

        SearchBar = view.findViewById(R.id.searchBar);

        SearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText를 클릭하면 SearchFragment로 이동
                SearchFragment searchFragment = new SearchFragment();
                Bundle args = new Bundle();
                args.putBoolean("comingFromHomeFragment", true); // 이 값을 true로 설정
                searchFragment.setArguments(args);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, searchFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        mapButton = view.findViewById(R.id.mapIcon);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of SupportMapFragment
                FindFragment findFragment = new FindFragment();

                // Replace the current fragment with the SupportMapFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, findFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        ArtStyle = view.findViewById(R.id.spinnerArtStyle);
        ArtStyle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                ArtStyle.setTextColor(R.color.color4);
                // 바텀 시트를 표시
                BottomSheetFragment bottomSheet = new BottomSheetFragment();
                bottomSheet.setHomeFragment(HomeFragment.this);
                bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
            }
        });

        Array= view.findViewById(R.id.spinnerArray);
        Array.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                Array.setTextColor(R.color.color4);
                // 바텀 시트를 표시
                BottomArrayFragment bottomArray = new BottomArrayFragment();
                bottomArray.setHomeFragment(HomeFragment.this);
                bottomArray.show(getParentFragmentManager(), bottomArray.getTag());
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening(); // FirebaseRecyclerAdapter 리스너 시작
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening(); // FirebaseRecyclerAdapter 리스너 종료
    }

    // 카테고리 선택 메서드
    public void selectCategories(List<String> categories) {
        selectedCategories.clear(); // Clear the current selected categories
        selectedCategories.addAll(categories); // Add the new selected categories
        filterReviews();
    }
    public void selectCategory(String category) {
        selectedCategories.clear(); // 기존에 선택된 카테고리를 모두 제거
        selectedCategories.add(category); // 새로운 카테고리를 추가
        updateReviewsWithCategory(category); // 리뷰 필터링 및 업데이트
    }

    // 리뷰 필터링 및 업데이트 메서드
    private void filterReviews() {
        Query query;

        // 만약 선택한 카테고리가 없다면 모든 리뷰를 가져옵니다.
        if (selectedCategories == null ||selectedCategories.isEmpty()) {
            query = databaseReference.child("reviews").orderByChild("date");
        } else {
            // 선택한 카테고리가 있는 경우, 해당 카테고리에 속한 리뷰만 가져오도록 쿼리를 조합합니다.
            List<Query> categoryQueries = new ArrayList<>();

            for (String category : selectedCategories) {
                switch (category) {
                    case "glamTextView":
                        categoryQueries.add(databaseReference.orderByChild("glamCategory").equalTo(true));
                        break;
                    case "simpleTextView":
                        categoryQueries.add(databaseReference.orderByChild("simpleCategory").equalTo(true));
                        break;
                    case "uniqueTextView":
                        categoryQueries.add(databaseReference.orderByChild("uniqueCategory").equalTo(true));
                        break;
                    case "y2kTextView":
                        categoryQueries.add(databaseReference.orderByChild("y2kCategory").equalTo(true));
                        break;
                    case "themeTextView":
                        categoryQueries.add(databaseReference.orderByChild("themeCategory").equalTo(true));
                        break;
                    case "romanticTextView":
                        categoryQueries.add(databaseReference.orderByChild("romanticCategory").equalTo(true));
                        break;
                    case "drawTextView":
                        categoryQueries.add(databaseReference.orderByChild("drawCategory").equalTo(true));
                        break;
                    case "patternTextView":
                        categoryQueries.add(databaseReference.orderByChild("patternCategory").equalTo(true));
                        break;
                    case "graTextView":
                        categoryQueries.add(databaseReference.orderByChild("graCategory").equalTo(true));
                        break;
                    default:
                        // 처리할 카테고리가 없는 경우, 아무 작업도 수행하지 않음.
                        break;
                }
            }
            // 선택한 카테고리에 속한 리뷰를 가져오도록 OR 조건으로 쿼리를 조합합니다.
            query = combineQueriesWithOr(categoryQueries);
            System.out.println(categoryQueries);
        }

        // FirebaseRecyclerOptions를 업데이트하고 어댑터를 다시 설정합니다.
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        adapter.updateOptions(options);
        adapter.notifyDataSetChanged();
    }
    // OR 연산으로 여러 쿼리를 결합하는 메서드
    private Query combineQueriesWithOr(List<Query> queries) {
        if (queries.size() == 1) {
            // 선택한 카테고리가 1개인 경우, 해당 카테고리의 쿼리 반환
            return queries.get(0);
        } else if (queries.size() > 1) {
            // 선택한 카테고리가 2개 이상인 경우, AND 연산을 수행
            DatabaseReference combinedResults = databaseReference.push();

            // 모든 선택한 카테고리에 대한 쿼리 결과를 가져와서 복사
            for (Query query : queries) {
                query.get().addOnSuccessListener(querySnapshot -> {
                    for (DataSnapshot snapshot : querySnapshot.getChildren()) {
                        combinedResults.child(snapshot.getKey()).setValue(true);
                    }
                });
            }

            // AND 연산 쿼리를 생성
            return databaseReference.orderByChild(combinedResults.getKey()).equalTo(true);
        } else {
            // 아무 카테고리도 선택하지 않은 경우, 기본 쿼리 반환
            return databaseReference.orderByKey();
        }
    }
    private void updateReviewsWithCategory(String category) {
        Query query;

        // 선택한 카테고리에 따라 쿼리를 생성
        switch (category) {
            case "Last":
                Array.setText("최신순");
                query = databaseReference.orderByChild("date");
                break;
            case "Rating Descending":
                Array.setText("평점 낮은 순");
                query = databaseReference.orderByChild("rating").limitToLast(50);
                break;
            case "Rating Ascending":
                Array.setText("평점 높은 순");
                query = databaseReference.orderByChild("rating").limitToFirst(50);
                break;
            case "Money Descending":
                Array.setText("가격 낮은 순");
                query = databaseReference.orderByChild("price").limitToLast(50);
                break;
            case "Money Ascending":
                Array.setText("가격 높은 순");
                query = databaseReference.orderByChild("price").limitToFirst(50);
                break;
            default:
                // 다른 카테고리에 대한 처리
                return;
        }

        // FirebaseRecyclerOptions를 업데이트하고 어댑터를 다시 설정합니다.
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        adapter.updateOptions(options);
        adapter.notifyDataSetChanged();
    }

}

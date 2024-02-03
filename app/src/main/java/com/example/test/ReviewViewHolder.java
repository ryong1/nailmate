package com.example.test;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

public class ReviewViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;
    private OnItemClickListener listener; // 리스너 변수 추가
    private View reviewLayout; // review_item.xml의 레이아웃을 참조하기 위한 필드


    public ReviewViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.listener = listener; // 외부에서 전달받은 리스너 설정
        reviewLayout = itemView.findViewById(R.id.reviewLayout); // reviewLayout을 찾습니다.
        imageView = itemView.findViewById(R.id.review_item_image);


        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        setLayoutSize(screenWidth);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // 리사이클러 뷰 아이템을 클릭했을 때 해당 리뷰의 ID를 전달
                    listener.onItemClick(position);
                }

            }
        });
    }
    public void setLayoutSize(int screenWidth) {
        int layoutSize = screenWidth / 3; // 화면 너비를 3으로 나눈 크기를 계산합니다.
        ViewGroup.LayoutParams layoutParams = reviewLayout.getLayoutParams();
        //layoutParams.width = layoutSize; // 가로 크기 설정
        layoutParams.height = layoutSize; // 세로 크기 설정
        reviewLayout.setLayoutParams(layoutParams);
    }

    public void bind(Review review) {
        // 이미지를 설정하는 코드 추가
        Glide.with(itemView.getContext())
                .load(review.getImageUrl())
                .into(imageView);
    }
    // 아이템 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
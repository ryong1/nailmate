package com.example.test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomArrayFragment extends BottomSheetDialogFragment {

    private HomeFragment homeFragment;
    private int selectedTextViewId = -1; // 선택된 TextView의 ID를 저장할 변수

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_array, container, false);

        TextView[] textViews = {
                view.findViewById(R.id.tvLast),
                view.findViewById(R.id.tvRatingD),
                view.findViewById(R.id.tvRatingU),
                view.findViewById(R.id.tvMoneyD),
                view.findViewById(R.id.tvMoneyU),
        };

        // 각 TextView에 OnClickListener 설정
        for (TextView textView : textViews) {
            textView.setOnClickListener(v -> onTextViewClicked(textView));
        }

        Button selectBtn = view.findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(v -> {
            if (homeFragment != null) {
                // 선택된 TextView의 ID를 문자열로 변환하여 HomeFragment에 전달
                String selectedCategory = getStringFromId(selectedTextViewId);
                homeFragment.selectCategory(selectedCategory);
                dismiss(); // Bottom sheet 닫기
            } else {
                // homeFragment가 null일 때 처리
                System.out.println("HomeFragment is null.");
            }
        });

        return view;
    }

    @SuppressLint("ResourceAsColor")
    private void onTextViewClicked(TextView textView) {
        // 이전에 선택된 TextView의 색상을 원래대로 되돌리기
        if (selectedTextViewId != -1) {
            TextView previousSelectedTextView = getView().findViewById(selectedTextViewId);
            previousSelectedTextView.setTextColor(Color.BLACK); // 이전 색상으로 변경
        }

        // 새로 선택된 TextView의 ID 저장 및 텍스트 색상 변경
        selectedTextViewId = textView.getId();
        textView.setTextColor(R.color.color2); // 선택된 색상으로 변경
    }

    private String getStringFromId(int id) {
        // ID에 따라 선택된 카테고리 문자열을 반환
        switch (id) {
            case R.id.tvLast:
                return "Last";
            case R.id.tvRatingD:
                return "Rating Descending";
            case R.id.tvRatingU:
                return "Rating Ascending";
            case R.id.tvMoneyD:
                return "Money Descending";
            case R.id.tvMoneyU:
                return "Money Ascending";
            default:
                return "";
        }
    }

    public void setHomeFragment(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }
}

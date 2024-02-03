package com.example.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private HomeFragment homeFragment;
    private int selectedTextColor; // 선택된 텍스트색 저장 변수
    private int unselectedTextColor; // 선택되지 않은 텍스트색 저장 변수
    private List<String> selectedTextViewNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_theme, container, false);

        selectedTextColor = getResources().getColor(R.color.white); // 선택된 텍스트색
        unselectedTextColor = getResources().getColor(R.color.color5); // 선택되지 않은 텍스트색
        selectedTextViewNames = new ArrayList<>();
        TextView[] textViews = {
                view.findViewById(R.id.glamTextView),
                view.findViewById(R.id.simpleTextView),
                view.findViewById(R.id.uniqueTextView),
                view.findViewById(R.id.y2kTextView),
                view.findViewById(R.id.themeTextView),
                view.findViewById(R.id.romanticTextView),
                view.findViewById(R.id.drawTextView),
                view.findViewById(R.id.patternTextView),
                view.findViewById(R.id.graTextView)
        };

        for (TextView textView : textViews) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTextViewSelection(textView);
                }
            });
        }

        Button selectBtn = view.findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(v -> {
            if (homeFragment != null) {
                // Convert selectedTextViewNames to a list of selected categories
                List<String> selectedCategories = new ArrayList<>(selectedTextViewNames);
                homeFragment.selectCategories(selectedCategories);
                dismiss(); // Close the bottom sheet
            } else {
                // Handle the case when homeFragment is null
                System.out.println("HomeFragment is null.");
            }
        });

        return view;
    }

    public void setHomeFragment(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    private void toggleTextViewSelection(TextView textView) {
        String textViewName = getResources().getResourceEntryName(textView.getId()); // 텍스트뷰의 리소스 ID로부터 이름 가져오기
        if (selectedTextViewNames.contains(textViewName)) {
            // 이미 선택된 텍스트뷰를 다시 클릭한 경우 선택 해제
            selectedTextViewNames.remove(textViewName); // textViewName을 제거해야 합니다.
            textView.setBackground(getResources().getDrawable(R.drawable.rounded_border)); // 배경 변경
            textView.setTextColor(unselectedTextColor); // 글씨 색상 변경
        } else {
            // 새로운 텍스트뷰를 선택한 경우
            selectedTextViewNames.add(textViewName); // textViewName을 추가해야 합니다.
            textView.setBackground(getResources().getDrawable(R.drawable.select_rounded_border)); // 배경 변경
            textView.setTextColor(selectedTextColor); // 글씨 색상 변경
        }
    }


}

package com.example.test;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SearchFragment extends Fragment {
    private ListView listView;
    private ArrayAdapter<ShopInfo> adapter;
    private List<ShopInfo> resultList;
    private EditText searchEditText;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference csvFileRef;
    private Handler uiHandler;
    private ExecutorService executorService;
    private String query;
    private boolean comingFromHomeFragment = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        listView = view.findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        searchEditText = view.findViewById(R.id.searchEditText);

        //파이어베이스 스토리지 열기
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        csvFileRef = storageRef.child("data/file.csv");
        File localFile = new File(requireContext().getCacheDir(), "file.csv");


        uiHandler = new Handler();

        // Initialize the executor service for background processing
        executorService = Executors.newCachedThreadPool();

        // Download the CSV file if not already downloaded
        if (!localFile.exists()) {
            csvFileRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            executorService.submit(new ParseCSVDataTask(localFile));
                        }
                    });
        } else {
            executorService.submit(new ParseCSVDataTask(localFile));
        }
        // Check if coming from HomeFragment
        Bundle args = getArguments();
        if (args != null) {
            comingFromHomeFragment = args.getBoolean("comingFromHomeFragment", false);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 사용자가 선택한 아이템을 가져옴
                ShopInfo selectedShopInfo = adapter.getItem(position);

                if (selectedShopInfo != null) {
                    navigateToCorrectFragment(selectedShopInfo);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString();  // 검색어를 query 변수에 저장
                performSearch(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private class ParseCSVDataTask implements Runnable {
        private File csvFile;

        public ParseCSVDataTask(File csvFile) {
            this.csvFile = csvFile;
        }

        @Override
        public void run() {
            List<ShopInfo> result = parseCSVData(csvFile);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Update UI with the parsed data
                    resultList = result;
                    adapter.clear();
                    adapter.addAll(resultList);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        private List<ShopInfo> parseCSVData(File csvFile) {
            List<ShopInfo> resultList = new ArrayList<>();
            BufferedReader reader = null;

            try {
                InputStream inputStream = new FileInputStream(csvFile);
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                String line;
                while ((line = reader.readLine()) != null) {
                    // CSV 라인을 쉼표 다음에 공백(', ')으로 분리
                    String[] values = line.split(",");

                    String businessName = ""; // 초기화
                    StringBuilder roadAddressBuilder = new StringBuilder();

                    int addressEndIndex = findAddressEndIndex(values); // 주소가 끝나는 인덱스 찾기
                    if (addressEndIndex < values.length - 2) { // 주소가 끝난 후에 2개 이상의 항목이 남아 있다면
                        businessName = values[addressEndIndex + 2]; // 다음 다음 항목을 businessName으로 설정
                        roadAddressBuilder.append(values[19]); // 주소 시작 부분
                        for (int i = 20; i <= addressEndIndex; i++) {
                            roadAddressBuilder.append(" ").append(values[i]);
                        }
                    }

                    // 주소에서 " 제거
                    String roadAddress = roadAddressBuilder.toString().replace("\"", "");

                    // businessName 앞뒤의 큰 따옴표 제거
                    businessName = businessName.trim().replaceAll("^\"|\"$", "");

                    ShopInfo shopInfo = new ShopInfo(businessName, roadAddress);
                    resultList.add(shopInfo);
                }

                // Sort the resultList by business name
                Collections.sort(resultList, new Comparator<ShopInfo>() {
                    @Override
                    public int compare(ShopInfo shop1, ShopInfo shop2) {
                        // 한글, 영어, 특수문자 순서로 정렬
                        Collator collator = Collator.getInstance(Locale.KOREAN);
                        collator.setStrength(Collator.PRIMARY);
                        return collator.compare(shop1.getBusinessName(), shop2.getBusinessName());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return resultList;
        }

        private int findAddressEndIndex(String[] values) {
            for (int i = 19; i < values.length; i++) {
                if (values[i].endsWith("\"")) {
                    return i;
                }
            }
            return values.length - 1;
        }
    }

    private class SearchTask implements Runnable {
        private String query;

        public SearchTask(String query) {
            this.query = query;
        }

        @Override
        public void run() {
            List<ShopInfo> result = performSearchInBackground(query);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Update UI with the search results
                    adapter.clear();
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private List<ShopInfo> performSearchInBackground(String query) {
        List<ShopInfo> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            // 검색어가 비어 있으면 CSV 파일에 있는 모든 가게 정보를 추가
            filteredList.addAll(resultList);
        } else {
            for (ShopInfo shop : resultList) {
                String businessName = shop.getBusinessName().toLowerCase();
                if (businessName.contains(query)) {
                    filteredList.add(shop);
                }
            }
        }
        return filteredList;
    }

    private void performSearch(String query) {
        executorService.submit(new SearchTask(query));
    }

    private void navigateToCorrectFragment(ShopInfo selectedShopInfo) {
        // 선택한 가게 정보를 ShopFragment 또는 WriteFragment로 전달합니다.
        Bundle args = new Bundle();
        args.putParcelable("selectedShopInfo", selectedShopInfo);

        if (comingFromHomeFragment) {
            // HomeFragment에서 온 경우, ShopFragment로 이동합니다.
            ShopFragment shopFragment = new ShopFragment();
            shopFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, shopFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            // WriteFragment에서 온 경우, WriteFragment로 이동합니다.
            WriteFragment writeFragment = new WriteFragment();
            writeFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, writeFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
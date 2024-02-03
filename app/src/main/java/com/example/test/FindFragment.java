package com.example.test;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindFragment extends Fragment {
    private Spinner spinnerSido;
    private Spinner spinnerSigungu;
    private ListView listView;
    private Handler uiHandler;
    private ArrayAdapter<ShopInfo> adapter;
    private List<ShopInfo> resultList;
    private String query;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference csvFileRef;

    // 시도명, 시군구명, 읍면동 데이터를 저장할 lists
    private List<String> sidoData = new ArrayList<>();
    private List<String> sigunguData = new ArrayList<>();
    private List<String> eupMyeonDongData = new ArrayList<>();
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);

        spinnerSido = view.findViewById(R.id.spinnerSido);
        spinnerSigungu = view.findViewById(R.id.spinnerSigungu);
        listView = view.findViewById(R.id.listView);

        // Adapter 초기화
        resultList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultList);
        listView.setAdapter(adapter);
        //파이어베이스에서 스토리지 열기
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        csvFileRef = storageRef.child("data/file.csv");
        File localFile = new File(requireContext().getCacheDir(), "file.csv");

        // Handler 초기화
        uiHandler = new Handler();

        executorService = Executors.newCachedThreadPool();

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
            System.out.println("왜 안되는거니");
        }

        // CSV 파일에서 데이터를 읽어옴
        try {
            readCSVData();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
        // Spinner 어댑터 설정
        ArrayAdapter<String> sidoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sidoData);
        ArrayAdapter<String> sigunguAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());

        // 기본 힌트 텍스트 추가
        sidoAdapter.insert("시/도", 0);
        sigunguAdapter.insert("시/군/구", 0);

        spinnerSido.setAdapter(sidoAdapter);
        spinnerSigungu.setAdapter(sigunguAdapter);

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

        // 첫 번째 Spinner(Sido) 선택 이벤트 처리
        spinnerSido.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedSido = spinnerSido.getSelectedItem().toString();
                    String Sigungu = "";
                    sigunguAdapter.clear();
                    sigunguAdapter.insert("시/군/구", 0);
                    Set<String> uniqueSigungu = new HashSet<>();
                    // 선택된 시도명에 해당하는 시군구명 데이터를 추출
                    for (String line : eupMyeonDongData) {
                        String[] parts = line.split(",");
                        if (parts[0].equals(selectedSido)) {
                            Sigungu = parts[1];
                            uniqueSigungu.add(Sigungu);
                        }
                    }
                    for (String item : uniqueSigungu) {
                        sigunguAdapter.add(item);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 두 번째 Spinner(Sigungu) 선택 이벤트 처리
        spinnerSigungu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedSido = spinnerSido.getSelectedItem().toString();
                    String selectedSigungu = spinnerSigungu.getSelectedItem().toString();
                    query = selectedSido + selectedSigungu;
                    performSearch(query);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }


    private void readCSVData() throws CsvException {
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.korea)));

            // Use a Set to store unique Sido values
            Set<String> uniqueSido = new HashSet<>();

            List<String[]> lines = reader.readAll();
            for (String[] parts : lines) {
                if (parts.length >= 3) {
                    String sido = parts[0]; // 1열: 시도명
                    String sigungu = parts[1]; // 2열: 시군구명
                    String eupMyeonDong = parts[2]; // 3열: 읍면동명

                    // Add Sido to the uniqueSido set
                    uniqueSido.add(sido);

                    sigunguData.add(sigungu);
                    eupMyeonDongData.add(sido + ", " + sigungu + ", " + eupMyeonDong);
                }
            }

            // Create a list from the uniqueSido set to maintain order
            sidoData = new ArrayList<>(uniqueSido);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            resultList.clear();

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
        synchronized (resultList) {
            System.out.println(resultList);
            if (query.isEmpty()) {
                // 검색어가 비어 있으면 CSV 파일에 있는 모든 가게 정보를 추가
                filteredList.addAll(resultList);
            } else {
                for (ShopInfo shop : resultList) {
                    String roadAddress = shop.getRoadAddress().toLowerCase();
                    if (roadAddress.contains(query)) {
                        filteredList.add(shop);
                    }
                }
            }
        }
        return filteredList;
    }
    private void performSearch(String query) {
        executorService.submit(new SearchTask(query));
    }
    private void navigateToCorrectFragment(ShopInfo selectedShopInfo) {
        // 선택한 가게 정보를 ShopFragment
        Bundle args = new Bundle();
        args.putParcelable("selectedShopInfo", selectedShopInfo);

        ShopFragment shopFragment = new ShopFragment();
        shopFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, shopFragment)
                .addToBackStack(null)
                .commit();
    }
}
package com.example.hadonggymapp; // << NHỚ THAY BẰNG PACKAGE NAME CỦA BẠN

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query; // THÊM IMPORT NÀY NẾU CHƯA CÓ
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;

import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerViewGyms;
    private GymAdapter gymAdapter;
    private List<Gym> gymList;
    private List<Gym> originalGymList;

    private FirebaseFirestore db;
    private Query gymsCollectionRef;
    private ProgressBar progressBarMain;
    private TextView textViewEmptyState;
    private SwipeRefreshLayout swipeRefreshLayoutMain;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String currentSearchQuery = "";
    private boolean isAdmin = false;
    private Menu mainMenu;

    private MaterialButton buttonFilter;

    private BottomSheetDialog filterBottomSheet;
    private ChipGroup bottomSheetChipGroupServices;
    private ChipGroup bottomSheetChipGroupAmenities;

    private List<String> selectedServicesFilter = new ArrayList<>();
    private List<String> selectedAmenitiesFilter = new ArrayList<>();

    // Danh sách dịch vụ và tiện ích chuẩn (Hardcoded tạm thời)
    private static final List<String> MASTER_SERVICES = Arrays.asList(
        "Yoga", "Zumba", "Đạp xe trong nhà", "Huấn luyện viên cá nhân", "Group X",
        "Kickboxing", "Pilates", "HIIT", "Boxing", "Yoga nâng cao", "Tập sức mạnh"
    );

    private static final List<String> MASTER_AMENITIES = Arrays.asList(
        "Phòng xông hơi", "Bể bơi", "Quầy bar dinh dưỡng", "Chỗ đậu xe", "Bể bơi bốn mùa",
        "Khu vực thư giãn", "Phòng thay đồ tiện nghi", "Nước uống miễn phí", "Quán cà phê"
    );

    private TextView textViewGreeting;
    private Button buttonFavorites;

    private float minRatingFilter = 0f; // 0 = không lọc, 3 = chỉ hiện >= 3 sao
    private int ratingSortOrder = 0; // 0 = không sắp xếp, 1 = tăng dần, 2 = giảm dần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Luôn chế độ sáng
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Nếu chưa đăng nhập, chuyển đến LoginActivity và không làm gì thêm ở MainActivity
            Log.d(TAG, "User not logged in. Redirecting to LoginActivity.");
            goToLoginActivity();
            return; // << QUAN TRỌNG: Dừng thực thi onCreate ở đây
        }

        // Chỉ thực hiện các lệnh dưới đây NẾU người dùng đã đăng nhập
        setContentView(R.layout.activity_main);
        Log.d(TAG, "User " + currentUser.getEmail() + " is logged in. Proceeding with MainActivity.");

        // Khởi tạo UI và các thành phần khác
        initializeUIComponents();
        setupRecyclerView();
        setupFirebase();
        setupListeners();
        setupFilterBottomSheet();
        setupRatingFilterButton();

        // Tải dữ liệu lần đầu
        fetchGymDataFromFirestore(false);
    }

    private void initializeUIComponents() {
        swipeRefreshLayoutMain = findViewById(R.id.swipeRefreshLayoutMain);
        progressBarMain = findViewById(R.id.progressBarMain);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        recyclerViewGyms = findViewById(R.id.recyclerViewGyms);
        buttonFilter = findViewById(R.id.buttonFilter);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        buttonFavorites = findViewById(R.id.buttonFavorites);
        buttonFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteGymsActivity.class);
            startActivity(intent);
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.main_activity_title));
        }
    }

    private void setupRecyclerView() {
        recyclerViewGyms.setHasFixedSize(true);
        recyclerViewGyms.setLayoutManager(new LinearLayoutManager(this));

        gymList = new ArrayList<>();
        originalGymList = new ArrayList<>();
        gymAdapter = new GymAdapter(this, gymList, false);
        recyclerViewGyms.setAdapter(gymAdapter);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        // Giả sử bạn muốn sắp xếp theo tên (thêm Query.Direction.ASCENDING nếu cần)
        gymsCollectionRef = db.collection("gyms").orderBy("name", Query.Direction.ASCENDING);
    }

    private void setupListeners() {
        gymAdapter.setOnItemClickListener(new GymAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Gym gym) {
                Intent intent = new Intent(MainActivity.this, GymDetailActivity.class);
                intent.putExtra(GymDetailActivity.EXTRA_GYM_OBJECT, gym);
                startActivity(intent);
            }
        });

        swipeRefreshLayoutMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                fetchGymDataFromFirestore(true);
            }
        });
    }

    private void setupFilterBottomSheet() {
        filterBottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filter, null);
        filterBottomSheet.setContentView(bottomSheetView);

        // Initialize chip groups from bottom sheet
        bottomSheetChipGroupServices = bottomSheetView.findViewById(R.id.chipGroupServices);
        bottomSheetChipGroupAmenities = bottomSheetView.findViewById(R.id.chipGroupAmenities);

        // Setup chips in bottom sheet (will now also set checked state)
        setupFilterChips(bottomSheetChipGroupServices, MASTER_SERVICES, selectedServicesFilter);
        setupFilterChips(bottomSheetChipGroupAmenities, MASTER_AMENITIES, selectedAmenitiesFilter);

        // Setup buttons
        bottomSheetView.findViewById(R.id.buttonReset).setOnClickListener(v -> {
            bottomSheetChipGroupServices.clearCheck();
            bottomSheetChipGroupAmenities.clearCheck();
            selectedServicesFilter.clear();
            selectedAmenitiesFilter.clear();
            applyFilter(selectedServicesFilter, selectedAmenitiesFilter);
            filterBottomSheet.dismiss();
        });

        bottomSheetView.findViewById(R.id.buttonApply).setOnClickListener(v -> {
            // Get selected chips and apply filter
            // The selectedServicesFilter and selectedAmenitiesFilter lists are now updated by the OnCheckedStateChangeListener
            applyFilter(selectedServicesFilter, selectedAmenitiesFilter);
            filterBottomSheet.dismiss();
        });

        // Setup main filter button - now also restores checked state when shown
        buttonFilter.setOnClickListener(v -> {
            // Re-setup chips to reflect current selected state before showing
            setupFilterChips(bottomSheetChipGroupServices, MASTER_SERVICES, selectedServicesFilter);
            setupFilterChips(bottomSheetChipGroupAmenities, MASTER_AMENITIES, selectedAmenitiesFilter);
            filterBottomSheet.show();
        });

        // Add listeners to update the selected filter lists when chips are checked/unchecked
        bottomSheetChipGroupServices.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedServicesFilter = getSelectedChips(bottomSheetChipGroupServices);
        });

        bottomSheetChipGroupAmenities.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedAmenitiesFilter = getSelectedChips(bottomSheetChipGroupAmenities);
        });
    }

    private void setupFilterChips(ChipGroup chipGroup, List<String> items, List<String> selectedItems) {
        chipGroup.removeAllViews();
        for (String item : items) {
            Chip chip = new Chip(this, null, R.style.CustomChipStyle);
            chip.setText(item);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setFocusable(true);
            // Set the checked state based on selectedItems list
            chip.setChecked(selectedItems.contains(item));
            chipGroup.addView(chip);
        }
    }

    private List<String> getSelectedChips(ChipGroup chipGroup) {
        List<String> selected = new ArrayList<>();
        for (int id : chipGroup.getCheckedChipIds()) {
            Chip chip = chipGroup.findViewById(id);
            if (chip != null) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
    }

    private void setupRatingFilterButton() {
        MaterialButton buttonFilterRating = findViewById(R.id.buttonFilterRating);
        buttonFilterRating.setOnClickListener(v -> {
            String[] options = {"Tất cả", "Từ 3★", "Từ 4★", "Từ 5★", "Sắp xếp: Thấp → Cao", "Sắp xếp: Cao → Thấp"};
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lọc/Sắp xếp theo đánh giá")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { minRatingFilter = 0f; ratingSortOrder = 0; }
                    else if (which == 1) { minRatingFilter = 3f; ratingSortOrder = 0; }
                    else if (which == 2) { minRatingFilter = 4f; ratingSortOrder = 0; }
                    else if (which == 3) { minRatingFilter = 5f; ratingSortOrder = 0; }
                    else if (which == 4) { ratingSortOrder = 1; } // tăng dần
                    else if (which == 5) { ratingSortOrder = 2; } // giảm dần
                    applyFilter(selectedServicesFilter, selectedAmenitiesFilter);
                })
                .show();
        });
    }

    private void applyFilter(List<String> selectedServices, List<String> selectedAmenities) {
        List<Gym> filteredList = new ArrayList<>();

        if (originalGymList != null) {
            for (Gym gym : originalGymList) {
                boolean matchesSearch = TextUtils.isEmpty(currentSearchQuery) ||
                        (gym.getName() != null && gym.getName().toLowerCase().contains(currentSearchQuery.toLowerCase())) ||
                        (gym.getAddress() != null && gym.getAddress().toLowerCase().contains(currentSearchQuery.toLowerCase()));

                boolean matchesServices = selectedServices.isEmpty() ||
                        (gym.getServices() != null && gym.getServices().containsAll(selectedServices));

                boolean matchesAmenities = selectedAmenities.isEmpty() ||
                        (gym.getAmenities() != null && gym.getAmenities().containsAll(selectedAmenities));

                boolean matchesRating = gym.getAverageRating() >= minRatingFilter;

                if (matchesSearch && matchesServices && matchesAmenities && matchesRating) {
                    filteredList.add(gym);
                }
            }
        }

        // Sắp xếp theo số sao nếu có chọn
        if (ratingSortOrder == 1) {
            java.util.Collections.sort(filteredList, java.util.Comparator.comparingDouble(Gym::getAverageRating));
        } else if (ratingSortOrder == 2) {
            java.util.Collections.sort(filteredList, (g1, g2) -> Float.compare(g2.getAverageRating(), g1.getAverageRating()));
        }

        gymList.clear();
        gymList.addAll(filteredList);
        gymAdapter.notifyDataSetChanged();
        updateEmptyStateView(gymList, false, currentSearchQuery);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            Intent intent = new Intent(MainActivity.this, FavoriteGymsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_user_profile) {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_admin) {
            Intent intent = new Intent(MainActivity.this, AdminGymActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchGymDataFromFirestore(boolean isSwipeRefresh) {
        if (!isSwipeRefresh) {
            progressBarMain.setVisibility(View.VISIBLE);
        }
        recyclerViewGyms.setVisibility(View.GONE);
        textViewEmptyState.setVisibility(View.GONE);

        gymsCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (swipeRefreshLayoutMain.isRefreshing()) {
                    swipeRefreshLayoutMain.setRefreshing(false);
                }
                if (!isSwipeRefresh) {
                    progressBarMain.setVisibility(View.GONE);
                }

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    Toast.makeText(MainActivity.this, getString(R.string.error_loading_data_check_connection), Toast.LENGTH_LONG).show();
                    // Khi có lỗi, originalGymList có thể vẫn giữ giá trị cũ, nên clear nó nếu muốn
                    // originalGymList.clear(); // Tùy chọn: reset original list khi lỗi
                    updateEmptyStateView(new ArrayList<>(), true, currentSearchQuery); // Hiển thị lỗi với danh sách rỗng
                    return;
                }

                if (snapshots != null) {
                    originalGymList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document : snapshots.getDocuments()) {
                        Gym gym = document.toObject(Gym.class);
                        if (gym != null) {
                            gym.setId(document.getId());
                            // Lấy thủ công averageRating và reviewCount nếu có
                            if (document.contains("averageRating")) {
                                Double avg = document.getDouble("averageRating");
                                gym.setAverageRating(avg != null ? avg.floatValue() : 0f);
                            }
                            if (document.contains("reviewCount")) {
                                Long count = document.getLong("reviewCount");
                                gym.setReviewCount(count != null ? count.intValue() : 0);
                            }
                            originalGymList.add(gym);
                        }
                    }
                    Log.d(TAG, "Data fetched. Original list size: " + originalGymList.size());
                    // --- ĐỒNG BỘ TRẠNG THÁI YÊU THÍCH ---
                    FavoriteGymManager favoriteGymManager = new FavoriteGymManager();
                    favoriteGymManager.getFavoriteGyms(new FavoriteGymManager.OnFavoriteGymsLoadedListener() {
                        @Override
                        public void onFavoriteGymsLoaded(List<Gym> favoriteGyms) {
                            java.util.Set<String> favoriteGymIds = new java.util.HashSet<>();
                            for (Gym gym : favoriteGyms) {
                                favoriteGymIds.add(gym.getId());
                            }
                            for (Gym gym : originalGymList) {
                                gym.setFavorite(favoriteGymIds.contains(gym.getId()));
                            }
                            filterGymList(currentSearchQuery); // Áp dụng lại bộ lọc hiện tại
                        }
                        @Override
                        public void onError(String errorMessage) {
                            // Nếu lỗi, vẫn filter như bình thường (không set favorite)
                            filterGymList(currentSearchQuery);
                        }
                    });
                    // --- END ĐỒNG BỘ YÊU THÍCH ---
                } else {
                    Log.d(TAG, "Current data: null from Firestore.");
                    originalGymList.clear();
                    filterGymList(currentSearchQuery); // Xử lý trường hợp snapshots là null
                }
            }
        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateEmptyStateView(List<Gym> listToDisplay, boolean isError, String currentQuery) {
        if (listToDisplay.isEmpty()) {
            recyclerViewGyms.setVisibility(View.GONE);
            if (isError) {
                textViewEmptyState.setText(getString(R.string.error_loading_data_check_connection));
            } else if (currentQuery != null && !currentQuery.isEmpty()) {
                textViewEmptyState.setText("Không tìm thấy kết quả phù hợp cho: \"" + currentQuery + "\"");
            } else {
                textViewEmptyState.setText(getString(R.string.no_gyms_found));
            }
            textViewEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewGyms.setVisibility(View.VISIBLE);
            textViewEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        mainMenu = menu;
        MenuItem searchItem = menu.findItem(R.id.action_search_gym);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView == null) {
            Log.e(TAG, "SearchView is null. Check your menu XML.");
            return super.onCreateOptionsMenu(menu);
        }

        searchView.setQueryHint("Tìm theo tên phòng gym...");
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(currentSearchQuery, false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim();
                filterGymList(currentSearchQuery);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                currentSearchQuery = "";
                filterGymList("");
                return true;
            }
        });

        // Ẩn menu admin mặc định, chỉ hiển thị nếu là admin
        MenuItem adminItem = menu.findItem(R.id.action_admin);
        if (adminItem != null) {
            adminItem.setVisible(isAdmin);
        }
        checkAdminPermission();
        loadUserProfile(); // Load user profile to get name for greeting
        return super.onCreateOptionsMenu(menu);
    }

    private void checkAdminPermission() {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
                        // Kiểm tra cả role và isAdmin
                        String role = documentSnapshot.getString("role");
                        Boolean isAdminBool = documentSnapshot.getBoolean("isAdmin");
                        isAdmin = "admin".equals(role) || (isAdminBool != null && isAdminBool);
                        
                        if (mainMenu != null) {
                            MenuItem adminItem = mainMenu.findItem(R.id.action_admin);
                            if (adminItem != null) {
                                adminItem.setVisible(isAdmin);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Lỗi kiểm tra quyền admin", e);
                    }
                });
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            textViewGreeting.setText("Xin chào!"); // Default greeting if user not logged in
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        if (userName != null && !userName.isEmpty()) {
                            textViewGreeting.setText("Xin chào, " + userName + "!");
                        } else {
                            textViewGreeting.setText("Xin chào!"); // Default if name is empty
                        }
                    } else {
                        textViewGreeting.setText("Xin chào!"); // Default if user document not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải profile người dùng", e);
                    textViewGreeting.setText("Xin chào!"); // Default on error
                });
    }

    private void filterGymList(String searchText) {
        currentSearchQuery = searchText;
        List<Gym> filteredList = new ArrayList<>();

        if (originalGymList != null) {
            for (Gym gym : originalGymList) {
                // Lọc theo tìm kiếm (tên hoặc địa chỉ)
                boolean matchesSearch = TextUtils.isEmpty(searchText) ||
                                        (gym.getName() != null && gym.getName().toLowerCase().contains(searchText.toLowerCase())) ||
                                        (gym.getAddress() != null && gym.getAddress().toLowerCase().contains(searchText.toLowerCase()));

                // Add to filtered list if matches search (chip filtering is handled in applyFilter)
                if (matchesSearch) {
                    filteredList.add(gym);
                }
            }
        }

        // Update RecyclerView and empty state
        gymList.clear();
        gymList.addAll(filteredList);
        gymAdapter.notifyDataSetChanged();
        updateEmptyStateView(gymList, false, currentSearchQuery);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchGymDataFromFirestore(false);
    }
}
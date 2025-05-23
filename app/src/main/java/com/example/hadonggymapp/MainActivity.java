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

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Tải dữ liệu lần đầu
        fetchGymDataFromFirestore(false);
    }

    private void initializeUIComponents() {
        swipeRefreshLayoutMain = findViewById(R.id.swipeRefreshLayoutMain);
        progressBarMain = findViewById(R.id.progressBarMain);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        recyclerViewGyms = findViewById(R.id.recyclerViewGyms);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.main_activity_title));
        }
    }

    private void setupRecyclerView() {
        recyclerViewGyms.setHasFixedSize(true);
        recyclerViewGyms.setLayoutManager(new LinearLayoutManager(this));

        gymList = new ArrayList<>();
        originalGymList = new ArrayList<>();
        gymAdapter = new GymAdapter(this, gymList);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_user_profile) {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
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
                            originalGymList.add(gym);
                        }
                    }
                    Log.d(TAG, "Data fetched. Original list size: " + originalGymList.size());
                    filterGymList(currentSearchQuery); // Áp dụng lại bộ lọc hiện tại
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
            // searchView.clearFocus(); // Có thể không cần clearFocus ở đây, để người dùng có thể sửa tiếp
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                // currentSearchQuery = query.trim(); // Đã cập nhật trong onQueryTextChange
                // filterGymList(currentSearchQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim(); // Cập nhật query khi text thay đổi
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
                // Khi đóng search view, filter với query rỗng sẽ hiển thị lại original list
                filterGymList("");
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void filterGymList(String searchText) {
        //searchText đã được trim() từ onQueryTextChange
        List<Gym> listToFilterFrom = (originalGymList == null) ? new ArrayList<>() : originalGymList;
        List<Gym> filteredList = new ArrayList<>();

        if (searchText.isEmpty()) {
            filteredList.addAll(listToFilterFrom);
        } else {
            String filterPattern = searchText.toLowerCase();
            for (Gym gym : listToFilterFrom) {
                if (gym.getName() != null && gym.getName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(gym);
                }
            }
        }

        gymList.clear();
        gymList.addAll(filteredList);
        gymAdapter.notifyDataSetChanged(); // Hoặc gymAdapter.updateData(filteredList) nếu bạn thích dùng hàm đó

        updateEmptyStateView(filteredList, false, searchText);
    }
}
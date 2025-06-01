package com.example.hadonggymapp;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoriteGymManager {
    private static final String TAG = "FavoriteGymManager";
    private static final String FAVORITES_COLLECTION = "favorites";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FavoriteGymManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void toggleFavorite(Gym gym, OnFavoriteToggleListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Vui lòng đăng nhập để sử dụng tính năng này");
            }
            return;
        }

        String userId = currentUser.getUid();
        String gymId = gym.getId();

        db.collection(FAVORITES_COLLECTION)
                .document(userId + "_" + gymId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nếu đã yêu thích, xóa khỏi danh sách
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    gym.setFavorite(false);
                                    if (listener != null) {
                                        listener.onFavoriteToggled(gym, false);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing favorite", e);
                                    if (listener != null) {
                                        listener.onError("Lỗi khi xóa khỏi danh sách yêu thích");
                                    }
                                });
                    } else {
                        // Nếu chưa yêu thích, thêm vào danh sách
                        db.collection(FAVORITES_COLLECTION)
                                .document(userId + "_" + gymId)
                                .set(new FavoriteGym(userId, gymId))
                                .addOnSuccessListener(aVoid -> {
                                    gym.setFavorite(true);
                                    if (listener != null) {
                                        listener.onFavoriteToggled(gym, true);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding favorite", e);
                                    if (listener != null) {
                                        listener.onError("Lỗi khi thêm vào danh sách yêu thích");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite status", e);
                    if (listener != null) {
                        listener.onError("Lỗi khi kiểm tra trạng thái yêu thích");
                    }
                });
    }

    public void getFavoriteGyms(OnFavoriteGymsLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Vui lòng đăng nhập để xem danh sách yêu thích");
            }
            return;
        }

        String userId = currentUser.getUid();
        List<String> favoriteGymIds = new ArrayList<>();

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FavoriteGym favoriteGym = document.toObject(FavoriteGym.class);
                        if (favoriteGym != null) {
                            favoriteGymIds.add(favoriteGym.getGymId());
                        }
                    }

                    if (favoriteGymIds.isEmpty()) {
                        if (listener != null) {
                            listener.onFavoriteGymsLoaded(new ArrayList<>());
                        }
                        return;
                    }

                    // Lấy thông tin chi tiết của các phòng gym yêu thích
                    db.collection("gyms")
                            .whereIn("id", favoriteGymIds)
                            .get()
                            .addOnSuccessListener(gymSnapshots -> {
                                List<Gym> favoriteGyms = new ArrayList<>();
                                for (QueryDocumentSnapshot document : gymSnapshots) {
                                    Gym gym = document.toObject(Gym.class);
                                    if (gym != null) {
                                        gym.setId(document.getId());
                                        gym.setFavorite(true);
                                        favoriteGyms.add(gym);
                                    }
                                }
                                if (listener != null) {
                                    listener.onFavoriteGymsLoaded(favoriteGyms);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading favorite gyms", e);
                                if (listener != null) {
                                    listener.onError("Lỗi khi tải danh sách phòng gym yêu thích");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading favorites", e);
                    if (listener != null) {
                        listener.onError("Lỗi khi tải danh sách yêu thích");
                    }
                });
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggled(Gym gym, boolean isFavorite);
        void onError(String errorMessage);
    }

    public interface OnFavoriteGymsLoadedListener {
        void onFavoriteGymsLoaded(List<Gym> favoriteGyms);
        void onError(String errorMessage);
    }

    private static class FavoriteGym {
        private String userId;
        private String gymId;

        public FavoriteGym() {
            // Required empty constructor for Firestore
        }

        public FavoriteGym(String userId, String gymId) {
            this.userId = userId;
            this.gymId = gymId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getGymId() {
            return gymId;
        }

        public void setGymId(String gymId) {
            this.gymId = gymId;
        }
    }
} 
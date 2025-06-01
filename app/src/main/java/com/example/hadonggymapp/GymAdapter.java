package com.example.hadonggymapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {

    private Context context;
    private List<Gym> gymList;
    private OnItemClickListener listener; // Interface để xử lý sự kiện click
    private OnItemLongClickListener longClickListener; // Interface cho long click
    private OnDeleteClickListener deleteListener; // Listener mới cho nút xóa
    private boolean isAdminContext; // Biến để kiểm tra ngữ cảnh admin
    private FavoriteGymManager favoriteGymManager;

    // Interface để Activity có thể lắng nghe sự kiện click trên item
    public interface OnItemClickListener {
        void onItemClick(Gym gym); // Truyền đối tượng Gym được click
    }

    // Interface cho long click
    public interface OnItemLongClickListener {
        void onItemLongClick(Gym gym);
    }

    // Interface mới cho sự kiện click nút xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(Gym gym);
    }

    // Phương thức để Activity đăng ký lắng nghe
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    // Phương thức để Activity đăng ký lắng nghe sự kiện xóa
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    // Constructor của Adapter - Thêm tham số isAdminContext
    public GymAdapter(Context context, List<Gym> gymList, boolean isAdminContext) {
        this.context = context;
        this.gymList = gymList;
        this.isAdminContext = isAdminContext; // Lưu trạng thái admin
        this.favoriteGymManager = new FavoriteGymManager();
    }

    // Phương thức này được gọi khi RecyclerView cần tạo một ViewHolder mới.
    // Nó "inflate" (thổi phồng/tạo) layout item (list_item_gym.xml) và trả về một ViewHolder mới.
    @NonNull
    @Override
    public GymViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_gym, parent, false);
        return new GymViewHolder(view);
    }

    // Phương thức này được gọi để hiển thị dữ liệu tại một vị trí cụ thể.
    // Nó lấy dữ liệu từ đối tượng Gym tại vị trí 'position' và gán vào các View trong ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull GymViewHolder holder, int position) {
        Gym currentGym = gymList.get(position);

        // Load image using Glide
        if (currentGym.getImageUrl() != null && !currentGym.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(currentGym.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.imageViewGym);
        } else if (currentGym.getImageUrls() != null && !currentGym.getImageUrls().isEmpty()) {
             // If multiple images available, use the first one for the list item
             Glide.with(context)
                    .load(currentGym.getImageUrls().get(0))
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.imageViewGym);
        }
        else {
            // If no image URL, display default image
            holder.imageViewGym.setImageResource(R.mipmap.ic_launcher);
        }

        // Display Gym Name and Address
        holder.textViewGymName.setText(currentGym.getName());
        holder.textViewGymAddress.setText(currentGym.getAddress() != null ? currentGym.getAddress() : "Đang cập nhật địa chỉ");

        // Display Gym Hours (if available)
        if (currentGym.getHours() != null && !currentGym.getHours().isEmpty()) {
            holder.textViewGymHours.setText("Giờ mở cửa: " + currentGym.getHours());
            holder.containerGymHours.setVisibility(View.VISIBLE); // Show the container if hours available
        } else {
            holder.containerGymHours.setVisibility(View.GONE); // Hide the container if no hours
        }

        // Set favorite icon state (dùng local)
        boolean isFavorite = FavoriteLocalManager.getFavorites(context).contains(currentGym.getId());
        holder.imageViewFavorite.setImageResource(
            isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
        );

        // Set favorite button click listener (dùng local)
        holder.imageViewFavorite.setOnClickListener(v -> {
            if (context == null) {
                android.util.Log.e("FAV_DEBUG", "Context is null, cannot toggle favorite");
                return;
            }
            String gymId = currentGym.getId();
            if (gymId == null || gymId.isEmpty()) {
                android.util.Log.e("FAV_DEBUG", "GymId is null or empty, cannot toggle favorite");
                Toast.makeText(context, "Không thể thao tác yêu thích với phòng gym này!", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean currentlyFavorite = FavoriteLocalManager.getFavorites(context).contains(gymId);
            if (currentlyFavorite) {
                FavoriteLocalManager.removeFavorite(context, gymId);
                holder.imageViewFavorite.setImageResource(R.drawable.ic_favorite_border);
                Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                FavoriteLocalManager.addFavorite(context, gymId);
                holder.imageViewFavorite.setImageResource(R.drawable.ic_favorite);
                Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide/show delete button based on admin context
        if (isAdminContext) {
            holder.imageViewDeleteGym.setVisibility(View.VISIBLE);
            // Attach click listener for delete button
            holder.imageViewDeleteGym.setOnClickListener(v -> {
                if (deleteListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        deleteListener.onDeleteClick(gymList.get(currentPosition));
                    }
                }
            });
        } else {
            holder.imageViewDeleteGym.setVisibility(View.GONE);
            // Remove listener if button is hidden
            holder.imageViewDeleteGym.setOnClickListener(null);
        }

        // Attach click listener to the whole item view (for details)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                     // Ensure position is valid
                    int currentPosition = holder.getAdapterPosition();
                     if (currentPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(gymList.get(currentPosition));
                     }
                }
            }
        });

        // Attach long click listener to the whole item view (optional)
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(gymList.get(currentPosition));
                        return true; // Consume the long click event
                    }
                }
                return false; // Do not consume long click
            }
        });

        // Hiển thị số sao trung bình nếu có
        if (holder.ratingBarItemAverage != null) {
            float avg = currentGym.getAverageRating();
            holder.ratingBarItemAverage.setRating(avg);
            // Thêm text hiển thị số sao và số lượng đánh giá
            if (avg > 0) {
                holder.textViewGymHours.setText(String.format("★ %.1f (%d đánh giá)", avg, currentGym.getReviewCount()));
            } else {
                holder.textViewGymHours.setText("Chưa có đánh giá");
            }
        }
    }

    // Trả về tổng số item trong danh sách
    @Override
    public int getItemCount() {
        return gymList == null ? 0 : gymList.size();
    }

    // Lớp ViewHolder: Giữ các tham chiếu đến các View trong mỗi item layout.
    // Điều này giúp tránh việc phải gọi findViewById() nhiều lần, tối ưu hiệu năng.
    public static class GymViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewGym;
        TextView textViewGymName;
        TextView textViewGymAddress;
        TextView textViewGymHours; // Added TextView for hours
        ImageView imageViewDeleteGym;
        ImageView imageViewFavorite;
        LinearLayout containerGymHours; // Added LinearLayout for hours container
        RatingBar ratingBarItemAverage;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewGym = itemView.findViewById(R.id.imageViewGym);
            textViewGymName = itemView.findViewById(R.id.textViewGymName);
            textViewGymAddress = itemView.findViewById(R.id.textViewGymAddress);
            textViewGymHours = itemView.findViewById(R.id.textViewGymHours); // Map hours TextView
            imageViewDeleteGym = itemView.findViewById(R.id.imageViewDeleteGym);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
            containerGymHours = itemView.findViewById(R.id.containerGymHours); // Map hours container
            ratingBarItemAverage = itemView.findViewById(R.id.ratingBarItemAverage);
        }
    }

    // (Tùy chọn) Phương thức để cập nhật danh sách dữ liệu và thông báo cho Adapter
    public void updateData(List<Gym> newGymList) {
        this.gymList.clear(); // Xóa dữ liệu cũ
        if (newGymList != null) {
            this.gymList.addAll(newGymList); // Thêm dữ liệu mới
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi và cần vẽ lại
    }
}
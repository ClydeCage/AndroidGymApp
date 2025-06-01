package com.example.hadonggymapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class TrainerAdapter extends RecyclerView.Adapter<TrainerAdapter.TrainerViewHolder> {

    private List<Trainer> trainerList;
    private Context context;
    private OnItemClickListener listener; // Interface để xử lý sự kiện click
    private OnDeleteClickListener deleteListener; // Listener mới cho nút xóa

    // Interface để Activity có thể lắng nghe sự kiện click trên item
    public interface OnItemClickListener {
        void onItemClick(Trainer trainer); // Truyền đối tượng Trainer được click
    }

    // Interface mới cho sự kiện click nút xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(Trainer trainer); // Truyền đối tượng Trainer được xóa
    }

    // Phương thức để Activity đăng ký lắng nghe click item
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Phương thức để Activity đăng ký lắng nghe sự kiện xóa
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    // Constructor của Adapter
    public TrainerAdapter(Context context, List<Trainer> trainerList) {
        this.context = context;
        this.trainerList = trainerList;
    }

    @NonNull
    @Override
    public TrainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trainer, parent, false);
        return new TrainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainerViewHolder holder, int position) {
        Trainer currentTrainer = trainerList.get(position);

        holder.textViewTrainerName.setText(currentTrainer.getName());
        holder.textViewSpecialization.setText(currentTrainer.getSpecialization());
        holder.ratingBarTrainer.setRating(currentTrainer.getRating());

        // Load ảnh trainer nếu có
        if (currentTrainer.getImageUrl() != null && !currentTrainer.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(currentTrainer.getImageUrl())
                .centerCrop()
                .into(holder.imageViewTrainer);
        }

        // Gán sự kiện click cho toàn bộ itemView của ViewHolder (cho chỉnh sửa)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(trainerList.get(currentPosition));
                    }
                }
            }
        });

        // Gán sự kiện click cho nút xóa
        holder.imageViewDeleteTrainer.setOnClickListener(v -> {
            if (deleteListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(trainerList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trainerList == null ? 0 : trainerList.size();
    }

    public static class TrainerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewTrainer;
        TextView textViewTrainerName;
        TextView textViewSpecialization;
        RatingBar ratingBarTrainer;
        ImageView imageViewDeleteTrainer; // ImageView cho nút xóa

        public TrainerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewTrainer = itemView.findViewById(R.id.imageViewTrainer);
            textViewTrainerName = itemView.findViewById(R.id.textViewTrainerName);
            textViewSpecialization = itemView.findViewById(R.id.textViewSpecialization);
            ratingBarTrainer = itemView.findViewById(R.id.ratingBarTrainer);
            imageViewDeleteTrainer = itemView.findViewById(R.id.imageViewDeleteTrainer);
        }
    }

    // (Tùy chọn) Phương thức để cập nhật danh sách dữ liệu và thông báo cho Adapter
    public void updateData(List<Trainer> newTrainerList) {
        this.trainerList.clear(); // Xóa dữ liệu cũ
        if (newTrainerList != null) {
            this.trainerList.addAll(newTrainerList); // Thêm dữ liệu mới
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi và cần vẽ lại
    }
} 
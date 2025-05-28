package com.example.hadonggymapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<String> serviceList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private OnDeleteClickListener deleteListener;

    // Interface để Activity có thể lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(String serviceName);
    }

    // Interface để Activity có thể lắng nghe sự kiện long click
    public interface OnItemLongClickListener {
        void onItemLongClick(String serviceName);
    }

    // Interface mới cho sự kiện click nút xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(String serviceName);
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

    // Constructor
    public ServiceAdapter(List<String> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        String currentService = serviceList.get(position);
        holder.textViewServiceName.setText(currentService);

        // Gán sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(serviceList.get(currentPosition));
                }
            }
        });

        // Gán sự kiện long click
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(serviceList.get(currentPosition));
                    return true;
                }
            }
            return false;
        });

        // Gán sự kiện click cho nút xóa
        holder.imageViewDeleteService.setOnClickListener(v -> {
            if (deleteListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(serviceList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewServiceName;
        ImageView imageViewDeleteService;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewServiceName = itemView.findViewById(R.id.textViewServiceName);
            imageViewDeleteService = itemView.findViewById(R.id.imageViewDeleteService);
        }
    }

    // (Tùy chọn) Phương thức để cập nhật danh sách dữ liệu
    public void updateData(List<String> newServiceList) {
        this.serviceList = newServiceList; // Thay thế hoàn toàn danh sách cũ
        notifyDataSetChanged();
    }
} 
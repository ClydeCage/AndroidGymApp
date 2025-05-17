package com.example.hadonggymapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {

    private Context context;
    private List<Gym> gymList;
    private OnItemClickListener listener; // Interface để xử lý sự kiện click

    // Interface để Activity có thể lắng nghe sự kiện click trên item
    public interface OnItemClickListener {
        void onItemClick(Gym gym); // Truyền đối tượng Gym được click
    }

    // Phương thức để Activity đăng ký lắng nghe
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor của Adapter
    public GymAdapter(Context context, List<Gym> gymList) {
        this.context = context;
        this.gymList = gymList;
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

        holder.textViewGymName.setText(currentGym.getName());
        holder.textViewGymAddress.setText(currentGym.getAddress());

        if (currentGym.getPhone() != null && !currentGym.getPhone().isEmpty()) {
            holder.textViewGymPhone.setText(currentGym.getPhone());
            holder.textViewGymPhone.setVisibility(View.VISIBLE);
        } else {
            holder.textViewGymPhone.setVisibility(View.GONE);
        }

        // SỬ DỤNG GLIDE ĐỂ TẢI ẢNH TỪ URL
        if (currentGym.getImageUrl() != null && !currentGym.getImageUrl().isEmpty()) {
            Glide.with(context) // 'context' là Context của Activity hoặc Application
                    .load(currentGym.getImageUrl()) // URL của ảnh
                    .placeholder(R.mipmap.ic_launcher) // Ảnh hiển thị trong khi tải (tùy chọn)
                    .error(R.mipmap.ic_launcher) // Ảnh hiển thị nếu có lỗi tải (tùy chọn, tạo 1 ảnh drawable tên ic_error_placeholder)
                    .centerCrop() // Hoặc .fitCenter() tùy theo bạn muốn ảnh hiển thị thế nào
                    .into(holder.imageViewGym); // ImageView để hiển thị ảnh
        } else {
            // Nếu không có imageUrl, hiển thị ảnh mặc định
            holder.imageViewGym.setImageResource(R.mipmap.ic_launcher);
        }

        // Gán sự kiện click cho toàn bộ itemView của ViewHolder
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Lấy đúng vị trí của item được click
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) { // Đảm bảo vị trí hợp lệ
                        listener.onItemClick(gymList.get(currentPosition));
                    }
                }
            }
        });
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
        TextView textViewGymPhone;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView); // itemView là view của list_item_gym.xml đã được inflate

            // Ánh xạ các View từ layout item vào các biến Java
            imageViewGym = itemView.findViewById(R.id.imageViewGym);
            textViewGymName = itemView.findViewById(R.id.textViewGymName);
            textViewGymAddress = itemView.findViewById(R.id.textViewGymAddress);
            textViewGymPhone = itemView.findViewById(R.id.textViewGymPhone);
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
package com.example.hadonggymapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SelectableItemsAdapter extends RecyclerView.Adapter<SelectableItemsAdapter.ItemViewHolder> {

    private List<String> itemList;
    private List<String> selectedItems;

    public SelectableItemsAdapter(List<String> itemList, List<String> selectedItems) {
        this.itemList = itemList;
        this.selectedItems = new ArrayList<>(selectedItems); // Copy danh sách ban đầu
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selectable_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String currentItem = itemList.get(position);
        holder.checkBox.setText(currentItem);

        // Kiểm tra nếu mục hiện tại đã được chọn trước đó
        holder.checkBox.setChecked(selectedItems.contains(currentItem));

        // Xử lý sự kiện khi checkbox thay đổi trạng thái
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedItems.contains(currentItem)) {
                    selectedItems.add(currentItem);
                }
            } else {
                selectedItems.remove(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public List<String> getSelectedItems() {
        return selectedItems;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkboxItem);
        }
    }
} 
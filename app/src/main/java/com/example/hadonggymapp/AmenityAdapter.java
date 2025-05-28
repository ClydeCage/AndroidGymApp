package com.example.hadonggymapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private List<Amenity> amenityList;
    private Context context;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Amenity amenity);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Amenity amenity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public AmenityAdapter(Context context, List<Amenity> amenityList) {
        this.context = context;
        this.amenityList = amenityList;
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_amenity, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        Amenity currentAmenity = amenityList.get(position);
        holder.textViewAmenityName.setText(currentAmenity.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(amenityList.get(currentPosition));
                }
            }
        });

        holder.imageViewDeleteAmenity.setOnClickListener(v -> {
            if (deleteListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(amenityList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return amenityList == null ? 0 : amenityList.size();
    }

    static class AmenityViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAmenityName;
        ImageView imageViewDeleteAmenity;

        AmenityViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmenityName = itemView.findViewById(R.id.textViewAmenityName);
            imageViewDeleteAmenity = itemView.findViewById(R.id.imageViewDeleteAmenity);
        }
    }

    public void updateData(List<Amenity> newAmenityList) {
        this.amenityList.clear();
        if (newAmenityList != null) {
            this.amenityList.addAll(newAmenityList);
        }
        notifyDataSetChanged();
    }
} 
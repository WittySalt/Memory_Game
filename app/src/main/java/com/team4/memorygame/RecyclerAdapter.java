package com.team4.memorygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{
    // Responsible for displaying items in RecyclerView
    // Creates rows and maps items in list to rows
    ArrayList<Bitmap> images;
    Context context;
    HashMap<Integer, BitmapDrawable> selected;
    final private ListItemClickListener onClickListener;

    public RecyclerAdapter(Context context) {
        this.context = context;
        images = new ArrayList<>();
        selected = new HashMap<>();
        this.onClickListener = (ListItemClickListener) context;
    }

    public void addImage(String url, Bitmap bitmap) {
        images.add(bitmap);
        this.notifyItemChanged(images.size() - 1);
    }

    public void clearImages() {
        images.clear();
        selected.clear();
        this.notifyDataSetChanged();
    }

    public ArrayList<BitmapDrawable> getSelectedImages() {
        return new ArrayList<>(selected.values());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create individual rows necessary
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView imageView = holder.imageView;
        ImageView tickBox = holder.tickBox;
        imageView.setImageBitmap(images.get(position));
        if (selected.containsKey(position)) {
            tickBox.setVisibility(View.VISIBLE);
        } else {
            tickBox.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        ImageView tickBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tickBox = itemView.findViewById(R.id.tickBox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (selected.containsKey(position)) {
                selected.remove(position);
            } else {
                selected.put(position, new BitmapDrawable(context.getResources(), images.get(position)));
            }
            onClickListener.onListItemClick(position);
            notifyItemChanged(position);
        }
    }
}

package com.example.spamsnap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{
    private ArrayList<Image> allimages = new ArrayList<Image>();
    private Context context;
    public ImageAdapter (Context context,ArrayList<Image> allimages){
        this.allimages=allimages;
        this.context=context;
    }
    public class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ImageViewHolder(View itemview){
            super(itemview);
            imageView =itemview.findViewById(R.id.image);
        }
    }
    @NonNull
    @Override

    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
        Image image = allimages.get(position);

    }

    @Override
    public int getItemCount() {
        return 0;
    }

}

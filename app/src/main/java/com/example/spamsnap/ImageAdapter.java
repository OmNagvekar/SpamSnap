package com.example.spamsnap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{
    private ArrayList<Image> allimages = new ArrayList<Image>();
    private ArrayList<String> deleteImages = new ArrayList<String>();
    private Context context;
    public ImageAdapter (Context context,ArrayList<Image> allimages){
        this.allimages=allimages;
        this.context=context;
    }
    public class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView,checkicon;
        public ImageViewHolder(View itemview){
            super(itemview);
            imageView =itemview.findViewById(R.id.image);
            checkicon = itemview.findViewById(R.id.check_icon);
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
        Glide.with(context).load(image.imagepath).apply(RequestOptions.centerCropTransform()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(MainActivity.edit){
                    if (image.select){
                        holder.checkicon.setVisibility(View.INVISIBLE);
                        image.select=false;
                        deleteImages.remove(image.imagepath);
                    }else {
                        image.select=true;
                        holder.checkicon.setVisibility(View.VISIBLE);
                        deleteImages.add(image.imagepath);
                    }
                } else if (MainActivity.cancel1) {
                    for (Image image: allimages) {
                        image.select = false;
                        holder.checkicon.setVisibility(View.INVISIBLE);
                    }
                    MainActivity.cancel1=false;
                    deleteImages.clear();
                } else {
                    Intent intent = new Intent(context,ImageFullActivity.class);
                    intent.putExtra("path",image.imagepath);
                    intent.putExtra("name",image.imagename);
                    context.startActivity(intent);
                }
            }

        });
    }

    @Override
    public int getItemCount() {
        return allimages.size();
    }

}

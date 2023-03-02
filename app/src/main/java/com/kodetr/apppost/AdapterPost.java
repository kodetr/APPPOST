package com.kodetr.apppost;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class AdapterPost extends RecyclerView.Adapter<AdapterPost.FavoritViewHolder> {

    public static MClickListener nListener;
    private List<ModelPost> list;

    public AdapterPost(MClickListener listener) {
        list = new ArrayList<>();
        nListener = listener;
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public FavoritViewHolder onCreateViewHolder(ViewGroup p1, int p2) {
        return new FavoritViewHolder(LayoutInflater.from(p1.getContext()).inflate(R.layout.item_post, null));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final FavoritViewHolder holdr, int pos) {
        ModelPost mp = list.get(pos);

        Picasso.get().load(mp.getImage_url()).into(holdr.iv_image);
        holdr.desc_text.setText(mp.getNote());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void addModelPost(ModelPost op) {
        list.add(op);
        notifyDataSetChanged();
    }

    public ModelPost getModelPost(int position) {
        return list.get(position);
    }

    public static class FavoritViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView iv_image;
        TextView desc_text;

        public FavoritViewHolder(@NonNull View view) {
            super(view);
            iv_image = view.findViewById(R.id.iv_image);
            desc_text = view.findViewById(R.id.desc_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            nListener.onClick(getLayoutPosition());
        }
    }

    public interface MClickListener {
        void onClick(int position);
    }
}


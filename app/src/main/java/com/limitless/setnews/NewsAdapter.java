package com.limitless.setnews;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>{
    ArrayList<News> newslist;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener eventListener;
    private ImageView newsImage;

    public NewsAdapter(){
        FirebaseUtil.newsDatabase("news");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        newslist = FirebaseUtil.mNewsList;
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                News news = dataSnapshot.getValue(News.class);
                Log.d("News ", news.getTitle());
                news.setId(dataSnapshot.getKey());
                newslist.add(news);
                notifyItemInserted(newslist.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(eventListener);


    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.news_list_rows, parent, false);
        return new NewsViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newslist.get(position);
        holder.bind(news);

    }

    @Override
    public int getItemCount() {
        return newslist.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView news_title;
        TextView news_detail;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            news_title = itemView.findViewById(R.id.department_name);
            news_detail = itemView.findViewById(R.id.news_details);
            newsImage = itemView.findViewById(R.id.depart_logo_thumb);
            itemView.setOnClickListener(this);
        }
        public void bind(News news){
            news_title.setText(news.getTitle());
            news_detail.setText(news.getDetails());
            showImage(news.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Click", String.valueOf(position));
            News selectedNews = newslist.get(position);
            Intent intent = new Intent(view.getContext(), NewsDetail_activity.class);
            intent.putExtra("News", selectedNews);
            view.getContext().startActivity(intent);
        }
    }
    private void showImage(final String urlPath){
        if(urlPath != null && urlPath.isEmpty() == false){
            Picasso.get()
                    .load(urlPath)
                    .resize(90, 90)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(newsImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(urlPath)
                                    .resize(90, 90)
                                    .centerCrop()
                                    .into(newsImage);
                        }
                    });
        }
    }
}

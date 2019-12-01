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

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder>{
    ArrayList<Departments> departmentlist;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener eventListener;
    private ImageView depart_logo_thumb;

    public DepartmentAdapter(){
        FirebaseUtil.departmentDatabase("departments");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        departmentlist = FirebaseUtil.departmentList;
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Departments departments = dataSnapshot.getValue(Departments.class);
                Log.d("Department ", departments.getName());
                departments.setId(dataSnapshot.getKey());
                departmentlist.add(departments);
                notifyItemInserted(departmentlist.size()-1);
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
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.department_list_row, parent, false);
        return new DepartmentViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        Departments departments = departmentlist.get(position);
        holder.bind(departments);

    }

    @Override
    public int getItemCount() {
        return departmentlist.size();
    }

    public class DepartmentViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView department_name;

        public DepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            department_name = itemView.findViewById(R.id.department_name);
            depart_logo_thumb = itemView.findViewById(R.id.depart_logo_thumb);
            itemView.setOnClickListener(this);
        }
        public void bind(Departments departments){
            department_name.setText(departments.getName());
            showLogo(departments.getImageUrl());

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Click", String.valueOf(position));
            Departments selectedDepart = departmentlist.get(position);
            Intent intent = new Intent(view.getContext(), DepartmentDetail.class);
            intent.putExtra("Departments", selectedDepart);
            view.getContext().startActivity(intent);
        }
    }

    private void showLogo(final String imageUrl) {
        if(imageUrl != null && imageUrl.isEmpty() == false){
            Picasso.get()
                    .load(imageUrl)
                    .resize(45, 45)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(depart_logo_thumb, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .resize(45, 45)
                                    .centerCrop()
                                    .into(depart_logo_thumb);
                        }
                    });
        }
    }
}

package com.limitless.setnews;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    private static FirebaseUtil firebaseUtil;
    private static FirebaseAuth firebaseAuth;
    private static LogInActivity caller;
    private static MainNewsActivity caller_main;
    private static FirebaseAuth.AuthStateListener authStateListener;
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;
    public static ArrayList<News> mNewsList;
    public static ArrayList<Departments> departmentList;
    public static ArrayList<Users> usersList;
    public static boolean isAdmin;

    FirebaseUtil(){}

    public static void newsDatabase(String ref){
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            connectNewsStorage();
        }
    mNewsList = new ArrayList<News>();
    databaseReference = firebaseDatabase.getReference().child(ref);
}

    public static void departmentDatabase(String depart){
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            connectHodStorage();
        }
        departmentList = new ArrayList<Departments>();
        databaseReference = firebaseDatabase.getReference().child(depart);
    }
public static void userDatabase(String user){
    if (firebaseUtil == null) {
        firebaseUtil = new FirebaseUtil();
        firebaseDatabase = FirebaseDatabase.getInstance();
        connectUserStorage();
    }
    usersList = new ArrayList<Users>();
    databaseReference = firebaseDatabase.getReference().child(user);
}


    public static void checkAdmin(String uid){
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = firebaseDatabase.getReference().child("administrators").child(uid);
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                Log.d("CheckAdmin: ", "You're an administrator");
                //caller_main.showMenu();
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
        ref.addChildEventListener(eventListener);
    }

    public static void attachListener(){
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    public static void detachListener(){
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public static void connectNewsStorage(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("news_pictures");
    }

    public static void connectHodStorage(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("hod_pictures");
    }
    public static void connectUserStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("user_pictures");
    }


}


package com.limitless.setnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.graphics.Color.BLACK;

public class NewsDetail_activity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static  final int PICTURE_RESULT = 20;
    EditText newsTitle;
    EditText newsDetails;
    private News news;
    ImageView imageView;
    public static final int TYPE_NULL = 0;
    private FirebaseAuth.AuthStateListener stateListener;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        FirebaseUtil.newsDatabase("news");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        newsTitle = findViewById(R.id.department_name);
        newsDetails = findViewById(R.id.news_details);
        imageView = findViewById(R.id.news_image);

        Intent intent = getIntent();
        News news = (News) intent.getSerializableExtra("News");
        if (news == null) {
            news = new News();
        }
        this.news = news;
        newsTitle.setText(news.getTitle());
        newsDetails.setText(news.getDetails());
        showImage(news.getImageUrl());

        Button imgButton = findViewById(R.id.image_button);
        if(!FirebaseUtil.isAdmin){
            imgButton.setVisibility(View.INVISIBLE);
            disableEditText();
        }
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "insert picture"), PICTURE_RESULT);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkAuthState();
    }

   /* private void checkAuthState() {
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    String userUid = FirebaseAuth.getInstance().getUid();
                    FirebaseUtil.checkAdmin(userUid);
                }
            }

        };
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_activity_menu, menu);
        if(!FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_item).setVisible(false);
        }
        //else
        //    menu.findItem(R.id.save_menu).setVisible(false);
        //    menu.findItem(R.id.delete_item).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveNews();
                Toast.makeText(this, "News Posted", Toast.LENGTH_LONG).show();
                clean();
                backToNewsList();
                return  true;

            case R.id.delete_item:
                deleteNews();
                Toast.makeText(this, "News Deleted", Toast.LENGTH_LONG).show();
                backToNewsList();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            finish();
        }
        else if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        downloadUrl = task.getResult().toString();
                        //news.setImageUrl(downloadUrl);
                        //databaseReference.child(news.getId()).setValue(news);
                        showImage(downloadUrl);
                    }
                }
            });
        }
    }


    private void saveNews(){
       news.setTitle(newsTitle.getText().toString());
       news.setDetails(newsDetails.getText().toString());
       news.setImageUrl(downloadUrl);
       if(news.getId() == null) {
           databaseReference.push().setValue(news);
       }
       else{
           databaseReference.child(news.getId()).setValue(news);
       }
    }

    private void deleteNews(){
        if (news == null){
            Toast.makeText(this,"News does not exist", Toast.LENGTH_LONG).show();
        }
            databaseReference.child(news.getId()).removeValue();
        if (news.getImageName() != null && !news.getImageName().isEmpty()){
            StorageReference pictureRef = FirebaseUtil.firebaseStorage.getReference().child(news.getImageName());
            pictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image: ", "Image deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image: ", e.getMessage());
                }
            });
        }
    }

    private void backToNewsList(){
        Intent intent = new Intent(this, MainNewsActivity.class);
        startActivity(intent);
    }

    private void clean(){
        newsTitle.setText("");
        newsDetails.setText("");
        newsTitle.requestFocus();
    }
    public void showImage(final String urlPath){
        if (urlPath != null && urlPath.isEmpty() == false){
            final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(urlPath)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(urlPath)
                                    .placeholder(R.drawable.ic_action_name)
                                    .resize(width, width*2/3)
                                    .centerCrop()
                                    .into(imageView);
                        }
                    });
        }
    }
    private void disableEditText(){
        //newsTitle.setInputType(none);
        //newsDetails.setInputType(none);
        newsDetails.setEnabled(false);
        newsDetails.setTextColor(BLACK);
        newsTitle.setEnabled(false);
        newsTitle.setTextColor(BLACK);
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
    }*/
}

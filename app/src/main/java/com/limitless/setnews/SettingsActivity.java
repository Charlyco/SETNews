package com.limitless.setnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.text.TextUtils.isEmpty;

public class SettingsActivity extends AppCompatActivity { private static final int PICTURE_CODE = 123;
private static final String TAG = "SettingsActivity";
private Users users;
private EditText user_name;
private EditText user_phone;
private Spinner spinner_depart;
private ImageView profile_image;
private Button upload_button, saveButton;
private FirebaseDatabase firebaseDatabase;
private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FirebaseUtil.userDatabase("users");
        user_name = findViewById(R.id.text_name);
        user_phone = findViewById(R.id.text_phone_set);
        spinner_depart = findViewById(R.id.spinner_dept_set);
        profile_image = findViewById(R.id.prof_image_set);
        upload_button = findViewById(R.id.button_set_profle);
        saveButton = findViewById(R.id.buttonSave);
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;

        Intent intent = getIntent();
        Users users = (Users) intent.getSerializableExtra("Users");
        if(users == null){
            users = new Users();
        }
        this.users = users;
        user_name.setText(this.users.getName());
        user_phone.setText(this.users.getPhone());
        //spinner_depart.setAdapter(this.users.getDepartment());
        showImage(users.getProfile_image());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(user_name.getText().toString()) && !isEmpty(user_phone.getText().toString()))
                saveChanges();
            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent1.createChooser(intent1, "upload picture"), PICTURE_CODE);
            }
        });

        setUserDetails();

    }


    private void setUserDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("users")
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                Users users = singleSnapshot.getValue(Users.class);
                Log.d(TAG, "onDataChange: user found " + users.toString());
                user_name.setText(users.getName());
                user_phone.setText(users.getPhone());
            }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void showImage(final String image_url) {
        if(image_url != null && image_url.isEmpty() == false){
            Picasso.get()
                    .load(image_url)
                    .resize(120, 140)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(image_url)
                                    .placeholder(R.drawable.ic_action_name)
                                    .resize(120, 140)
                                    .centerCrop()
                                    .into(profile_image);
                        }
                    });
        }
    }

    private void saveChanges() {
        users.setName(user_name.getText().toString());
        users.setPhone(user_phone.getText().toString());
        users.setDepartment(spinner_depart.getSelectedItem().toString());
            databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(users);
            Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) {
            finish();
        }else if(requestCode== PICTURE_CODE && resultCode == RESULT_OK){
            Uri photoUri = data.getData();
            final StorageReference ref = FirebaseUtil.storageReference.child(photoUri.getLastPathSegment());
            ref.putFile(photoUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                        throw task.getException();

                    return ref.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        String image_url = task.getResult().toString();
                        Log.d(TAG,"URL: "+ image_url);
                        users.setProfile_image(image_url);
                        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(users);
                        showImage(image_url);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingsActivity.this,
                            "Upload Failed. Check Internet Connection",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

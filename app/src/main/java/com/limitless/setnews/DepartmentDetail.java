package com.limitless.setnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.graphics.Color.BLACK;

public class DepartmentDetail extends AppCompatActivity {
    private static final int TYPE_NULL = 0;
    private static final int LOGO_CODE = 111;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText departName, departDetails;
    private Departments departments;
    private ImageView departLogo;
    private Button uploadLogo;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_detail);
        FirebaseUtil.departmentDatabase("departments");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        departName = findViewById(R.id.text_name);
        departDetails = findViewById(R.id.depratment_details);
        departLogo = findViewById(R.id.depart_logo);
        uploadLogo = findViewById(R.id.button_logo);

            if(!FirebaseUtil.isAdmin){
                uploadLogo.setVisibility(View.INVISIBLE);
                disableEditText();

            }
        uploadLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent uploadIntent = new Intent(Intent.ACTION_GET_CONTENT);
              uploadIntent.setType("image/jpg");
              uploadIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
              startActivityForResult(uploadIntent.createChooser(uploadIntent, "Upload Logo"), LOGO_CODE);
            }
        });

        Intent intent = getIntent();
        Departments departments = (Departments) intent.getSerializableExtra("Departments");
        if (departments == null){
            departments = new Departments();
        }
        this.departments = departments;
        departName.setText(departments.getName());
        departDetails.setText(departments.getDetails());
        showLogo(departments.getImageUrl());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            finish();
        }
        else if(requestCode == LOGO_CODE && resultCode == RESULT_OK){
            Uri logoUri = data.getData();
            final StorageReference ref = FirebaseUtil.storageReference.child(logoUri.getLastPathSegment());
            ref.putFile(logoUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                        throw task.getException();
                    return ref.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        downloadUrl = task.getResult().toString();
                        //departments.setImageUrl(downloadUrl);
                        //databaseReference.child(departments.getId()).setValue(departments);
                        showLogo(downloadUrl);
                    }
                }
            });
        }
    }

    public void showLogo(final String urlPath){
        if (urlPath != null && urlPath.isEmpty() == false){
            Picasso.get()
                    .load(urlPath)
                    .resize(140, 140)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(departLogo, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(urlPath)
                                    .placeholder(R.drawable.ic_action_name)
                                    .resize(140, 140)
                                    .centerCrop()
                                    .into(departLogo);
                        }
                    });
        }
    }


    private void disableEditText() {
        //departName.setInputType(typeNull);
        //departDetails.setInputType(typeNull);
        departDetails.setEnabled(false);
        departDetails.setTextColor(BLACK);
        departName.setEnabled(false);
        departName.setTextColor(BLACK);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.department_menu, menu);
        if(!FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_depart).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_depart:
                saveDepartment();
                Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    private void saveDepartment() {
        departments.setName(departName.getText().toString());
        departments.setDetails(departDetails.getText().toString());
        departments.setImageUrl(downloadUrl);
        if (departments.getId() == null) {
            databaseReference.push().setValue(departments);
        } else {
            databaseReference.child(departments.getId()).setValue(departments);
        }
    }
}

package com.limitless.setnews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity {

    private EditText register_email;
    private EditText reg_phone, reg_user_name;
    private Spinner department_spinner;
    private EditText register_password, confirm_password;
    private Button register_button;
    private FirebaseAuth.AuthStateListener stateListener;
    private ProgressBar progressBar;
    private TextView signIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        checkAuthState();

        register_button = findViewById(R.id.register_button);
        reg_user_name = findViewById(R.id.reg_user_name);
        reg_phone = findViewById(R.id.reg_phone);
        register_email = findViewById(R.id.user_register_email);
        register_password = findViewById(R.id.user_register_pswd);
        confirm_password = findViewById(R.id.user_reg_confirm_pswd);
        department_spinner = findViewById(R.id.spinner_department);
        progressBar = findViewById(R.id.progressBar_hor);
        signIn = findViewById(R.id.sign_in_text);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isEmpty(register_email.getText().toString()) &&
                        !isEmpty(register_password.getText().toString()) &&
                !isEmpty(confirm_password.getText().toString()) &&
                        !isEmpty(reg_user_name.getText().toString())
                        && !isEmpty(reg_phone.getText().toString())){


                    if(doStringMatch(register_password.getText().toString(), confirm_password.getText().toString())){
                        Log.d("Register:", "Password match");
                        Log.d("Register", "Attempting to register");
                        registerNewUser(register_email.getText().toString(), register_password.getText().toString());
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Password do not match", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void registerNewUser(final String email, String password){
        showProgressBar();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("Register", "User successfuly registered");

                            //send verification email to user after signup
                            sendVerificationEmail();

                            Users user = new Users();
                            user.setName(reg_user_name.getText().toString());
                            user.setPhone(reg_phone.getText().toString());
                            user.setProfile_image("");
                            user.setDepartment(department_spinner.getSelectedItem().toString());
                            user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseAuth.getInstance().signOut();
                                    redirectToLoginScreen();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    FirebaseAuth.getInstance().signOut();
                                    redirectToLoginScreen();
                                    Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else if(!task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this,
                                    "Unable to register, check internet connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressBar();
                    }
                });

    }

    private void redirectToLoginScreen() {
        Log.d("Register", "redirecting to LoginAvtivity");

        Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendVerificationEmail() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Verification email sent to" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(RegisterActivity.this, "Unable to send verification email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean doStringMatch(String s1, String s2) {
        return true;
    }

    private void checkAuthState() {
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Log.d("Register", "Already has account, login instead");

                    Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
    }
}

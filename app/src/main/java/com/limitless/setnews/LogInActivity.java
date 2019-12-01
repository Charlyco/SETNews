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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 12;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;
    private EditText email, password;
    private Button SignIn;
    private ProgressBar progressBar;
    private TextView reset_password, backToRegister, resend_verification_email;

    public static final String TAG = "LogInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);
        SignIn = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        reset_password = findViewById(R.id.reset_password);
        backToRegister = findViewById(R.id.register);
        resend_verification_email = findViewById(R.id.resendEmail);

        firebaseAuth = FirebaseAuth.getInstance();

        checkAuthStatus();
        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(email.getText().toString()) &&
                        !isEmpty(password.getText().toString())) {
                    Log.d(TAG, "onClick: Trying to authenticate");

                    showProgressBar();

                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),
                                password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user.isEmailVerified()) {
                                        hideProgressBar();
                                        startActivity(new Intent(LogInActivity.this, MainNewsActivity.class));
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(LogInActivity.this, "Please verify your email before attempting to login",
                                                Toast.LENGTH_SHORT).show();
                                        hideProgressBar();
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideProgressBar();
                            }
                        });
                } else {
                        Toast.makeText(LogInActivity.this, "Enter both email and password", Toast.LENGTH_LONG).show();
                    }
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(email.getText().toString())) {
                    resetPassword(email.getText().toString());
                }
                else {
                    Toast.makeText(LogInActivity.this, "You must enter email to reset your password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

       resend_verification_email.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               resendVerificationEmail();
           }
       });
    }

    private void resendVerificationEmail() {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(LogInActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(LogInActivity.this, "Unable to send verification email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }

    private void resetPassword(String userEmail){
        FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Password reset email sent");
                            Toast.makeText(LogInActivity.this, "Check Your mail to reset your password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkAuthStatus() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Log.d("Signed in?", "onAuthStateChanged:signed_in:" + user.getEmail());

                    if (user.isEmailVerified()) {
                        startActivity(new Intent(LogInActivity.this, MainNewsActivity.class));
                        finish();
                    }
                }
                //else{
                //    startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
                //    finish();
                //}
        }
        };
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}

package com.limitless.setnews;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainNewsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth.AuthStateListener authStateListener;
    private RecyclerView recyclerViewItems;
    private LinearLayoutManager newsLayoutManager;
    private NewsAdapter newsadapter;
    private DepartmentAdapter departmentAdapter;
    private LinearLayoutManager departLayoutManager;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private static final String TAG = "MainNewsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_news);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab_add_news);
        if(!FirebaseUtil.isAdmin) {
            fab.hide();
            showMenu();
        }

        nav_drawer();

        setUpFirebase();
        setUserEmail();
        setUserDetails();
    }

    private void nav_drawer() {
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDisplayContent();
        checkAuthState();
        if(FirebaseUtil.isAdmin){
            fab.hide();
            showMenu();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainNewsActivity.this, NewsDetail_activity.class));
            }
        });
        setUserEmail();
        setUserDetails();
    }

    private void checkAuthState() {
        Log.d(TAG, "checkAuthState: Checking Authentication state");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null){
            Log.d(TAG, "CheckAuthState: User = null, Navigating back to LogInActivity");

            Intent intent = new Intent(MainNewsActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else {
            Log.d(TAG, "CheckAuthState: User authenticated");

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

    }

   private void setUpFirebase() {
       authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Log.d("Signed in?", "onAuthStateChanged:signed_in:" + user.getEmail());
                    String userUid = FirebaseAuth.getInstance().getUid();
                    FirebaseUtil.checkAdmin(userUid);
                    //User is Signed in
                }
                else {
                    Intent intent = new Intent(MainNewsActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

    }
private void setUserEmail(){
    NavigationView navigationView = findViewById(R.id.nav_view);
    View headerView = navigationView.getHeaderView(0);
    TextView userEmail = headerView.findViewById(R.id.user_email_header);
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
        String email = user.getEmail();
        Log.d("Email:", email);
        userEmail.setText(email);
    }
}
private void setUserDetails(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        final TextView userName = headerView.findViewById(R.id.user_name_header);
        final TextView userDepartment = headerView.findViewById(R.id.nav_user_department);
        final TextView userPhone = headerView.findViewById(R.id.nav_phone_number);
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    Query query = reference.child("users")
            .orderByKey()
            .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

    query.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                Log.d(TAG, "onDataChange: user found " + singleSnapshot.getValue(Users.class).toString());
                Users user = singleSnapshot.getValue(Users.class);
                userName.setText(user.getName());
                userPhone.setText(user.getPhone());
                showImage(user.getProfile_image());
                userDepartment.setText(user.getDepartment());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}

    private void showImage(final String image_path) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        final ImageView userPicture = headerView.findViewById(R.id.profile_image);
        if(image_path != null && image_path.isEmpty() == false){
            Picasso.get()
                    .load(image_path)
                    .resize(120, 140)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(userPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(image_path)
                                    .placeholder(R.drawable.ic_action_name)
                                    .resize(120, 140)
                                    .centerCrop()
                                    .into(userPicture);
                        }
                    });
        }
    }

    private void initialiseDisplayContent() {
        recyclerViewItems = findViewById(R.id.list_news_view);
        newsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        newsadapter = new NewsAdapter();

        displayNews();
    }

    private void displayNews() {
        recyclerViewItems.setLayoutManager(newsLayoutManager);
        recyclerViewItems.setAdapter(newsadapter);

        selectNavMenuItem(R.id.nav_news);
    }

    private void selectNavMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displayDepartments(){
        departLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        departmentAdapter = new DepartmentAdapter();
        recyclerViewItems.setLayoutManager(departLayoutManager);
        recyclerViewItems.setAdapter(departmentAdapter);

        selectNavMenuItem(R.id.nav_department);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_list_menu, menu);
        if(!FirebaseUtil.isAdmin) {
            menu.findItem(R.id.add_news).setVisible(false);
            menu.findItem(R.id.add_depart).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_news:
                startActivity(new Intent(MainNewsActivity.this, NewsDetail_activity.class) );
                return true;

            case R.id.add_depart:
                startActivity(new Intent(MainNewsActivity.this, DepartmentDetail.class));
                return true;

            case R.id.sign_out:
                signOut();
                return  true;

            case R.id.action_settings:
                startActivity(new Intent(MainNewsActivity.this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        Intent intent = new Intent(MainNewsActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainNewsActivity.this, NewsDetail_activity.class));
                }
            });
           displayNews();
        } else if (id == R.id.nav_department) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainNewsActivity.this, DepartmentDetail.class));
                }
            });
            displayDepartments();
        } else if (id == R.id.nav_chat_room) {
        //startActivity(new Intent(MainNewsActivity.this, ChatRoom.class));
        } else if (id == R.id.fpno_portal) {
            startActivity(new Intent(MainNewsActivity.this,PortalActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleSelection(String message) {
        View view = findViewById(R.id.list_news_view);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }

}

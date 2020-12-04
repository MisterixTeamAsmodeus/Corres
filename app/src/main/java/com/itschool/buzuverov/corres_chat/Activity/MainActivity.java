package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.itschool.buzuverov.corres_chat.Fragment.ChatsFragment;
import com.itschool.buzuverov.corres_chat.Fragment.FriendsFragment;
import com.itschool.buzuverov.corres_chat.Fragment.RequestFragment;
import com.itschool.buzuverov.corres_chat.Model.myFragment;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.User;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener {
    private User currentUser;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private FirebaseAuth auth;
    private DatabaseReference  rootUser;
    private CircularImageView userImage;
    private TextView userName, userEmail, noImage;
    private static long back_pressed;
    private static myFragment fragment = new ChatsFragment();
    private boolean emptyFragment = true;
    static boolean calledAlready = false;
    private MenuItem checkedItem;
    private NavigationView navigationView;
    private MaterialSearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initNavigationView();
        initStartFragment();
        initToolbar();
        connectAndOptionDatabase();
        SharedPreferences.Editor preferences = getSharedPreferences("OpenUserInfo", Context.MODE_PRIVATE).edit();
        preferences.putString("openedUserId","null");
        preferences.apply();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser.getId() == null){
            openLoginActivity();
        } else {
            connectAndOptionDatabase();
            checkUserDate();
            setOnline("online");
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            String token = task.getResult().getToken();
                            FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getId()).child("token").setValue(token);
                        }
                    });
            FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getId()).child("online").onDisconnect().setValue(String.valueOf(new Date().getTime()));
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN && currentUser.getId() != null)
            setOnline(String.valueOf(new Date().getTime()));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        drawerLayout.closeDrawers();
        switch (menuItem.getItemId()){
            case R.id.menu_navigation_chat:
                manager = getSupportFragmentManager();
                transaction = manager.beginTransaction();
                fragment = new ChatsFragment();
                checkedItem.setChecked(false);
                checkedItem = menuItem;
                menuItem.setChecked(true);
                transaction.replace(R.id.frame, fragment);
                transaction.commit();
                break;
            case R.id.menu_navigation_friends:
                manager = getSupportFragmentManager();
                transaction = manager.beginTransaction();
                fragment = new FriendsFragment();
                checkedItem.setChecked(false);
                checkedItem = menuItem;
                menuItem.setChecked(true);
                transaction.replace(R.id.frame, fragment);
                transaction.commit();
                break;
            case R.id.menu_navigation_requests:
                manager = getSupportFragmentManager();
                transaction = manager.beginTransaction();
                fragment = new RequestFragment();
                checkedItem.setChecked(false);
                checkedItem = menuItem;
                menuItem.setChecked(true);
                transaction.replace(R.id.frame, fragment);
                transaction.commit();
                break;
            case R.id.menu_navigation_find_friends:
                intent = new Intent(this, FindFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_navigation_setting:
                intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_navigation_logout:
                auth.signOut();
                openLoginActivity();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            back_pressed = 0;
            setOnline(String.valueOf(new Date().getTime()));
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_LONG).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setOnline(String online){
        rootUser.child("online").setValue(online);
    }

    private void checkUserDate() {
        if(Objects.requireNonNull(auth.getCurrentUser()).getEmail() != null && Objects.requireNonNull(auth.getCurrentUser().getEmail()).length() > 1){
            currentUser.setEmail(auth.getCurrentUser().getEmail());
        } else{
            currentUser.setEmail(auth.getCurrentUser().getPhoneNumber());
        }
        rootUser.keepSynced(true);
        rootUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("name")){
                    openSettingAccount();
                }
                if(dataSnapshot.hasChild("image") && dataSnapshot.child("image").getValue().toString().length() > 0){
                    currentUser.setImagePath(dataSnapshot.child("image").getValue().toString());
                }
                if(dataSnapshot.hasChild("name") && dataSnapshot.child("name").getValue().toString().length() > 0){
                    currentUser.setName(dataSnapshot.child("name").getValue().toString());
                    if(currentUser.getName().split(" ").length == 1) {
                        noImage.setText(String.valueOf(currentUser.getName().charAt(0)));
                    }
                    else {
                        String[] text = currentUser.getName().split(" ");
                        noImage.setText(String.valueOf(text[0].charAt(0)) + String.valueOf(text[1].charAt(0)));
                    }
                }

                FirebaseDatabase.getInstance().getReference().child("ChatRequest").child(currentUser.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Iterator iterator = dataSnapshot.getChildren().iterator();
                        currentUser.setRequestCount(0);
                        while (iterator.hasNext()){
                            DataSnapshot data = ((DataSnapshot)iterator.next());
                            String type = data.child("request_type").getValue().toString();
                            if(type.equals("send")){
                                currentUser.setRequestCount(currentUser.getRequestCount() + 1);
                            }


                        }
                        setUserInfo();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setUserInfo() {
        if(currentUser.getImagePath().length() > 1) {
            try {
                Glide.with(this).load(currentUser.getImagePath()).into(userImage);
            } catch (Exception ignored){}
        }
        userName.setText(currentUser.getName());
        userEmail.setText(currentUser.getEmail());
        TextView view = (TextView) navigationView.getMenu().getItem(1).getActionView();
        if (currentUser.getRequestCount() == 0){
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(String.valueOf(currentUser.getRequestCount()));
        }
    }

    private void openSettingAccount() {
        Intent intent = new Intent(MainActivity.this, SettingsNewAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void openLoginActivity() {
        if (currentUser.getId() != null)
            setOnline(String.valueOf(new Date().getTime()));
        Intent intent = new Intent(MainActivity.this, LoginPhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void connectAndOptionDatabase() {
        if (!calledAlready) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                calledAlready = true;
            } catch (Exception e){}
        }
        auth = FirebaseAuth.getInstance();
        currentUser = new User();
        currentUser.setId(auth.getUid());
        if (currentUser.getId() != null)
            rootUser = FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getId());
    }

    private void initStartFragment() {
        if (emptyFragment){
            emptyFragment = false;
            manager = getSupportFragmentManager();
            transaction = manager.beginTransaction();
            transaction.add(R.id.frame, fragment);
            transaction.commit();
            if (fragment.getClass() == ChatsFragment.class){
                checkedItem = navigationView.getMenu().getItem(0);
                checkedItem.setChecked(true);
            } else if (fragment.getClass() == RequestFragment.class){
                checkedItem = navigationView.getMenu().getItem(1);
                checkedItem.setChecked(true);
            } else if (fragment.getClass() == FriendsFragment.class){
                checkedItem = navigationView.getMenu().getItem(2);
                checkedItem.setChecked(true);
            }
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.settings_toolbar).findViewById(R.id.main_toolbar);
        searchView = findViewById(R.id.settings_toolbar).findViewById(R.id.main_toolbar_search);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
            {

                public void onDrawerClosed(View view)
                {
                    supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView)
                {
                    supportInvalidateOptionsMenu();
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            drawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }
    }

    private void initNavigationView() {
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        userImage = navigationView.getHeaderView(0).findViewById(R.id.header_image);
        userName = navigationView.getHeaderView(0).findViewById(R.id.header_profile_name);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.header_profile_email);
        noImage = navigationView.getHeaderView(0).findViewById(R.id.header_no_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        searchView.setMenuItem(menu.findItem(R.id.toolbar_menu_search));
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchViewListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fragment.onSearchViewTextChangeListener(newText);
        return false;
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {
        fragment.onSearchViewTextChangeListener("");
    }
}
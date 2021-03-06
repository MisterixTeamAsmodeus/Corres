package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ComponentCallbacks2;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itschool.buzuverov.corres_chat.Adapter.PeopleAdapter;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.User;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class NewChatActivity extends AppCompatActivity implements MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener{

    private DatabaseReference allUsers;
    private PeopleAdapter adapter;
    private TextView info;
    private ProgressBar loading;
    private RelativeLayout root;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        initialize();
    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.new_chats_toolbar).findViewById(R.id.main_toolbar);
        searchView = findViewById(R.id.new_chats_toolbar).findViewById(R.id.main_toolbar_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        String authId = FirebaseAuth.getInstance().getUid();
        root = findViewById(R.id.new_chats_root);
        loading = findViewById(R.id.new_chats_loading);
        RecyclerView recyclerView = findViewById(R.id.new_chats_recycle_list);
        info = findViewById(R.id.new_chats_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PeopleAdapter(this);
        adapter.setOpenChat();
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        allUsers = FirebaseDatabase.getInstance().getReference().child("Contact").child(authId);

        final SwipeRefreshLayout refreshLayout = findViewById(R.id.new_chats_recycle_list_updater);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void update() {
        allUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    info.setVisibility(View.INVISIBLE);
                    getUsers(dataSnapshot);
                } else{
                    loading.setVisibility(View.GONE);
                    root.setVisibility(View.VISIBLE);
                    adapter.setData(new ArrayList<User>());
                    adapter.notifyDataSetChanged();
                    info.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
            FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").setValue(String.valueOf(new Date().getTime()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").onDisconnect().setValue(String.valueOf(new Date().getTime()));
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").setValue("online");
        update();
    }

    private void getUsers(DataSnapshot dataSnapshot) {
        final ArrayList<User> users = new ArrayList<>();
        final Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            final User user = new User();
            user.setId(((DataSnapshot)iterator.next()).getKey());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(user.getId());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        user.setName(dataSnapshot.child("name").getValue().toString());
                        user.setStatus(dataSnapshot.child("status").getValue().toString());
                        user.setImagePath(dataSnapshot.child("image").getValue().toString());
                        user.setOnline(dataSnapshot.child("online").getValue().toString());

                        boolean chek = true;
                        for(int i = 0; i < users.size(); i++){
                            if(users.get(i).getId().equals(user.getId())){
                                users.set(i,user);
                                chek = false;
                                break;
                            }
                        }
                        if(chek && user.getName().trim().length() > 0)
                            users.add(user);

                        if(!iterator.hasNext()){
                            adapter.setData(users);
                            loading.setVisibility(View.GONE);
                            root.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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
        if (newText.trim().length() > 0){
            adapter.setNameSearch(newText);
        } else {
            adapter.setNameSearch("");
        }
        return false;
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {
        adapter.setNameSearch("");
    }
}


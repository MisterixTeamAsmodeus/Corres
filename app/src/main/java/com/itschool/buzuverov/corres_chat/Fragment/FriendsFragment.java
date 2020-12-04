package com.itschool.buzuverov.corres_chat.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itschool.buzuverov.corres_chat.Activity.FindFriendsActivity;
import com.itschool.buzuverov.corres_chat.Adapter.PeopleAdapter;
import com.itschool.buzuverov.corres_chat.Model.myFragment;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.User;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class FriendsFragment extends myFragment {

    private TextView message;
    private View contactView;
    private RecyclerView recyclerList;
    private DatabaseReference allUsers;
    private Context context;
    private PeopleAdapter adapter;
    private RelativeLayout root;
    private ProgressBar loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactView =  inflater.inflate(R.layout.fragment_friends, container, false);
        initUi();
        initDatabase();
        return contactView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (adapter != null)
            adapter.setContext(this.context);
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    private void initDatabase() {
        allUsers = FirebaseDatabase.getInstance().getReference().child("Contact").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        allUsers.keepSynced(true);
    }

    private void initUi(){
        root = contactView.findViewById(R.id.contacts_root);
        loading = contactView.findViewById(R.id.contacts_loading);
        message = contactView.findViewById(R.id.contacts_message);
        recyclerList = contactView.findViewById(R.id.contacts_list_friends);
        recyclerList.setLayoutManager(new LinearLayoutManager(context));
        final SwipeRefreshLayout refreshLayout = contactView.findViewById(R.id.contacts_list_friends_updater);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
                refreshLayout.setRefreshing(false);
            }
        });
        adapter = new PeopleAdapter(context);
        recyclerList.setAdapter(adapter);
        recyclerList.setItemAnimator(null);
        FloatingActionButton actionButton = contactView.findViewById(R.id.contacts_floating_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFindFriendsActivity();
            }
        });
    }

    private void openFindFriendsActivity() {
        Intent intent = new Intent(context, FindFriendsActivity.class);
        startActivity(intent);
    }

    private void update(){
        allUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    recyclerList.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);
                    getUsers(dataSnapshot);
                } else{
                    loading.setVisibility(View.GONE);
                    root.setVisibility(View.VISIBLE);
                    recyclerList.setVisibility(View.GONE);
                    adapter.setData(new ArrayList<User>());
                    adapter.notifyDataSetChanged();
                    message.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(DataSnapshot dataSnapshot) {
        final ArrayList<User> users = new ArrayList<>();
        final Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            final User user = new User();
            user.setId(((DataSnapshot)iterator.next()).getKey());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(user.getId());
            userRef.keepSynced(true);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        user.setName(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                        user.setStatus(Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString());
                        user.setImagePath(Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString());
                        user.setOnline(Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString());

                        boolean chek = true;
                        for(int i = 0; i < users.size(); i++){
                            if(users.get(i).getId().equals(user.getId())){
                                users.set(i,user);
                                chek = false;
                                break;
                            }
                        }
                        if(chek)
                            users.add(user);

                        if(!iterator.hasNext()){
                            adapter.setData(users);
                            root.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    @Override
    public void onSearchViewTextChangeListener(String newText) {
        if (newText.trim().length() > 0){
            adapter.setNameSearch(newText);
        } else {
            adapter.setNameSearch("");
        }
    }

    @Override
    public void onCloseSearchViewListener() {
        adapter.setNameSearch("");
    }

}

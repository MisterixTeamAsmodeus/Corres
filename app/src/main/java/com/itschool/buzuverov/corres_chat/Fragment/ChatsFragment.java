package com.itschool.buzuverov.corres_chat.Fragment;

import android.app.NotificationManager;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.itschool.buzuverov.corres_chat.Adapter.ChatAdapter;
import com.itschool.buzuverov.corres_chat.Model.Chat;
import com.itschool.buzuverov.corres_chat.Activity.NewChatActivity;
import com.itschool.buzuverov.corres_chat.Model.myFragment;
import com.itschool.buzuverov.corres_chat.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ChatsFragment extends myFragment {
    private TextView message;
    private View view;
    private DatabaseReference chatRef;
    private RecyclerView chatList;
    private String authUserId;
    private Context context;
    private ChatAdapter adapter;
    private ProgressBar loading;
    private RelativeLayout root;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        initDatabase();
        initUi();
        ((NotificationManager) Objects.requireNonNull(context.getSystemService(Context.NOTIFICATION_SERVICE))).cancelAll();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    private void initDatabase() {
        authUserId = FirebaseAuth.getInstance().getUid();
        if(authUserId != null)
            chatRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId);
        try {
            chatRef.keepSynced(true);
        } catch (Exception ignored){}

    }

    private void initUi(){
        root = view.findViewById(R.id.chat_fragment_root);
        loading = view.findViewById(R.id.chat_fragment_loading);
        adapter = new ChatAdapter(this, authUserId);
        message = view.findViewById(R.id.chat_fragment_message);
        chatList = view.findViewById(R.id.chat_fragment_list);
        chatList.setLayoutManager(new LinearLayoutManager(context));
        chatList.setAdapter(adapter);
        chatList.setItemAnimator(null);
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.chat_fragment_list_update);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
                refreshLayout.setRefreshing(false);
            }
        });
        FloatingActionButton actionButton = view.findViewById(R.id.chat_fragment_floating_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewChatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void update(){
        if(authUserId != null)
            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        chatList.setVisibility(View.VISIBLE);
                        message.setVisibility(View.GONE);
                        getUsers(dataSnapshot);
                    } else{
                        loading.setVisibility(View.GONE);
                        root.setVisibility(View.VISIBLE);
                        chatList.setVisibility(View.GONE);
                        adapter.setData(new ArrayList<Chat>());
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
        final Iterator iterator = dataSnapshot.getChildren().iterator();
        final ArrayList<Chat> chats = new ArrayList<Chat>();
        while (iterator.hasNext()){
            final Chat chat = new Chat();
            chat.setId(((DataSnapshot)iterator.next()).getKey());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(chat.getId());
            userRef.keepSynced(true);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        chat.setName(dataSnapshot.child("name").getValue().toString());
                        chat.setImagePath(dataSnapshot.child("image").getValue().toString());
                        chat.setOnline(dataSnapshot.child("online").getValue().toString());
                        FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(chat.getId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                chat.setNotCheckMessage(0);
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.child("status").getValue().toString().equals("notCheck")){
                                        chat.setNotCheckMessage(chat.getNotCheckMessage() + 1);
                                    }
                                }
                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(chat.getId()).limitToLast(1).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            try {
                                                DataSnapshot data = dataSnapshot.getChildren().iterator().next();
                                                chat.setLastMessage(data.child("massage").getValue().toString());
                                                chat.setLastMessageTime(data.child("date").getValue().toString());
                                                chat.setStatusMessages(data.child("status").getValue().toString());
                                                chat.setFromMessages(data.child("from").getValue().toString());
                                                chat.setLastMessageType(data.child("type").getValue().toString());
                                            } catch (Exception e){}
                                        }
                                        boolean chek = true;
                                        for(int i = 0; i < chats.size(); i++){
                                            if(chats.get(i).getId().equals(chat.getId())){
                                                chats.set(i,chat);
                                                chek = false;
                                                break;
                                            }
                                        }
                                        if(chek)
                                            chats.add(chat);
                                        if(!iterator.hasNext()){
                                            adapter.setData(chats);
                                            root.setVisibility(View.VISIBLE);
                                            loading.setVisibility(View.GONE);
                                        }
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
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

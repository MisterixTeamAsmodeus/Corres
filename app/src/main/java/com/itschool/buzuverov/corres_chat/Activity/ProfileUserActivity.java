package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileUserActivity extends AppCompatActivity {

    private String state, currentUserId, myName;
    private User openUser;
    private CircularImageView userImage;
    private TextView userName, userStatus, userOnline;
    private Button sendRequest, cancelRequestBTN;
    private DatabaseReference userRef,chatRequestRef, contactRef;
    private View main;
    private ProgressBar progressBar;
    private final String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").onDisconnect().setValue(String.valueOf(new Date().getTime()));
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").setValue("online");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
            FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").setValue(String.valueOf(new Date().getTime()));
    }

    private void initialize() {
        main = findViewById(R.id.profile_main);
        progressBar = findViewById(R.id.profile_progress);
        Toolbar toolbar = findViewById(R.id.profile_toolbar).findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("ChatRequest");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contact");
        openUser = new User();
        openUser.setId(getIntent().getExtras().get("openedUserId").toString());
        cancelRequestBTN = findViewById(R.id.profile_but_accept);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("User").child(currentUserId).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.profile_name);
        userOnline = findViewById(R.id.profile_online);
        userStatus = findViewById(R.id.profile_status);
        sendRequest = findViewById(R.id.profile_but_request);
        state = "new";
        updateInfo();
    }

    private void updateInfo(){
        userRef.child(openUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRequests();
                openUser.setName(dataSnapshot.child("name").getValue().toString());
                openUser.setStatus(dataSnapshot.child("status").getValue().toString());
                openUser.setOnline(dataSnapshot.child("online").getValue().toString());
                openUser.setImagePath(dataSnapshot.child("image").getValue().toString());
                openUser.setToken(dataSnapshot.child("token").getValue().toString());
                userName.setText(openUser.getName());
                if(openUser.getStatus().trim().length() > 1){
                    userStatus.setVisibility(View.VISIBLE);
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                }
                else
                    userStatus.setVisibility(View.GONE);
                if(openUser.getOnline().equals("online"))
                    userOnline.setText("online");
                else {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm d MMMM");
                    userOnline.setText("Был(а) в " + dateFormat.format(new Date(Long.parseLong(dataSnapshot.child("online").getValue().toString()))));
                }
                if (openUser.getImagePath().length() > 0) {
                    try {
                        Glide.with(ProfileUserActivity.this).load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile_image).into(userImage);
                    } catch (Exception e){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chatRequests() {
        chatRequestRef.child(openUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentUserId)){
                    String requestType = dataSnapshot.child(currentUserId).child("request_type").getValue().toString();
                    if(requestType.equals("send")){
                        main.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        state = "request send";
                        cancelRequestBTN.setVisibility(View.GONE);
                        sendRequest.setText("Cancel chat request");
                        sendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();
                            }
                        });
                    } else if(requestType.equals("received")){
                        state = "request received";
                        main.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        sendRequest.setText("Accept chat request");
                        sendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                acceptRequest();
                            }
                        });
                        cancelRequestBTN.setText("Denying chat request");
                        cancelRequestBTN.setVisibility(View.VISIBLE);
                        cancelRequestBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();
                            }
                        });
                    }
                } else {
                    contactRef.child(openUser.getId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(currentUserId)){
                                state = "friends";
                                main.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                cancelRequestBTN.setVisibility(View.VISIBLE);
                                cancelRequestBTN.setText("Remove");
                                cancelRequestBTN.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        removeContact();
                                    }
                                });
                                sendRequest.setText("Send message");
                                sendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        openChat();
                                    }
                                });
                            } else{
                                cancelRequestBTN.setVisibility(View.GONE);
                                state = "new";
                                main.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                sendRequest.setText("Add to friends");
                                sendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendChatRequest();
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openChat() {
        Intent intent = new Intent(getApplicationContext(), ChatsActivity.class);
        SharedPreferences.Editor editor = getSharedPreferences("OpenUserInfo",MODE_PRIVATE).edit();
        editor.putString("openedUserId", openUser.getId());
        editor.apply();
        editor.putString("openedUserName", openUser.getName());
        editor.apply();
        editor.putString("openedUserImage", openUser.getImagePath());
        editor.apply();
        startActivity(intent);
    }

    private void removeContact() {
        contactRef.child(openUser.getId()).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactRef.child(currentUserId).child(openUser.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                cancelRequestBTN.setVisibility(View.GONE);
                                state = "new";
                                sendRequest.setText("Add to friends");
                                sendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendChatRequest();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptRequest() {
        contactRef.child(openUser.getId()).child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactRef.child(currentUserId).child(openUser.getId()).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                chatRequestRef.child(openUser.getId()).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            chatRequestRef.child(currentUserId).child(openUser.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    state = "friends";
                                                    cancelRequestBTN.setVisibility(View.VISIBLE);
                                                    cancelRequestBTN.setText("Remove");
                                                    cancelRequestBTN.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            removeContact();
                                                        }
                                                    });
                                                    sendRequest.setText("Send message");
                                                    sendRequest.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            openChat();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelRequest() {
        chatRequestRef.child(openUser.getId()).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(currentUserId).child(openUser.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                cancelRequestBTN.setVisibility(View.GONE);
                                state = "new";
                                sendRequest.setText("Add to friends");
                                sendNotification(openUser.getToken(),openUser.getId(),myName,currentUserId,"cancelRequest");
                                sendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendChatRequest();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(openUser.getId()).child(currentUserId).child("request_type").setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    sendNotification(openUser.getToken(),openUser.getId(),myName,currentUserId,"newRequest");
                    chatRequestRef.child(currentUserId).child(openUser.getId()).child("request_type").setValue("received");
                }
            }
        });
    }

    public void sendNotification(String to, String openUserID, String userName,String userID, String type){
        RequestQueue mRequestQue;
        mRequestQue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {
            json.put("to",to);
            JSONObject extraData = new JSONObject();
            extraData.put("userName",userName);
            extraData.put("type",type);
            extraData.put("openUserID",openUserID);
            extraData.put("userId",userID);
            json.put("data",extraData);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAiFBlXas:APA91bGhlOkIsL6RedlLb73K0lOAqsO3vWr9Kva1qzo0fMFjN5qegtqdUMHbtq2sZvNrV0R19KZ7np_qkA0NG655uiGOGTtWwoTFt43RZoTDysMwSLGrPzvlWnhfOzyWwU6sqrniiNhR");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }

}

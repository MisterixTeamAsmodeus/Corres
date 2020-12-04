package com.itschool.buzuverov.corres_chat.Fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itschool.buzuverov.corres_chat.Adapter.RequestAdapter;
import com.itschool.buzuverov.corres_chat.Model.myFragment;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RequestFragment extends myFragment {
    private TextView message;
    private View requestView;
    private RecyclerView recyclerList;
    private DatabaseReference chatRequestRef;
    private String authUserID;
    private Context context;
    private RequestAdapter adapter;
    private RelativeLayout root;
    private ProgressBar loading;
    private final String URL = "https://fcm.googleapis.com/fcm/send";
    private String myName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requestView =  inflater.inflate(R.layout.fragment_requests, container, false);
        initDatabase();
        initUi();
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        return requestView;
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
        authUserID = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("User").child(authUserID).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("ChatRequest");
    }

    private void initUi(){
        root = requestView.findViewById(R.id.request_root);
        loading = requestView.findViewById(R.id.request_loading);
        message = requestView.findViewById(R.id.request_message);
        recyclerList = requestView.findViewById(R.id.request_list);
        recyclerList.setLayoutManager(new LinearLayoutManager(context));
        adapter = new RequestAdapter(this, authUserID);
        recyclerList.setAdapter(adapter);
        recyclerList.setItemAnimator(null);
        final SwipeRefreshLayout refreshLayout = requestView.findViewById(R.id.request_list_updater);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void update(){
        chatRequestRef.child(authUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    recyclerList.setVisibility(View.VISIBLE);
                    getUsers(dataSnapshot);
                    message.setVisibility(View.INVISIBLE);
                } else{
                    loading.setVisibility(View.GONE);
                    root.setVisibility(View.VISIBLE);
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    message.setVisibility(View.VISIBLE);
                    recyclerList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(DataSnapshot dataSnapshot) {
        final ArrayList<Request> requests = new ArrayList<>();
        final Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            final Request request = new Request();
            DataSnapshot data = ((DataSnapshot)iterator.next());
            request.setId(data.getKey());
            String type = data.child("request_type").getValue().toString();
            if(type.equals("send"))
                request.setType("send");
            else if(type.equals("received")){
                request.setType("received");
            }
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(request.getId());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        request.setName(dataSnapshot.child("name").getValue().toString());
                        request.setStatus(dataSnapshot.child("status").getValue().toString());
                        request.setImage(dataSnapshot.child("image").getValue().toString());
                        request.setToken(dataSnapshot.child("token").getValue().toString());

                        boolean chek = true;
                        for(int i = 0; i < requests.size(); i++){
                            if(requests.get(i).getId().equals(request.getId())){
                                requests.set(i,request);
                                chek = false;
                                break;
                            }
                        }
                        if(chek)
                            requests.add(request);

                        if(!iterator.hasNext()){
                            adapter.setData(requests);
                            root.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void sendNotification(String to, String openUserID, String type){
        RequestQueue mRequestQue;
        mRequestQue = Volley.newRequestQueue(context);
        JSONObject json = new JSONObject();
        try {
            json.put("to",to);
            JSONObject extraData = new JSONObject();
            extraData.put("userName",myName);
            extraData.put("type",type);
            extraData.put("openUserID",openUserID);
            extraData.put("userId",authUserID);
            json.put("data",extraData);
            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, URL,
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

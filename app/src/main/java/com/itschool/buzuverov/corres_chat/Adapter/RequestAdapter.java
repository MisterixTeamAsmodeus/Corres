package com.itschool.buzuverov.corres_chat.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.itschool.buzuverov.corres_chat.Activity.ProfileUserActivity;
import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapter;
import com.itschool.buzuverov.corres_chat.Fragment.RequestFragment;
import com.itschool.buzuverov.corres_chat.Model.Request;
import com.itschool.buzuverov.corres_chat.R;
import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Request[] allRequest, showRequest;
    private CharSequence name = "";
    private RequestFragment context;
    private String authUserID;

    public RequestAdapter(RequestFragment context, String authUserID) {
        this.allRequest = new Request[0];
        showRequest = new Request[0];
        this.inflater = LayoutInflater.from(context.getContext());
        this.context = context;
        this.authUserID = authUserID;
        name = "";
    }

    public void setData(ArrayList<Request> requests) {
        Request[] old = this.showRequest;
        this.allRequest = requests.toArray(new Request[0]);
        setShow();
        Request[] news = this.showRequest;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<Request>(old, news)).dispatchUpdatesTo(this);
        }
    }

    public void clear() {
        this.allRequest = new Request[0];
        this.showRequest = new Request[0];
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_request_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Request request = showRequest[position];
        holder.bind(request);
    }

    private void acceptRequest(final Request currentUser) {
        FirebaseDatabase.getInstance().getReference().child("Contact").child(authUserID).child(currentUser.getId()).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child("Contact").child(currentUser.getId()).child(authUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseDatabase.getInstance().getReference().child("ChatRequest").child(authUserID).child(currentUser.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance().getReference().child("ChatRequest").child(currentUser.getId()).child(authUserID).removeValue();
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

    private void cancelRequest(final Request currentUser) {
        FirebaseDatabase.getInstance().getReference().child("ChatRequest").child(authUserID).child(currentUser.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child("ChatRequest").child(currentUser.getId()).child(authUserID).removeValue();
                    context.sendNotification(currentUser.getToken(),currentUser.getId(),"cancelRequest");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return showRequest.length;
    }

    public void setNameSearch(String newText) {
        Request[] old = showRequest;
        this.name = newText;
        setShow();
        Request[] news = showRequest;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<Request>(old, news)).dispatchUpdatesTo(this);
        }
    }

    private void setShow() {
        if (name.toString().trim().length() > 0){
            List<Request> list = new ArrayList<>();
            name = name.toString().toLowerCase();
            for (Request u : allRequest){
                if (u.getName().toLowerCase().contains(name))
                    list.add(u);
            }
            showRequest = list.toArray(new Request[0]);
        } else {
            showRequest = allRequest;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final CircularImageView image;
        final TextView name, cancelMyRequest;
        final TextView accept, cancel;

        ViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.chat_display_profile_image);
            name = view.findViewById(R.id.chat_display_profile_name);
            accept = view.findViewById(R.id.request_display_accept);
            cancel = view.findViewById(R.id.request__display_cancel);
            cancelMyRequest = view.findViewById(R.id.request_display_cancel_my_request);
        }

        private void bind(Request request){
            if (request.getImage().length() > 1)
                Glide.with(context).load(request.getImage()).placeholder(R.drawable.profile_image).into(image);
            else
                Glide.with(context).load(R.drawable.profile_image).into(image);

            name.setText(request.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    String userClickId = showRequest[pos].getId();
                    Intent intent = new Intent(context.getContext(), ProfileUserActivity.class);
                    intent.putExtra("openedUserId", userClickId);
                    context.startActivity(intent);
                }
            });
            if(request.getType().equals("received")){
                cancelMyRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        cancelRequest(showRequest[pos]);
                    }
                });
                accept.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                cancelMyRequest.setVisibility(View.VISIBLE);
            } else{
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        cancelRequest(showRequest[pos]);
                    }
                });
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        acceptRequest(showRequest[pos]);
                    }
                });
            }
        }
    }
}
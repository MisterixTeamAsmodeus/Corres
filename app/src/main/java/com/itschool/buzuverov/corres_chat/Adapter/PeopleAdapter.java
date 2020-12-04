package com.itschool.buzuverov.corres_chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.itschool.buzuverov.corres_chat.Activity.ChatsActivity;
import com.itschool.buzuverov.corres_chat.Activity.ProfileUserActivity;
import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapter;
import com.itschool.buzuverov.corres_chat.Model.User;
import com.itschool.buzuverov.corres_chat.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private User[] allUser = new User[0];
    private User[] showUser;
    private CharSequence name = "";
    private Context context;
    private boolean openChat = false;

    public PeopleAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        Arrays.sort(allUser,User.NAME_COMPARATOR);
        setShow();
    }

    public void setData(ArrayList<User> users){
        User[] old = showUser;
        this.allUser = users.toArray(new User[0]);
        Arrays.sort(allUser,User.NAME_COMPARATOR);
        setShow();
        User[] news = showUser;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<User>(old, news)).dispatchUpdatesTo(this);
        }
    }

    public void setNameSearch(CharSequence name){
        User[] old = showUser;
        this.name = name;
        setShow();
        User[] news = showUser;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<User>(old, news)).dispatchUpdatesTo(this);
        }
    }

    private void setShow(){
        if (name.toString().trim().length() > 0){
            List<User> list = new ArrayList<>();
            name = name.toString().toLowerCase();
            for (User u : allUser){
                if (u.getName().toLowerCase().contains(name))
                    list.add(u);
            }
            showUser = list.toArray(new User[0]);
        } else {
            showUser = allUser;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PeopleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_friends_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PeopleAdapter.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        User currentUser = showUser[position];
        holder.bind(currentUser);
    }

    @Override
    public int getItemCount() {
        return showUser.length;
    }

    public void setOpenChat() {
        this.openChat = true;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final CircularImageView image;
        final ImageView online;
        final TextView name, status;

        ViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.friends_display_profile_image);
            name = view.findViewById(R.id.friends_display_profile_name);
            status = view.findViewById(R.id.friends_display_users_profile_status);
            online = view.findViewById(R.id.friends_display_profile_online);
        }

        private void bind(User currentUser){
            if (currentUser.getImagePath().length() > 1)
                Glide.with(context).load(currentUser.getImagePath()).placeholder(R.drawable.profile_image).into(image);
            else
                Glide.with(context).load(R.drawable.profile_image).into(image);

            if(currentUser.getOnline().equals("online"))
                online.setVisibility(View.VISIBLE);
            else
                online.setVisibility(View.INVISIBLE);

            name.setText(currentUser.getName());
            status.setText(currentUser.getStatus());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (openChat){
                        Intent intent = new Intent(context, ChatsActivity.class);
                        SharedPreferences.Editor editor = context.getSharedPreferences("OpenUserInfo", Context.MODE_PRIVATE).edit();
                        editor.putString("openedUserId", showUser[pos].getId());
                        editor.apply();
                        editor.putString("openedUserName", showUser[pos].getName());
                        editor.apply();
                        editor.putString("openedUserImage", showUser[pos].getImagePath());
                        editor.apply();
                        context.startActivity(intent);
                    } else {
                        String userClickId = showUser[pos].getId();
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        intent.putExtra("openedUserId", userClickId);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}

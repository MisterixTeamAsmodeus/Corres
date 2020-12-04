package com.itschool.buzuverov.corres_chat.Adapter;

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
import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapter;
import com.itschool.buzuverov.corres_chat.Fragment.ChatsFragment;
import com.itschool.buzuverov.corres_chat.Model.Chat;
import com.itschool.buzuverov.corres_chat.R;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Chat[] allChats;
    private Chat[] showChats;
    private CharSequence name;
    private ChatsFragment context;
    private String authUserId;

    public ChatAdapter(ChatsFragment context, String authUserId) {
        this.allChats = new Chat[0];
        this.authUserId = authUserId;
        this.inflater = LayoutInflater.from(context.getContext());
        this.context = context;
        name = "";
        setShow();
    }

    public void setData(ArrayList<Chat> chats){
        Chat[] old = showChats;
        this.allChats = chats.toArray(new Chat[0]);
        Arrays.sort(allChats,Chat.TIME_COMPARATOR);
        setShow();
        Chat[] news = showChats;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<Chat>(old, news)).dispatchUpdatesTo(this);
        }
    }

    public void setNameSearch(CharSequence name){
        Chat[] old = showChats;
        this.name = name;
        setShow();
        Chat[] news = showChats;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<Chat>(old, news)).dispatchUpdatesTo(this);
        }
    }

    private void setShow(){
        if (name.toString().trim().length() > 0){
            List<Chat> list = new ArrayList<>();
            name = name.toString().toLowerCase();
            for (Chat c : allChats){
                if (c.getName().toLowerCase().contains(name))
                    list.add(c);
            }
            showChats = list.toArray(new Chat[0]);
        } else {
            showChats = allChats;
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_chat_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Chat currentChat = showChats[position];
        holder.bind(currentChat);
    }

    @Override
    public int getItemCount() {
        return showChats.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final CircularImageView image;
        final ImageView online, status;
        final TextView name, message, times, notCheckCount;

        ViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.chat_display_profile_image);
            name = view.findViewById(R.id.chat_display_profile_name);
            message = view.findViewById(R.id.chat_display_message);
            times = view.findViewById(R.id.chat_display_time_message);
            online = view.findViewById(R.id.chat_display_profile_online);
            status = view.findViewById(R.id.chat_display_message_status);
            notCheckCount = view.findViewById(R.id.chat_display_count);
        }

        private void bind(Chat currentChat){
            if (currentChat.getImagePath().length() > 1)
                Glide.with(context).load(currentChat.getImagePath()).placeholder(null).into(image);
            else
                Glide.with(context).load(R.drawable.profile_image).into(image);

            if(currentChat.getOnline().equals("online"))
                online.setVisibility(View.VISIBLE);
            else
                online.setVisibility(View.GONE);

            if (currentChat.getFromMessages().equals(authUserId)) {
                status.setVisibility(View.VISIBLE);
                CharSequence str = "";
                switch (currentChat.getLastMessageType()) {
                    case "text":
                        str = "Вы: " + currentChat.getLastMessage();
                        break;
                    case "image":
                        str = "Вы: Изображение";
                        break;
                    case "file":
                        str = "Вы: Файл";
                        break;
                }
                message.setText(str);
                switch (currentChat.getStatusMessages()){
                    case "notSent":
                        status.setImageResource(R.drawable.ic_not_sent);
                        break;
                    case "sent":
                        status.setImageResource(R.drawable.ic_sent);
                        break;
                    case "served":
                        status.setImageResource(R.drawable.ic_served);
                        break;
                    case "check":
                        status.setImageResource(R.drawable.ic_delivered);
                        break;
                }
            } else{
                status.setVisibility(View.GONE);
                CharSequence str = "";
                switch (currentChat.getLastMessageType()) {
                    case "text":
                        str = currentChat.getName() + ": " + currentChat.getLastMessage();
                        break;
                    case "image":
                        str = currentChat.getName() + ": Изображение";
                        break;
                    case "file":
                        str = currentChat.getName() + ": Файл";
                        break;
                }
                message.setText(str);
                if (currentChat.getNotCheckMessage() == 0)
                    notCheckCount.setVisibility(View.GONE);
                else {
                    notCheckCount.setVisibility(View.VISIBLE);
                    notCheckCount.setText(String.valueOf(currentChat.getNotCheckMessage()));
                }
            }
            name.setText(currentChat.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(context.getContext(), ChatsActivity.class);
                    SharedPreferences.Editor editor = context.getContext().getSharedPreferences("OpenUserInfo",MODE_PRIVATE).edit();
                    editor.putString("openedUserId", showChats[position].getId());
                    editor.apply();
                    editor.putString("openedUserName", showChats[position].getName());
                    editor.apply();
                    editor.putString("openedUserImage", showChats[position].getImagePath());
                    editor.apply();
                    context.getContext().startActivity(intent);
                }
            });
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            try {
                String time = dateFormat.format(new Date(Long.parseLong(currentChat.getLastMessageTime())));
                times.setText(time);
            } catch (Exception ignored){}
        }
    }
}
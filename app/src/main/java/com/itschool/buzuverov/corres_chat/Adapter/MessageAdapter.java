package com.itschool.buzuverov.corres_chat.Adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.itschool.buzuverov.corres_chat.Activity.ChatsActivity;
import com.itschool.buzuverov.corres_chat.Activity.OpenImageActivity;
import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapter;
import com.itschool.buzuverov.corres_chat.Dialog.DialogDeleteMessage;
import com.itschool.buzuverov.corres_chat.Model.Messages;
import com.itschool.buzuverov.corres_chat.R;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MassageViewHolder> {
    private Messages[] showMessage;
    private Messages[] allMessage;
    private String auth;
    private String openUserId;
    private ChatsActivity activity;
    private int multiply;
    private static final int step = 50;

    public MessageAdapter(String auth, String openUserId, ChatsActivity activity) {
        this.auth = auth;
        this.openUserId = openUserId;
        setData(new ArrayList<Messages>());
        this.activity = activity;
        multiply = 1;
    }

    public void setData(List<Messages> messagesList) {
        Messages[] old = showMessage;
        allMessage = messagesList.toArray(new Messages[0]);
        setShow(allMessage);
        Messages[] news = showMessage;
        if (old != null && news != null) {
            DiffUtil.calculateDiff(new UpdaterAdapter<Messages>(old, news)).dispatchUpdatesTo(this);
        }
    }

    private void setShow(Messages[] messages) {
        if (messages.length != 0) {
            List<Messages> mes = new ArrayList<>();
            int indexFirstItem = allMessage.length - multiply * step;
            if (indexFirstItem < 0)
                indexFirstItem = 0;
            DateFormat dateFormat = new SimpleDateFormat("d MMMM");
            mes.add(messages[allMessage.length - 1]);
            for (int i = allMessage.length - 1; i > indexFirstItem; i--) {
                String t1 = dateFormat.format(new Date(Long.parseLong(messages[i].getTime())));
                String t2 = dateFormat.format(new Date(Long.parseLong(messages[i - 1].getTime())));
                if (t1.equals(t2)) {
                    mes.add(messages[i - 1]);
                } else {
                    mes.add(new Messages(dateFormat.format(new Date(Long.parseLong(messages[i].getTime()))), "info"));
                    mes.add(messages[i - 1]);
                }
            }
            if (indexFirstItem == 0){
                mes.add(new Messages(dateFormat.format(new Date(Long.parseLong(messages[0].getTime()))), "info"));
            }
            showMessage = mes.toArray(new Messages[0]);
        } else
            showMessage = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.MassageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layuot_message_display, parent, false);
        return new MessageAdapter.MassageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MassageViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        final Messages messages = showMessage[position];
        holder.bind(messages);
    }

    @Override
    public int getItemCount() {
        return showMessage.length;
    }

    public String getItemDate(int firstItem) {
        DateFormat dateFormat = new SimpleDateFormat("d MMMM");
        if (showMessage[firstItem].getType().equals("info"))
            return dateFormat.format(Long.parseLong(showMessage[firstItem + 1].getTime()));
        else
            return dateFormat.format(Long.parseLong(showMessage[firstItem].getTime()));
    }

    public int getStep() {
        return step;
    }

    public int getMultiply() {
        return multiply;
    }

    public void plusMultiply() {
        multiply++;
        setShow(allMessage);
    }

    public void addMessage(String textMessage, String type, String from, long time, String status, String id) {
        List<Messages> messages = new ArrayList<>(Arrays.asList(allMessage));
        messages.add(new Messages(from, textMessage, type, String.valueOf(time), id, status));
        activity.sendMessage = false;
        setData(messages);
    }

    private void openFile(Messages messages, Context context) {
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/Corres/Corres Documents/" + messages.getMessage());
        if (new File(Environment.getExternalStorageDirectory() + "/Corres/Corres Documents/" + messages.getMessage()).exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (messages.getMessage().lastIndexOf(".") > 0) {
                String extension = messages.getMessage().substring(messages.getMessage().lastIndexOf(".") + 1);
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (messages.getMessage().substring(messages.getMessage().lastIndexOf(".") + 1).equals("apk"))
                    intent.setDataAndType(selectedUri, "*/*");
                else
                    intent.setDataAndType(selectedUri, type);

            } else
                intent.setDataAndType(selectedUri, "*/*");

            context.startActivity(intent);
        } else {
            Toast.makeText(activity, "Скачайте файл чтобы его открыть.", Toast.LENGTH_LONG).show();
        }

    }

    private void copyMessage(String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, "Скоприованно", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Ошибка", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteMessage(final Messages messages, String type) {
        switch (type) {
            case "notMy":
                FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                break;
            case "my":
                DialogDeleteMessage dialog = new DialogDeleteMessage(new DialogDeleteMessage.DialogListener() {
                    @Override
                    public void accept(final boolean b) {
                        FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (b) {
                                    if (!messages.getType().equals("check")) {
                                        activity.sendNotification("updateMessage");
                                    }
                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(auth).child(messages.getId()).removeValue();
                                }
                            }
                        });
                    }
                });
                dialog.show(activity.getSupportFragmentManager(), "test");
                break;
            case "myFile":
                DialogDeleteMessage dialog1 = new DialogDeleteMessage(new DialogDeleteMessage.DialogListener() {
                    @Override
                    public void accept(final boolean b) {
                        FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (b) {
                                    if (messages.getMessage().lastIndexOf(".") > 0) {
                                        FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId() + messages.getMessage().substring(messages.getMessage().lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(auth).child(messages.getId()).removeValue();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        if (!messages.getType().equals("check")) {
                                            activity.sendNotification("updateMessage");
                                        }
                                        FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(auth).child(messages.getId()).removeValue();
                                                    }
                                                });
                                            }
                                        });
                                    }

                                } else {
                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(auth).child(messages.getId()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                if (dataSnapshot.hasChildren())
                                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                                            } else {
                                                if (messages.getMessage().lastIndexOf(".") > 0) {
                                                    FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId() + messages.getMessage().substring(messages.getMessage().lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                                                        }
                                                    });
                                                } else {
                                                    FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                                                        }
                                                    });
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                });
                dialog1.show(activity.getSupportFragmentManager(), "test");
                break;
            case "notMyFile":
                FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(auth).child(messages.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChildren())
                                FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                        } else {
                            if (messages.getMessage().lastIndexOf(".") > 0) {
                                FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId() + messages.getMessage().substring(messages.getMessage().lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                                    }
                                });
                            } else {
                                FirebaseStorage.getInstance().getReference().child("Any Files").child(messages.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(auth).child(openUserId).child(messages.getId()).removeValue();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
        }
    }

    private void downloadFile(Messages messages) {
        if (activity.isStoragePermissionGranted()) {
            File file = new File(Environment.getExternalStorageDirectory() + "/Corres/Corres Documents/" + messages.getMessage());
            if (file.exists()) {
                file.delete();
                DownloadManager downloadmanager = (DownloadManager) activity.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getUri()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("Corres/Corres Documents", messages.getMessage());
                downloadmanager.enqueue(request);
            } else {
                DownloadManager downloadmanager = (DownloadManager) activity.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messages.getUri()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("Corres/Corres Documents", messages.getMessage());
                downloadmanager.enqueue(request);
            }

        }
    }

    class MassageViewHolder extends RecyclerView.ViewHolder {

        TextView
                senderText,
                senderTextTime,
                senderImageTime,
                senderFile,
                senderFileTime,
                senderFileExtension,
                receiverText,
                receiverTextTime,
                receiverImageTime,
                receiverFile,
                receiverFileTime,
                receiverFileExtension,
                infoDate;

        ConstraintLayout
                senderMessageImage,
                receiverMessageImage;

        RelativeLayout
                senderMessageFile,
                receiverMessageFile;

        LinearLayout
                senderMessageText,
                receiverMessageText;

        ImageView
                senderImage,
                receiverImage,
                statusText,
                statusImage,
                statusFile;

        private DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        MassageViewHolder(@NonNull View itemView) {
            super(itemView);

            infoDate = itemView.findViewById(R.id.message_info_date);

            senderMessageText = itemView.findViewById(R.id.message_sender_layout);
            senderText = itemView.findViewById(R.id.message_senderText);
            senderTextTime = itemView.findViewById(R.id.message_senderText_time);
            statusText = itemView.findViewById(R.id.message_status);

            senderMessageImage = itemView.findViewById(R.id.message_sender_image_layout);
            senderImage = itemView.findViewById(R.id.message_sender_image);
            senderImageTime = itemView.findViewById(R.id.message_sender_image_time);
            statusImage = itemView.findViewById(R.id.message_image_status);

            senderMessageFile = itemView.findViewById(R.id.message_sender_file_layout);
            senderFile = itemView.findViewById(R.id.message_sender_file);
            senderFileTime = itemView.findViewById(R.id.message_sender_file_time);
            statusFile = itemView.findViewById(R.id.message_file_status);
            senderFileExtension = itemView.findViewById(R.id.message_sender_file_extension);

            receiverMessageText = itemView.findViewById(R.id.message_receiver_layout);
            receiverText = itemView.findViewById(R.id.message_receiver_text);
            receiverTextTime = itemView.findViewById(R.id.message_receiver_text_time);

            receiverMessageImage = itemView.findViewById(R.id.message_receiver_image_layout);
            receiverImage = itemView.findViewById(R.id.message_receiver_image);
            receiverImageTime = itemView.findViewById(R.id.message_receiver_image_time);

            receiverMessageFile = itemView.findViewById(R.id.message_receiver_file_layout);
            receiverFile = itemView.findViewById(R.id.message_receiver_file);
            receiverFileTime = itemView.findViewById(R.id.message_receiver_file_time);
            receiverFileExtension = itemView.findViewById(R.id.message_receiver_file_extension);


        }

        void bind(Messages messages) {
            infoDate.setVisibility(View.GONE);
            senderMessageText.setVisibility(View.GONE);
            senderMessageImage.setVisibility(View.GONE);
            senderMessageFile.setVisibility(View.GONE);
            receiverMessageText.setVisibility(View.GONE);
            receiverMessageImage.setVisibility(View.GONE);
            receiverMessageFile.setVisibility(View.GONE);

            switch (messages.getType()) {
                case "text":
                    bindText(messages);
                    break;
                case "file":
                    bindFile(messages);
                    break;
                case "image":
                    bindImage(messages);
                    break;
                case "info":
                    bindInfo(messages);
                    break;
            }
        }

        private void bindFile(Messages messages) {
            String fromUserId = messages.getFrom();
            String time = dateFormat.format(new Date(Long.parseLong(messages.getTime())));
            if (fromUserId.equals(auth)) {
                senderMessageFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final CharSequence[] sequence = new CharSequence[]{
                                "Копировать",
                                "Удалить",
                                "Сохранить",
                                "Открыть"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pos = getAdapterPosition();
                                switch (i) {
                                    case 0:
                                        copyMessage(showMessage[pos].getMessage());
                                        break;
                                    case 1:
                                        deleteMessage(showMessage[pos], "myFile");
                                        break;
                                    case 2:
                                        downloadFile(showMessage[pos]);
                                        break;
                                    case 3:
                                        openFile(showMessage[pos], itemView.getContext());
                                        break;

                                }
                            }
                        });
                        builder.show();
                    }
                });

                senderMessageFile.setVisibility(View.VISIBLE);
                senderFile.setText(messages.getMessage());
                if (messages.getMessage().lastIndexOf(".") > 0)
                    senderFileExtension.setText(messages.getMessage().substring(messages.getMessage().lastIndexOf(".") + 1));
                else
                    senderFileExtension.setText("");
                senderFileTime.setText(time);
                switch (messages.getStatus()) {
                    case "notSent":
                        statusFile.setImageResource(R.drawable.ic_not_sent);
                        break;
                    case "sent":
                        statusFile.setImageResource(R.drawable.ic_sent);
                        break;
                    case "served":
                        statusFile.setImageResource(R.drawable.ic_served);
                        break;
                    case "check":
                        statusFile.setImageResource(R.drawable.ic_delivered);
                        break;
                }
            } else {

                receiverMessageFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final CharSequence[] sequence = new CharSequence[]{
                                "Копировать",
                                "Удалить",
                                "Сохранить",
                                "Открыть"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pos = getAdapterPosition();
                                switch (i) {
                                    case 0:
                                        copyMessage(showMessage[pos].getMessage());
                                        break;
                                    case 1:
                                        deleteMessage(showMessage[pos], "notMyFile");
                                        break;
                                    case 2:
                                        downloadFile(showMessage[pos]);
                                        break;
                                    case 3:
                                        openFile(showMessage[pos], itemView.getContext());
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                });

                receiverMessageFile.setVisibility(View.VISIBLE);
                receiverFile.setText(messages.getMessage());
                if (messages.getMessage().lastIndexOf(".") > 0)
                    receiverFileExtension.setText(messages.getMessage().substring(messages.getMessage().lastIndexOf(".") + 1));
                else
                    receiverFileExtension.setText("");
                receiverFileTime.setText(time);
            }
        }

        private void bindImage(Messages messages) {
            String fromUserId = messages.getFrom();
            String time = dateFormat.format(new Date(Long.parseLong(messages.getTime())));
            if (fromUserId.equals(auth)) {

                senderMessageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        Intent intent = new Intent(activity, OpenImageActivity.class);
                        intent.putExtra("openedUserId", openUserId);
                        intent.putExtra("imageUri", showMessage[pos].getUri());
                        intent.putExtra("messageFrom", showMessage[pos].getFrom());
                        intent.putExtra("messageId", showMessage[pos].getId());
                        intent.putExtra("authUserId", auth);
                        intent.putExtra("message", showMessage[pos].getMessage());
                        activity.startActivity(intent);
                    }
                });

                senderMessageImage.setVisibility(View.VISIBLE);
                Glide.with(activity).load(messages.getUri()).into(senderImage);
                senderImageTime.setText(time);
                switch (messages.getStatus()) {
                    case "notSent":
                        statusImage.setImageResource(R.drawable.ic_not_sent);
                        break;
                    case "sent":
                        statusImage.setImageResource(R.drawable.ic_sent);
                        break;
                    case "served":
                        statusImage.setImageResource(R.drawable.ic_served);
                        break;
                    case "check":
                        statusImage.setImageResource(R.drawable.ic_delivered);
                        break;
                }
            } else {
                receiverMessageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        Intent intent = new Intent(activity, OpenImageActivity.class);
                        intent.putExtra("openedUserId",openUserId);
                        intent.putExtra("imageUri", showMessage[pos].getUri());
                        intent.putExtra("messageFrom", showMessage[pos].getFrom());
                        intent.putExtra("messageId", showMessage[pos].getId());
                        intent.putExtra("authUserId", auth);
                        intent.putExtra("message", showMessage[pos].getMessage());
                        activity.startActivity(intent);
                    }
                });

                receiverMessageImage.setVisibility(View.VISIBLE);
                Glide.with(activity).load(messages.getUri()).into(receiverImage);
                receiverImageTime.setText(time);
            }
        }

        private void bindText(Messages messages) {
            String fromUserId = messages.getFrom();

            String time = dateFormat.format(new Date(Long.parseLong(messages.getTime())));
            if (fromUserId.equals(auth)) {

                senderMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] sequence = new CharSequence[]{
                                "Копировать",
                                "Удалить",
                                "Редактировать"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pos = getAdapterPosition();
                                switch (i) {
                                    case 0:
                                        copyMessage(showMessage[pos].getMessage());
                                        break;
                                    case 1:
                                        deleteMessage(showMessage[pos], "my");
                                        break;
                                    case 2:
                                        activity.inputMessage.setText(showMessage[pos].getMessage());
                                        activity.inputMessage.setSelection(showMessage[pos].getMessage().length());
                                        activity.editingUserId = showMessage[pos].getId();
                                        activity.editingLayout.setVisibility(View.VISIBLE);
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                });

                senderMessageText.setVisibility(View.VISIBLE);
                senderText.setText(messages.getMessage());
                senderTextTime.setText(time);
                switch (messages.getStatus()) {
                    case "notSent":
                        statusText.setImageResource(R.drawable.ic_not_sent);
                        break;
                    case "sent":
                        statusText.setImageResource(R.drawable.ic_sent);
                        break;
                    case "served":
                        statusText.setImageResource(R.drawable.ic_served);
                        break;
                    case "check":
                        statusText.setImageResource(R.drawable.ic_delivered);
                        break;
                }
            } else {
                receiverMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] sequence = new CharSequence[]{
                                "Копировать",
                                "Удалить",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pos = getAdapterPosition();
                                switch (i) {
                                    case 0:
                                        copyMessage(showMessage[pos].getMessage());
                                        break;
                                    case 1:
                                        deleteMessage(showMessage[pos], "notMy");
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                });

                receiverMessageText.setVisibility(View.VISIBLE);
                receiverText.setText(messages.getMessage());
                receiverTextTime.setText(time);
            }

        }

        private void bindInfo(Messages messages) {
            infoDate.setVisibility(View.VISIBLE);
            infoDate.setText(messages.getMessage());
        }
    }
}
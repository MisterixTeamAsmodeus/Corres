package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itschool.buzuverov.corres_chat.Adapter.MessageAdapter;
import com.itschool.buzuverov.corres_chat.Model.DelayRan;
import com.itschool.buzuverov.corres_chat.Dialog.DialogTypeFile;
import com.itschool.buzuverov.corres_chat.Model.Messages;
import com.itschool.buzuverov.corres_chat.R;
import com.itschool.buzuverov.corres_chat.Model.User;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatsActivity extends AppCompatActivity implements DialogTypeFile.DialogListener {

    private static final String URL_SEND_NOTIFICATION = "https://fcm.googleapis.com/fcm/send";

    private User openUser;

    public String editingUserId = "null";
    private String notCheckMessageText = "";
    private String notCheckMessageId = "";
    private String authUserId, myName;

    private DelayRan delayRan;
    private DelayRan setWrite;

    public boolean
            open,
            friends = false,
            showDate = false,
            sendPrint = true,
            sendMessage = true;

    private ImageButton buttonDown;
    public EditText inputMessage;
    private TextView
            userName,
            userOnline,
            lastMessageDate;
    private CircularImageView userImage;
    private DatabaseReference rootRef;
    private MessageAdapter adapter;
    private RecyclerView userMessagesList;
    private ProgressDialog progressDialog;
    public RelativeLayout editingLayout;
    private ProgressBar loading;
    private RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        init();
        initStartPreference();
    }

    private void initStartPreference() {
        open = true;
        SharedPreferences.Editor editor = getSharedPreferences("OpenUserInfo", MODE_PRIVATE).edit();
        editor.putBoolean("openChat", open);
        editor.apply();
        showDate = false;
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Integer.parseInt(openUser.getId().replaceAll("[\\D]", "")));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initStartPreference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Contact").child(authUserId).child(openUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friends = dataSnapshot.exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("User").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("online").onDisconnect().setValue(String.valueOf(new Date().getTime()));
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("online").setValue("online");
        rootRef.child("Messages").child(authUserId).child(openUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getAllMessages(dataSnapshot);
                } else {
                    notCheckMessageText = "";
                    notCheckMessageId = "";
                    adapter.setData(new ArrayList<Messages>());
                    loading.setVisibility(View.GONE);
                    root.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference().child("Chat Status").child(authUserId).child(openUser.getId()).child("status").setValue("notPrint").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendPrint = true;
            }
        });
        open = false;
        SharedPreferences.Editor editor = getSharedPreferences("OpenUserInfo", MODE_PRIVATE).edit();
        editor.putBoolean("openChat", open);
        editor.apply();
        delayRan.stop();
        setWrite.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ALBUM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String path = data.getParcelableArrayListExtra(Define.INTENT_PATH).get(0).toString();
            uploadFile(new File(Objects.requireNonNull(getPath(Uri.parse(path)))), "image");
            SharedPreferences.Editor editor = getSharedPreferences("OpenUserInfo", MODE_PRIVATE).edit();
            editor.putBoolean("openChat", open);
            editor.apply();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private String getPath(Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        if (DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("image".equals(type)) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            selection = "_id=?";
            selectionArgs = new String[]{
                    split[1]
            };
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getApplicationContext().getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private void uploadFile(final File file, String typeFile) {
        try {
            if (typeFile.equals("image")) {
                copyFile(file, new File(Environment.getExternalStorageDirectory() + "/Corres/Corres Images/" + file.getName()));
            } else {
                copyFile(file, new File(Environment.getExternalStorageDirectory() + "/Corres/Corres Documents/" + file.getName()));
            }
        } catch (IOException e) {
            Log.i("TAG", e.getMessage());
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sending...");
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        final Date date = new Date();
        final String id = rootRef.child("Messages").child(authUserId).child(openUser.getId()).push().getKey();
        final Map<String, Object> messageText = new HashMap<>();
        messageText.put("type", typeFile);
        messageText.put("from", authUserId);
        messageText.put("status", "notSent");
        messageText.put("id", id);
        messageText.put("massage", file.getName());
        final StorageReference storageRef;
        if (typeFile.equals("image"))
            storageRef = FirebaseStorage.getInstance().getReference().child("Image Files").child(id + "." + "jpg");
        else if (file.getName().lastIndexOf(".") > 0)
            storageRef = FirebaseStorage.getInstance().getReference().child("Any Files").child(id + file.getName().substring(file.getName().lastIndexOf(".")));
        else
            storageRef = FirebaseStorage.getInstance().getReference().child("Any Files").child(id);

        storageRef.putFile(Uri.fromFile(file)).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage((int) progress + " % Uploading...");
                progressDialog.setProgress((int) progress);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    final Uri downloadUri = task.getResult();
                    messageText.put("uri", downloadUri.toString());
                    messageText.put("date", date.getTime());
                    rootRef.child("Messages").child(authUserId).child(openUser.getId()).child(id).setValue(messageText).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            inputMessage.setText("");
                            userMessagesList.smoothScrollToPosition(-5);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(authUserId).child(openUser.getId()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        messageText.put("status", "notCheck");
                                        rootRef.child("Messages").child(openUser.getId()).child(authUserId).child(id).setValue(messageText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                notCheckMessageText += "'" + file.getName() + "'";
                                                notCheckMessageId += id;
                                                sendNotification("message");
                                                rootRef.child("Messages").child(authUserId).child(openUser.getId()).child(id).child("status").setValue("sent");
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void getAllMessages(DataSnapshot dataSnapshot) {
        new GetAllMessage(this, authUserId, openUser.getId(), open).execute(dataSnapshot);
    }

    private void init() {
        delayRan = new DelayRan(1700, new Runnable() {
            @Override
            public void run() {
                lastMessageDate.setVisibility(View.GONE);
            }
        }, new Handler());

        setWrite = new DelayRan(2500, new Runnable() {
            @Override
            public void run() {
                if (!sendPrint)
                    FirebaseDatabase.getInstance().getReference().child("Chat Status").child(authUserId).child(openUser.getId()).child("status").setValue("notPrint").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendPrint = true;
                        }
                    });

            }
        }, new Handler());

        lastMessageDate = findViewById(R.id.chat_last_item_date);
        root = findViewById(R.id.chat_root);
        loading = findViewById(R.id.chat_loading);
        openUser = new User();
        SharedPreferences preferences = getSharedPreferences("OpenUserInfo", Context.MODE_PRIVATE);
        openUser.setId(preferences.getString("openedUserId", ""));
        openUser.setImagePath(preferences.getString("openedUserImage", ""));
        openUser.setName(preferences.getString("openedUserName", ""));
        FirebaseAuth auth = FirebaseAuth.getInstance();
        authUserId = auth.getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("User").child(authUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        rootRef.child("User").child(openUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        openUser.setName(dataSnapshot.child("name").getValue().toString());
                        openUser.setOnline(dataSnapshot.child("online").getValue().toString());
                        openUser.setImagePath(dataSnapshot.child("image").getValue().toString());
                        openUser.setToken(dataSnapshot.child("token").getValue().toString());

                        if (openUser.getOnline().equals("online"))
                            userOnline.setText(openUser.getOnline());
                        else {
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm d MMMM");
                            userOnline.setText("Был(а) в " + dateFormat.format(new Date(Long.parseLong(openUser.getOnline()))));
                        }
                        userName.setText(openUser.getName());
                        if (openUser.getImagePath().length() > 0)
                            try {
                                Glide.with(ChatsActivity.this).load(openUser.getImagePath()).placeholder(R.drawable.profile_image).into(userImage);
                            } catch (Exception e) {
                            }
                    } catch (Exception e) {
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        rootRef.child("Chat Status").child(openUser.getId()).child(authUserId).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getValue().toString().equals("print") && openUser.getOnline().equals("online"))
                        userOnline.setText("Печатает...");
                    else {
                        if (openUser.getOnline().equals("online"))
                            userOnline.setText(openUser.getOnline());
                        else {
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm d MMMM");
                            userOnline.setText("Был(а) в " + dateFormat.format(new Date(Long.parseLong(openUser.getOnline()))));
                        }
                    }
                } else {
                    if (openUser.getOnline().equals("online"))
                        userOnline.setText(openUser.getOnline());
                    else {
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm d MMMM");
                        userOnline.setText("Был(а) в " + dateFormat.format(new Date(Long.parseLong(openUser.getOnline()))));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        userImage = findViewById(R.id.toolbar_chat_image);
        userName = findViewById(R.id.toolbar_chat_name);
        userOnline = findViewById(R.id.toolbar_chat_online);
        ImageButton sendMessageBTN = findViewById(R.id.chat_send_message);
        inputMessage = findViewById(R.id.chat_input_message);
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (sendPrint)
                    FirebaseDatabase.getInstance().getReference().child("Chat Status").child(authUserId).child(openUser.getId()).child("status").setValue("print").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendPrint = false;
                        }
                    });
                setWrite.reset();
            }
        });

        userMessagesList = findViewById(R.id.chat_message_list);
        buttonDown = findViewById(R.id.chat_button_down);
        ImageView openFileChoser = findViewById(R.id.chat_open_chose_type);
        editingLayout = findViewById(R.id.chat_editing_layout);
        ImageButton closeEditing = findViewById(R.id.chat_editing_close);
        closeEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editingLayout.setVisibility(View.GONE);
                editingUserId = "null";
                inputMessage.setText("");
            }
        });
        openFileChoser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friends) {
                    Toast.makeText(getApplicationContext(), "Сначало добавте пользователя ы друзья.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isStoragePermissionGranted()) {
                    DialogTypeFile dialog = new DialogTypeFile();
                    dialog.show(getSupportFragmentManager(), "test");
                }
            }
        });


        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMessageDate.setVisibility(View.GONE);
                showDate = false;
                userMessagesList.smoothScrollToPosition(-5);
            }
        });

        userMessagesList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItem = -1;
                try {
                    firstItem = ((LinearLayoutManager) userMessagesList.getLayoutManager()).findFirstVisibleItemPosition();
                } catch (Exception e) {
                }
                if (sendMessage) {
                    if (showDate) {
                        lastMessageDate.setVisibility(View.VISIBLE);
                        try {
                            String time = adapter.getItemDate(((LinearLayoutManager) userMessagesList.getLayoutManager()).findLastVisibleItemPosition());
                            lastMessageDate.setText(String.valueOf(time));
                        } catch (Exception e) {
                        }
                        showDate = false;
                    } else {
                        showDate = true;
                    }
                }
                if (firstItem > 2) {
                    buttonDown.setVisibility(View.VISIBLE);
                } else {
                    buttonDown.setVisibility(View.GONE);
                }

                if (firstItem > (adapter.getStep() - 20) * adapter.getMultiply()) {
                    adapter.plusMultiply();
                }
                delayRan.reset();
            }
        });
        userName.setText(openUser.getName());
        if (openUser.getImagePath().length() > 0)
            Glide.with(this).load(openUser.getImagePath()).into(userImage);

        sendMessageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMassage();
            }
        });

        adapter = new MessageAdapter(authUserId, openUser.getId(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(adapter);
        userMessagesList.setItemAnimator(null);
    }

    private void sendMassage() {
        if (!friends) {
            Toast.makeText(getApplicationContext(), "Сначало добавте пользователя ы друзья.", Toast.LENGTH_LONG).show();
            return;
        }
        final String textMessage = inputMessage.getText().toString();
        if (textMessage.trim().length() > 0) {
            if (!sendPrint)
                FirebaseDatabase.getInstance().getReference().child("Chat Status").child(authUserId).child(openUser.getId()).child("status").setValue("notPrint").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendPrint = true;
                        setWrite.stop();
                    }
                });
            inputMessage.setText("");
            showDate = false;
            clearShowDate();
            if (editingUserId.equals("null")) {
                final Date date = new Date();
                clearShowDate();
                final Map<String, java.io.Serializable> messageText = new HashMap<String, java.io.Serializable>();
                final String id = rootRef.child("Messages").child(authUserId).child(openUser.getId()).push().getKey();
                messageText.put("id", id);
                messageText.put("massage", textMessage);
                messageText.put("type", "text");
                messageText.put("from", authUserId);
                messageText.put("date", date.getTime());
                messageText.put("status", "notSent");
                adapter.addMessage(textMessage, "text", authUserId, date.getTime(), "notSent", id);
                userMessagesList.smoothScrollToPosition(-5);
                rootRef.child("Messages").child(authUserId).child(openUser.getId()).child(id).setValue(messageText).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseDatabase.getInstance().getReference().child("Contact").child(authUserId).child(openUser.getId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    messageText.put("status", "notCheck");
                                    messageText.put("date", date.getTime());
                                    rootRef.child("Messages").child(openUser.getId()).child(authUserId).child(id).setValue(messageText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            rootRef.child("Messages").child(authUserId).child(openUser.getId()).child(id).child("date").setValue(date.getTime()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    rootRef.child("Messages").child(authUserId).child(openUser.getId()).child(id).child("status").setValue("sent");
                                                    notCheckMessageText += "'" + textMessage + "'";
                                                    notCheckMessageId += id;
                                                    sendNotification("message");
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
                sendMessage = true;
            } else {
                editingLayout.setVisibility(View.GONE);
                FirebaseDatabase.getInstance().getReference().child("Messages").child(openUser.getId()).child(authUserId).child(editingUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!editingUserId.equals("null")) {
                            if (dataSnapshot.hasChild("massage")) {
                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUser.getId()).child(editingUserId).child("massage").setValue(textMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (!editingUserId.equals("null")) {
                                            sendNotification("message");
                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(openUser.getId()).child(authUserId).child(editingUserId).child("massage").setValue(textMessage);
                                            editingUserId = "null";
                                        }
                                    }
                                });
                            } else {
                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUser.getId()).child(editingUserId).child("massage").setValue(textMessage);
                                editingUserId = "null";
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        destFile.delete();
        destFile.getParentFile().mkdirs();
        destFile.createNewFile();
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public void sendNotification(String type) {
        if (adapter.getItemCount() == 0 && type.equals("updateMessage")) {
            notCheckMessageText = "";
            notCheckMessageId = "";
        }
        RequestQueue mRequestQue;
        mRequestQue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {
            json.put("to", openUser.getToken());
            JSONObject extraData = new JSONObject();
            extraData.put("messageText", notCheckMessageText);
            extraData.put("userName", myName);
            extraData.put("type", type);
            extraData.put("messageId", notCheckMessageId);
            extraData.put("userId", authUserId);
            json.put("data", extraData);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SEND_NOTIFICATION,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: " + error.networkResponse);
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAAiFBlXas:APA91bGhlOkIsL6RedlLb73K0lOAqsO3vWr9Kva1qzo0fMFjN5qegtqdUMHbtq2sZvNrV0R19KZ7np_qkA0NG655uiGOGTtWwoTFt43RZoTDysMwSLGrPzvlWnhfOzyWwU6sqrniiNhR");
                    return header;
                }
            };
            mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getType(String type) {
        if (type.equals("image")) {
            FishBun.with(this)
                    .setImageAdapter(new GlideAdapter())
                    .setMaxCount(1)
                    .setAlbumSpanCount(1, 2)
                    .setAllViewTitle("All photos")
                    .setActionBarTitle("Select your photo")
                    .textOnImagesSelectionLimitReached("You can't select any more.")
                    .textOnNothingSelected("I need a photo!")
                    .startAlbum();

        } else if (type.equals("file")) {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.root = new File(Environment.getExternalStorageDirectory().getPath());
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = new File(DialogConfigs.DEFAULT_DIR);
            properties.extensions = null;
            properties.show_hidden_files = false;
            FilePickerDialog dialog = new FilePickerDialog(this, properties);
            dialog.setTitle("Select a File");
            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    uploadFile(new File(files[0]), "file");
                }
            });
            dialog.show();

        }
    }

    public void clearShowDate() {
        lastMessageDate.setVisibility(View.GONE);
        showDate = false;
    }

    public void scroll() {
        clearShowDate();
        root.setVisibility(View.VISIBLE);
        int firstItem = -1;
        try {
            firstItem = ((LinearLayoutManager) userMessagesList.getLayoutManager()).findFirstVisibleItemPosition();
        } catch (Exception e) {
        }
        if (firstItem < 2 && firstItem != -1) {
            userMessagesList.smoothScrollToPosition(-5);
        }
    }

    private void setNotificationData(String notCheckMessageText, String notCheckMessageId) {
        this.notCheckMessageId = notCheckMessageId;
        this.notCheckMessageText = notCheckMessageText;
    }

    private void updateDataInAdapter(List<Messages> messages) {
        clearShowDate();
        adapter.setData(messages);
        loading.setVisibility(View.GONE);
        scroll();
    }

    private static class GetAllMessage extends AsyncTask<DataSnapshot, Void, List<Messages>> {

        private ChatsActivity activity;
        private String notCheckMessageText = "", notCheckMessageId = "";
        private String authUserId;
        private String openUserId;
        private Boolean status;

        private GetAllMessage(ChatsActivity activity, String authUserId, String openUserId, Boolean status) {
            this.activity = activity;
            this.authUserId = authUserId;
            this.openUserId = openUserId;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            activity.clearShowDate();
            activity.setNotificationData(notCheckMessageText, notCheckMessageId);
        }

        @Override
        protected void onPostExecute(List<Messages> messeges) {
            activity.setNotificationData(notCheckMessageText, notCheckMessageId);
            activity.clearShowDate();
            activity.updateDataInAdapter(messeges);
        }

        @Override
        protected List<Messages> doInBackground(DataSnapshot... dataSnapshots) {
            List<Messages> messagesList = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshots[0].getChildren()) {
                final Messages messages = new Messages();
                if (snapshot.exists()) {
                    try {
                        if (snapshot.hasChild("uri"))
                            messages.setUri(snapshot.child("uri").getValue().toString());
                        messages.setMessage(snapshot.child("massage").getValue().toString());
                        messages.setType(snapshot.child("type").getValue().toString());
                        messages.setFrom(snapshot.child("from").getValue().toString());
                        messages.setTime(snapshot.child("date").getValue().toString());
                        messages.setId(snapshot.child("id").getValue().toString());
                        messages.setStatus(snapshot.child("status").getValue().toString());
                        if (!messages.getStatus().equals("check") && !messages.getStatus().equals("notSent")) {
                            notCheckMessageText += "'" + messages.getMessage() + "'" + (char) (1);
                            notCheckMessageId += messages.getId() + " ";
                        }
                        if (messages.getStatus().equals("notCheck") && status) {
                            FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messages.getId()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.hasChild("massage")) {
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messages.getId()).child("status").setValue("check");
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messages.getId()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists() && dataSnapshot.hasChild("massage"))
                                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messages.getId()).child("status").setValue("check");
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
                        messagesList.add(messages);

                    } catch (Exception e) {
                    }
                }
            }
            return messagesList;
        }
    }

}

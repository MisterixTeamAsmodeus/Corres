package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.diegocarloslima.byakugallery.lib.TouchImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.itschool.buzuverov.corres_chat.Dialog.DialogDeleteMessage;
import com.itschool.buzuverov.corres_chat.R;

import java.io.File;

public class OpenImageActivity extends AppCompatActivity {

    String uri, messageId, authUserId, openUserId, messageFrom, message;
    ProgressBar loading;
    TouchImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_image);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.parseColor("#000000"));
        uri = getIntent().getExtras().get("imageUri").toString();
        messageId = getIntent().getExtras().get("messageId").toString();
        authUserId = getIntent().getExtras().get("authUserId").toString();
        openUserId = getIntent().getExtras().get("openedUserId").toString();
        messageFrom = getIntent().getExtras().get("messageFrom").toString();
        message = getIntent().getExtras().get("message").toString();

        Toolbar toolbar = findViewById(R.id.open_image_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });


        loading = findViewById(R.id.open_image_progress);
        image = findViewById(R.id.open_image_image);
        Glide.with(this).load(uri).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                loading.setVisibility(View.GONE);
                return false;
            }
        }).into(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_image_save:
                downloadFile();
                break;
            case R.id.menu_image_delete:
                if(messageFrom.equals(authUserId))
                    deleteMessage("myImage");
                else
                    deleteMessage("notMyImage");
                break;
        }
        return true;
    }

    private void downloadFile(){
        if (isStoragePermissionGranted()){
            File file = new File(Environment.getExternalStorageDirectory() + "/Corres/Corres Images/" + message);
            if (file.exists()){
                file.delete();
                DownloadManager downloadmanager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir( "Corres/Corres Images", message);
                downloadmanager.enqueue(request);
            } else {
                DownloadManager downloadmanager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir( "Corres/Corres Images", message);
                downloadmanager.enqueue(request);
            }

        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private void deleteMessage(String type){
        switch (type) {
            case "myImage":
                DialogDeleteMessage dialog1 = new DialogDeleteMessage(new DialogDeleteMessage.DialogListener() {
                    @Override
                    public void accept(final boolean b) {
                        FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(openUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(b){
                                    if (message.lastIndexOf(".") > 0){
                                        FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId + message.substring(message.lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }

                                } else {
                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messageId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                if (dataSnapshot.hasChildren())
                                                    FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue();
                                            } else {
                                                if (message.lastIndexOf(".") > 0){
                                                    FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId + message.substring(message.lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue();
                                                        }
                                                    });
                                                } else {
                                                    FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue();
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
                dialog1.show(getSupportFragmentManager(), "test");
                break;
            case "notMyImage":
                FirebaseDatabase.getInstance().getReference().child("Messages").child(openUserId).child(authUserId).child(messageId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if (dataSnapshot.hasChildren())
                                FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                    }
                                });
                        } else {
                            if (message.lastIndexOf(".") > 0){
                                FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId + message.substring(message.lastIndexOf("."))).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } else {
                                FirebaseStorage.getInstance().getReference().child("Image Files").child(messageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("Messages").child(authUserId).child(openUserId).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                finish();
                                            }
                                        });
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

}

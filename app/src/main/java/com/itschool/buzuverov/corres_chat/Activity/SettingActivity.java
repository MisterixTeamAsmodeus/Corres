package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itschool.buzuverov.corres_chat.R;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.Date;

public class SettingActivity extends AppCompatActivity {

    private EditText userName, userStatus;
    private CircularImageView userImage;
    private String userId;
    private DatabaseReference root;
    private static final int GALLERY_CODE = 1;
    private StorageReference userImageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initProgressDialog();
        iniToolbar();
        initDatabase();
        initUi();
        updateInfo();
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

    private void iniToolbar(){
        Toolbar toolbar = findViewById(R.id.settings_toolbar).findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initDatabase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userImageRef = FirebaseStorage.getInstance().getReference().child("imageProfile");
        userId = auth.getCurrentUser().getUid();
        root = FirebaseDatabase.getInstance().getReference();
    }

    private void initUi() {
        Button updateAccount = findViewById(R.id.setting_update);
        userImage = findViewById(R.id.setting_user_image);
        userName = findViewById(R.id.setting_user_name);
        userStatus = findViewById(R.id.setting_user_status);
        TextView dontUpdate = findViewById(R.id.settings_not_update);

        dontUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dintUpdateInfo();
            }
        });
        updateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galery = new Intent();
                galery.setAction(Intent.ACTION_GET_CONTENT);
                galery.setType("image/*");
                startActivityForResult(galery, GALLERY_CODE);
            }
        });
    }

    private void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Set profile image");
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null){
            Uri image = data.getData();
            CropImage.activity(image).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                progressDialog.show();

                final StorageReference filePath = userImageRef.child(userId + ".jpg");
                final UploadTask uploadTask = filePath.putFile(resultUri);
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){


                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    return filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        final String dowloadUti = task.getResult().toString();
                                        filePath.child(userId + ".jpg").getDownloadUrl().toString();

                                        root.child("User").child(userId).child("image").setValue(dowloadUti).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
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
    }

    private void updateSettings() {
        final String
                name = userName.getText().toString(),
                status = userStatus.getText().toString();

        if(name.length() > 0){
            root.child("User").child(userId).child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    root.child("User").child(userId).child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }
            });


        }
    }

    private void updateInfo(){
        root.child("User").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userName.setText(dataSnapshot.child("name").getValue().toString());
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    try {
                        Glide.with(SettingActivity.this).load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile_image).into(userImage);
                    } catch (Exception e){
                        try {
                            Glide.with(SettingActivity.this).load(R.drawable.profile_image).into(userImage);
                        } catch (Exception e1){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void dintUpdateInfo() {
        finish();
    }
}

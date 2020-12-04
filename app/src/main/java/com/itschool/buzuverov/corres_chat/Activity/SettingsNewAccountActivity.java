package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itschool.buzuverov.corres_chat.R;
import java.util.HashMap;

public class SettingsNewAccountActivity extends AppCompatActivity {

    private TextInputEditText userName, userStatus;
    private String userId;
    private DatabaseReference root;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        setContentView(R.layout.activity_settungs_new_account);
        initUi();
        iniToolbar();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            back_pressed = 0;
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_LONG).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    private void iniToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_new_account_toolbar).findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initDatabase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        root = FirebaseDatabase.getInstance().getReference();
    }

    private void initUi() {
        Button updateAccount = findViewById(R.id.setting_new_account_update);
        userName = findViewById(R.id.setting_new_account_user_name);
        userStatus = findViewById(R.id.setting_new_account_user_status);
        updateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });
    }

    private void updateSettings() {
        String
                name = userName.getText().toString(),
                statys = userStatus.getText().toString();

        if(name.length() > 0){
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("id",userId);
            hashMap.put("name",name);
            hashMap.put("image", "");
            hashMap.put("status",statys);
            root.child("User").child(userId).setValue(hashMap).addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendUserMain();
                            }
                        }
                    }
            );
        }
    }

    private void sendUserMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

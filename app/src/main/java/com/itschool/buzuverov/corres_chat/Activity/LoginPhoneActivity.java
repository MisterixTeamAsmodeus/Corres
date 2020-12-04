package com.itschool.buzuverov.corres_chat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itschool.buzuverov.corres_chat.R;

import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private Button checkCodeButton;
    private TextInputEditText inputPhone, inputCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog progressDialog;
    private TextInputLayout inputCodeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
        initUi();
        initDatabase();
        iniToolbar();
    }

    private void iniToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.login_phone_toolbar).findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initUi() {
        inputCodeLayout = findViewById(R.id.login_phone_input_layout_code);
        Button sendCodeButton = findViewById(R.id.login_phone_send_code_buttons);
        checkCodeButton = findViewById(R.id.login_phone_check_code_buttons);
        inputPhone = findViewById(R.id.login_phone_input_phone);
        inputCode = findViewById(R.id.login_phone_input_code);
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });
        inputCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputCode();
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInPhoneCode(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                inputCode.setEnabled(false);
                checkCodeButton.setEnabled(false);
                inputCodeLayout.setVisibility(View.GONE);
                checkCodeButton.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressDialog.dismiss();
                LoginPhoneActivity.this.verificationId = verificationId;
                mResendToken = token;
            }
        };

        progressDialog = new ProgressDialog(this);
    }

    private void inputCode() {
        String code = inputCode.getText().toString();
        if (code.trim().length() > 0) {
            progressDialog.setTitle("Code verification");
            progressDialog.setMessage("Please wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInPhoneCode(credential);
        }
    }

    private void sendCode() {
        String phoneNumber = inputPhone.getText().toString();
        if (phoneNumber.trim().length() > 0) {
            if (phoneNumber.charAt(0) == '8') {
                phoneNumber = "+7" + phoneNumber.substring(1);
            }
            inputCode.setEnabled(true);
            checkCodeButton.setEnabled(true);
            inputCodeLayout.setVisibility(View.VISIBLE);
            checkCodeButton.setVisibility(View.VISIBLE);
            progressDialog.setTitle("Phone verification");
            progressDialog.setMessage("Please wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, 60, TimeUnit.SECONDS, LoginPhoneActivity.this, callbacks);
        }
    }

    private void initDatabase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    private void signInPhoneCode(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String currentUserId = auth.getCurrentUser().getUid();
                    database = FirebaseDatabase.getInstance().getReference().child("User").child(currentUserId);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists() && !dataSnapshot.hasChildren()) {
                                database.setValue("");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    progressDialog.dismiss();
                    openMainActivity();
                }
            }
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
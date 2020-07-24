package com.allenbenny.sender;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private Button registerButton, PhoneRegisterButton, alreadyregisteredButton;
    private TextInputEditText userEmail, userPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootReference = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        alreadyregisteredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }

    private void CreateNewAccount() {

        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }
        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while we do the Work...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUserID = mAuth.getCurrentUser().getUid();
                        RootReference.child("Users").child(currentUserID).setValue("");

                        RootReference.child("Users").child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Created Account Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        });

                    }
                    else{
                        String message = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void InitializeFields() {

        registerButton = (Button) findViewById(R.id.register_button);
        PhoneRegisterButton = (Button) findViewById(R.id.phone_register_button);
        alreadyregisteredButton = (Button) findViewById(R.id.already_have_account_link);

        userEmail = (TextInputEditText) findViewById(R.id.register_email);
        userPassword = (TextInputEditText) findViewById(R.id.register_password);

        loadingBar = new ProgressDialog(RegisterActivity.this);

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

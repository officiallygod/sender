package com.allenbenny.sender;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private TextInputLayout InputPhoneNumberBox, InputVerificationCodeBox;
    private EditText InputPhoneNumber, InputVerificationCode;
    private Button sendVerificationCode, VerifyButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    ProgressDialog loadingBar;
    public int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCode = (Button) findViewById(R.id.send_verification_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        InputPhoneNumberBox = (TextInputLayout) findViewById(R.id.phone_number_input_box);
        InputVerificationCodeBox = (TextInputLayout) findViewById(R.id.verification_code_input_box);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = InputPhoneNumber.getText().toString();
                Log.v("Allen", phoneNumber);
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter your Phone No.", Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait while we Authenticate");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode.setEnabled(false);
                InputPhoneNumberBox.setEnabled(false);

                String verificationCode = InputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Input Verification Code first ", Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait while we verify your Code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone No.! Please enter correct Country Code", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setEnabled(true);
                InputPhoneNumberBox.setEnabled(true);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCodeBox.setVisibility(View.INVISIBLE);
            }
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.dismiss();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Verification Code has been sent ", Toast.LENGTH_SHORT).show();
                counter = 0;
                sendVerificationCode.setEnabled(false);
                InputPhoneNumberBox.setEnabled(false);
                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCodeBox.setVisibility(View.VISIBLE);
                countdowntimer();
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                loadingBar.dismiss();
                                Toast.makeText(PhoneLoginActivity.this, "You have been Logged In", Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                        }
                        else {
                            String messsage = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + messsage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void countdowntimer() {
        final MaterialTextView counttime =  findViewById(R.id.Timer_otp);
        counttime.setVisibility(View.VISIBLE);
        new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeleft = String.valueOf(counter) + " sec";
                counttime.setText(timeleft);
                counter++;
            }
            @Override
            public void onFinish() {
                counttime.setVisibility(View.INVISIBLE);
                sendVerificationCode.setText("TRY AGAIN");
                sendVerificationCode.setEnabled(true);
            }
        }.start();
    }


}

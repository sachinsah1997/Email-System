package com.sachinsah.emailsystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.chaos.view.PinView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shuhart.stepview.StepView;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    LinearLayout layout1, layout2, layout3;
    StepView stepView;
    private int currentStep = 0;
    AlertDialog dialog_verifying;
    private String phoneNumber;
    private EditText phoneNumberEditText;
    private PinView verifyCodeET;
    private TextView phoneNumberTextView;
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_Phone_Auth);

        mAuth = FirebaseAuth.getInstance();

        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);

        Button generateOtpButton = findViewById(R.id.submit1);
        Button verifyOtpButton = findViewById(R.id.submit2);
        Button successOtpVerifiedButton = findViewById(R.id.submit3);
        TextView reSendOtpTextView = findViewById(R.id.resend_otp);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        verifyCodeET = findViewById(R.id.pinView);
        phoneNumberTextView = findViewById(R.id.phone_number_text_view);


        stepView = findViewById(R.id.step_view);
        stepView.setStepsNumber(2);
        stepView.go(0, true);

        layout1.setVisibility(View.VISIBLE);

        generateOtpButton.setOnClickListener(view -> {

            phoneNumber = phoneNumberEditText.getText().toString();
            phoneNumberTextView.setText(phoneNumber);

            if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10) {
                phoneNumberEditText.setError("Enter a Phone Number");
                phoneNumberEditText.requestFocus();
            } else {
                if (currentStep < stepView.getStepCount() - 1) {
                    currentStep++;
                    stepView.go(currentStep, true);
                } else {
                    stepView.done(true);
                }
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);


                phoneNumber = "+91" + phoneNumberEditText.getText().toString().trim();
                startPhoneNumberVerification(phoneNumber);                 }
        });

        //mCallBack started
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                // 1 - Instant verification 2 - Auto-retrieval.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid Number",
                            Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        //end of mCallBacks

        verifyOtpButton.setOnClickListener(view -> {
            String verificationCode = verifyCodeET.getText().toString();
            if (verificationCode.isEmpty()) {
                Toast.makeText(PhoneAuthActivity.this, "Enter verification code", Toast.LENGTH_SHORT).show();
            } else {

                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.processing_dialog, null);
                AlertDialog.Builder show = new AlertDialog.Builder(PhoneAuthActivity.this);

                show.setView(alertLayout);
                show.setCancelable(false);
                dialog_verifying = show.create();
                dialog_verifying.show();

                verifyPhoneNumberWithCode(mVerificationId, verificationCode);
            }
        });

        successOtpVerifiedButton.setOnClickListener(view -> {
            if (currentStep < stepView.getStepCount() - 1) {
                currentStep++;
                stepView.go(currentStep, true);
            } else {
                stepView.done(true);
            }

            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.GONE);
            layout3.setVisibility(View.GONE);
        });

        reSendOtpTextView.setOnClickListener(v -> resendVerificationCode(phoneNumber, mResendToken));
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        Log.d(TAG, "signInWithCredential:success");

                        dialog_verifying.dismiss();

                        Snackbar.make(findViewById(android.R.id.content), "Login Successfully :)",
                                Snackbar.LENGTH_SHORT).show();
                        layout1.setVisibility(View.GONE);
                        layout2.setVisibility(View.GONE);
                        layout3.setVisibility(View.VISIBLE);


                    } else {

                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            Snackbar.make(findViewById(android.R.id.content), "Invalid Number",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
        }

    }
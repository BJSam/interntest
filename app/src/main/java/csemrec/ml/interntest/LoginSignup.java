package csemrec.ml.interntest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginSignup extends AppCompatActivity {
private Button btnSendOtp,btnVerifyOTP;
private EditText etPhoneNum,etCustOpt;
private String mVerificationId;
private PhoneAuthCredential credential;
private FirebaseAuth mAuth;
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        btnSendOtp = findViewById(R.id.btnSendotp);
        btnVerifyOTP = findViewById(R.id.btnSignOTPVerify);
        etPhoneNum = findViewById(R.id.etNum);
        etCustOpt = findViewById(R.id.etOTPVerify);
        mAuth=FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                Toast.makeText(LoginSignup.this, "Verification number Sent, Verify it and Login/Signup", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }
        };
        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Phonenum = etPhoneNum.getText().toString().trim();
                if(Phonenum.length() !=10){
                    Toast.makeText(LoginSignup.this, "Enter Correct Phone Number", Toast.LENGTH_SHORT).show();
                }else{

                   sendCode(Phonenum);
                }


            }
        });
        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etCustOpt.getText().toString().trim();
                if(otp.length()!=6){
                    Toast.makeText(LoginSignup.this, "Please enter correct 6 digit OTP", Toast.LENGTH_SHORT).show();
                }
                else {
                     credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //verification successful we will start the profile activity
                    Intent intent = new Intent(LoginSignup.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {

                    //verification unsuccessful.. display an error message
                    Toast.makeText(LoginSignup.this, "Somthing is wrong, we will fix it soon...", Toast.LENGTH_SHORT).show();


                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginSignup.this, "Invalid Code Entered", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void sendCode(String phonenum) {
       String pnnum = "+91"+phonenum;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                pnnum,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

}

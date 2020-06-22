package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmailInput, userPasswordInput;
    private TextView goToRegBtn;
    private Button userloginBtn;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String myId, passHide = "hide";
    private ImageView passwordVisibility;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        userEmailInput = (EditText)findViewById(R.id.user_email_input);
        userPasswordInput = (EditText)findViewById(R.id.user_password_input);
        goToRegBtn = (TextView) findViewById(R.id.go_to_reg_btn);
        userloginBtn = (Button) findViewById(R.id.user_login_btn);
        passwordVisibility = (ImageView)findViewById(R.id.password_visibility);

        userloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        userPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent!=null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)){
                    userloginBtn.performClick();
                }
                return false;
            }
        });

        goToRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        passwordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passHide.equals("hide")){
                    userPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisibility.setImageResource(R.drawable.ic_visibility);
                    passHide = "show";
                }else{
                    userPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisibility.setImageResource(R.drawable.ic_visibility_off);
                    passHide = "hide";
                }
            }
        });
    }

    private void userLogin() {

        final String email = userEmailInput.getText().toString().trim();
        final String password = userPasswordInput.getText().toString().trim();

        if (!email.equals("") && !password.equals("")) {

            loadingBar.setTitle("Account Login");
            loadingBar.setMessage("Please wait, while we checking credentials...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "You are login successfully", Toast.LENGTH_SHORT).show();

                        myId = FirebaseAuth.getInstance().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken().toString();
                        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(myId).child("device_token");
                        userRef.setValue(deviceToken);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("device_token", deviceToken);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Error, try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            Toast.makeText(LoginActivity.this, "Please, compile the login form", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

}

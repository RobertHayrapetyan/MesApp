package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    private Button registerBtn;
    private EditText userFirstName, userLastName, userPhone, userEmail, userAddress, userPassword;
    private ImageView userImage;
    private FirebaseAuth mAuth;
    private StorageReference storageProfilePictureRef;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;
    private String checker = "";
    private Uri imageUri;
    private String myUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        loadingBar = new ProgressDialog(this);

        userFirstName = (EditText)findViewById(R.id.user_first_name);
        userLastName = (EditText)findViewById(R.id.user_last_name);
        userPhone = (EditText)findViewById(R.id.user_phone_number);
        userEmail = (EditText)findViewById(R.id.user_email);
        userAddress = (EditText)findViewById(R.id.user_address);
        userPassword = (EditText)findViewById(R.id.user_password);
        userImage = (ImageView) findViewById(R.id.user_image);

        registerBtn = (Button)findViewById(R.id.user_register_btn);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";

                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .start(RegistrationActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            userImage.setImageURI(imageUri);
        }else {
            Toast.makeText(RegistrationActivity.this, "Error: Try Again", Toast.LENGTH_SHORT);
            startActivity(new Intent(RegistrationActivity.this, RegistrationActivity.class));
            finish();
        }
    }

    private void registerUser() {

        final String firstName = userFirstName.getText().toString();
        final String lastName = userLastName.getText().toString();
        final String phone = userPhone.getText().toString();
        final String email = userEmail.getText().toString();
        final String address = userAddress.getText().toString();
        final String password = userPassword.getText().toString();

        if (!firstName.equals("") && !lastName.equals("") && !phone.equals("") && !email.equals("") && !address.equals("") && !password.equals("")){

            if (checker.equals("clicked") && imageUri != null) {
                loadingBar.setTitle("Creating Account");
                loadingBar.setMessage("Please wait, while we checking credentials...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final StorageReference fileRef = storageProfilePictureRef.child(phone + ".jpg");

                uploadTask = fileRef.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Uri> task) {
                                                 if (task.isSuccessful()) {
                                                     Uri downloadUri = task.getResult();
                                                     myUrl = downloadUri.toString();

                                                     mAuth.createUserWithEmailAndPassword(email, password)
                                                             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<AuthResult> task) {
                                                                     if (task.isSuccessful()) {
                                                                         final DatabaseReference rootRef;
                                                                         rootRef = FirebaseDatabase.getInstance().getReference();

                                                                         String uid = mAuth.getCurrentUser().getUid();

                                                                         HashMap<String, Object> userMap = new HashMap<>();
                                                                         userMap.put("uid", uid);
                                                                         userMap.put("phone", phone);
                                                                         userMap.put("email", email);
                                                                         userMap.put("address", address);
                                                                         userMap.put("firstName", firstName);
                                                                         userMap.put("lastName", lastName);
                                                                         userMap.put("image", myUrl);

                                                                         rootRef.child("Users").child(uid).updateChildren(userMap)
                                                                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                     @Override
                                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                                         loadingBar.dismiss();
                                                                                         Toast.makeText(RegistrationActivity.this, "You are registered successfully", Toast.LENGTH_SHORT).show();

                                                                                         Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                                                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                         startActivity(intent);
                                                                                         finish();
                                                                                     }
                                                                                 });
                                                                     } else {
                                                                         loadingBar.dismiss();
                                                                         Toast.makeText(RegistrationActivity.this, "Error" + task.getException().toString(), Toast.LENGTH_LONG).show();
                                                                     }
                                                                 }
                                                             });
                                                 }
                                             }
                                         });


            } else{
                Toast.makeText(RegistrationActivity.this, "Choose the image, please.", Toast.LENGTH_SHORT).show();
            }

        }else {
            loadingBar.dismiss();
            Toast.makeText(RegistrationActivity.this, "Please, compile the register form", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userFirstName, userLastName, userPhone, userEmail, userAddress, userPassword;
    private ImageView userImage;
    private FirebaseAuth mAuth;
    private StorageReference storageProfilePictureRef;
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

        userFirstName = findViewById(R.id.user_first_name);
        userLastName = findViewById(R.id.user_last_name);
        userPhone = findViewById(R.id.user_phone_number);
        userEmail = findViewById(R.id.user_email);
        userAddress = findViewById(R.id.user_address);
        userPassword = findViewById(R.id.user_password);
        userImage = findViewById(R.id.user_image);

        Button registerBtn = findViewById(R.id.user_register_btn);


        registerBtn.setOnClickListener(view -> registerUser());

        userImage.setOnClickListener(v -> {
            checker = "clicked";

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(RegistrationActivity.this);
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
            Toast.makeText(RegistrationActivity.this, "Error: Try Again", Toast.LENGTH_SHORT).show();
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

                StorageTask<UploadTask.TaskSnapshot> uploadTask = fileRef.putFile(imageUri);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        final DatabaseReference rootRef;
                                        rootRef = FirebaseDatabase.getInstance().getReference();

                                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                                        HashMap<String, Object> userMap = new HashMap<>();
                                        userMap.put("uid", uid);
                                        userMap.put("phone", phone);
                                        userMap.put("email", email);
                                        userMap.put("address", address);
                                        userMap.put("firstName", firstName);
                                        userMap.put("lastName", lastName);
                                        userMap.put("image", myUrl);

                                        rootRef.child("Users").child(uid).updateChildren(userMap)
                                                .addOnCompleteListener(task11 -> {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RegistrationActivity.this, "You are registered successfully", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RegistrationActivity.this, "Error" + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
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
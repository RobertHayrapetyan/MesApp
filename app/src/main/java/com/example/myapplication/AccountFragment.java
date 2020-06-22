package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private TextView accountName, accountAddress, accountPhone, accountEmail;
    private CircleImageView accountImage;
    private ImageButton goToChatBtn, logOutBtn;
    private String uid;
    private String myUid;
    private DatabaseReference accountRef;
    private FirebaseAuth mAuth;
    //private final String email = "robby3000navs@gmail.com";
   // private final String password = "809091rob";
    private Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        bundle = new Bundle();
        //bundle.putString("uid", uid);

        accountName = view.findViewById(R.id.account_name);
        accountPhone = view.findViewById(R.id.account_phone);
        accountAddress = view.findViewById(R.id.account_address);
        accountEmail = view.findViewById(R.id.account_email);
        accountImage = view.findViewById(R.id.account_image);

        goToChatBtn = view.findViewById(R.id.go_to_chat_btn);
        goToChatBtn.setVisibility(View.GONE);

        logOutBtn = view.findViewById(R.id.logout_btn);
        logOutBtn.setVisibility(View.VISIBLE);
//         if (getArguments().getString("uid")!=null){
//             uid = getArguments().getString("uid");
//         }else{
//             uid = FirebaseAuth.getInstance().getUid();
//         }
//
//        myUid = FirebaseAuth.getInstance().getUid();
//
//        accountRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomNavigationView botNavView = getActivity().findViewById(R.id.bottom_navigation);
        botNavView.findViewById(R.id.navigation_account).setSelected(true);

        if (getArguments().getString("uid")!=null){
            uid = getArguments().getString("uid");
        }else{
            uid = FirebaseAuth.getInstance().getUid();
        }

        myUid = FirebaseAuth.getInstance().getUid();

        accountRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogout();
            }
        });

        if (uid.equals(myUid)){
            goToChatBtn.setVisibility(View.GONE);
            logOutBtn.setVisibility(View.GONE);
            logOutBtn.setVisibility(View.VISIBLE);
        }else{
            View bottomNavView = getActivity().findViewById(R.id.bottom_navigation);
            logOutBtn.setVisibility(View.GONE);
            goToChatBtn.setVisibility(View.VISIBLE);
            bottomNavView.setVisibility(View.GONE);
        }

        accountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("image").exists()) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(accountImage);
                    }

                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String address = dataSnapshot.child("address").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();

                    accountName.setText(firstName + " " + lastName);
                    accountPhone.setText(phone);
                    accountAddress.setText(address);
                    accountEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        goToChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment accountFrag = new MessageFragment();
                bundle.putString("uid", uid);
                accountFrag.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, accountFrag, "")
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    private void userLogout() {
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent intentLogout = new Intent(getContext(), LoginActivity.class);
        intentLogout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentLogout);
        getActivity().finish();
    }

}

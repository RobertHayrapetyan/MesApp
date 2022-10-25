package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {


    private final String uid = FirebaseAuth.getInstance().getUid();
    private final String myId = FirebaseAuth.getInstance().getUid();
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bundle = new Bundle();
        bundle.putString("uid", uid);
        Fragment messagesFragment = new MessagesFragment();
        messagesFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                messagesFragment).commit();

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch (menuItem.getItemId()){
                case R.id.navigation_contacts:
                    selectedFragment = new ContactsFragment();
                    selectedFragment.setArguments(bundle);
                    break;
                case R.id.navigation_messages:
                    selectedFragment = new MessagesFragment();
                    selectedFragment.setArguments(bundle);
                    break;
                case R.id.navigation_account:
                    selectedFragment = new AccountFragment();
                    selectedFragment.setArguments(bundle);
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }
    };

}

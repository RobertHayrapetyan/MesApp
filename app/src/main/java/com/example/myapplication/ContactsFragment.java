package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.ContactListItem;
import com.example.myapplication.ViewHolder.ContactViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ContactsFragment extends Fragment {

     private DatabaseReference contactsRef;
     private ImageButton searchBtn, closeSearchBtn;
     private EditText searchInput;
     private RecyclerView recyclerView;
     private String uid, search = "";
     Bundle bundle;
     private BottomNavigationView bottomNavView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchBtn = view.findViewById(R.id.search_btn);
        closeSearchBtn = view.findViewById(R.id.close_searc_btn);
        closeSearchBtn.setVisibility(View.GONE);
        searchInput = view.findViewById(R.id.search_contact_input);
        recyclerView = (RecyclerView)view.findViewById(R.id.contact_list);
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Users");
        bottomNavView = getActivity().findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bundle = new Bundle();
        uid = FirebaseAuth.getInstance().getUid();

        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
        bottomNavView.setVisibility(View.VISIBLE);
        bottomNavView.findViewById(R.id.navigation_contacts).setSelected(true);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!searchInput.isFocused()){
                    searchInput.requestFocus();
                    closeSearchBtn.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }else {
                    searchInput.clearFocus();
                }



                if (searchInput.getText().toString()!=""){
                    search = searchInput.getText().toString();
                    onStart();
                }
            }
        });
        closeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
                searchInput.clearFocus();
                closeSearchBtn.setVisibility(View.GONE);
                search = searchInput.getText().toString();
                onStart();
            }
        });

        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            }
        });

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy>3){
                    bottomNavView.setVisibility(View.GONE);
                }
                if (dy<-3){
                    bottomNavView.setVisibility(View.VISIBLE);
                }
            }
        });


        getContacts();

    }

    private void getContacts() {
        FirebaseRecyclerOptions<ContactListItem> options = new FirebaseRecyclerOptions.Builder<ContactListItem>()
                .setQuery(contactsRef.orderByChild("firstName").startAt(search), ContactListItem.class).build();

        FirebaseRecyclerAdapter<ContactListItem, ContactViewHolder> adapter =
                new FirebaseRecyclerAdapter<ContactListItem, ContactViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactViewHolder contactViewHolder, int i, @NonNull final ContactListItem contact) {
                        final String uid = getRef(i).getKey();

                        contactsRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String name = dataSnapshot.child("firstName").getValue().toString() + " " + dataSnapshot.child("lastName").getValue().toString();
                                    String image = dataSnapshot.child("image").getValue().toString();

                                    contactViewHolder.contactName.setText(name);
                                    Picasso.get().load(image).into(contactViewHolder.contactImage);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        contactViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("uid", uid);
                                Fragment accountFrag = new AccountFragment();
                                accountFrag.setArguments(bundle);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, accountFrag, "")
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout, parent, false);
                        ContactViewHolder contactViewHolder = new ContactViewHolder(view);
                        return contactViewHolder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

}

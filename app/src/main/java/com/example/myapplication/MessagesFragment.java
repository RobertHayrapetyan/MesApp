package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.ChatsListItem;
import com.example.myapplication.Models.Contact;
import com.example.myapplication.Models.ContactListItem;
import com.example.myapplication.ViewHolder.ContactViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MessagesFragment extends Fragment {

    private RecyclerView chatsList;

    private DatabaseReference chatsRef;

    private String uid = "uid";
    private String myUid = "uid";

    private BottomNavigationView bottomNavView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        chatsList = view.findViewById(R.id.chats_list);

        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        myUid = getArguments().getString("uid");

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(myUid);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        bottomNavView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavView.findViewById(R.id.navigation_messages).setSelected(true);
        getChats();

        chatsList.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
    }

    private void getChats() {


        FirebaseRecyclerOptions<ContactListItem> options = new FirebaseRecyclerOptions.Builder<ContactListItem>()
                .setQuery(chatsRef, ContactListItem.class).build();

        FirebaseRecyclerAdapter<ContactListItem, ContactViewHolder> adapter = new FirebaseRecyclerAdapter<ContactListItem, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder chatsViewHolder, int i, @NonNull ContactListItem chatsListItem) {
                final String chatId = getRef(i).getKey();


                DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference().child("Users").child(chatId);

                contactRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String name = dataSnapshot.child("firstName").getValue().toString() + " " + dataSnapshot.child("lastName").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();


                            chatsViewHolder.contactName.setText(name);
                            Picasso.get().load(image).into(chatsViewHolder.contactImage);

                        }

                        chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("uid", chatId);
                                Fragment accountFrag = new MessageFragment();
                                accountFrag.setArguments(bundle);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, accountFrag, "findThisFragment")
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

}

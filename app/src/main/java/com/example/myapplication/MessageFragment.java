package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageFragment extends Fragment {

    private String uid;
    private String myUid = FirebaseAuth.getInstance().getUid();
    private RecyclerView userMessageList;
    private EditText messageInput;
    private CircleImageView userImage;
    private ImageView sentIndicator, readIndicator;
    private TextView userName;
    private ImageButton messageSendBtn, goBackBtn;
    private DatabaseReference messageUserRef, chatRef, lastMesRef;
    private LinearLayoutManager layoutManager;
    private View bottomNavView;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference notificationRef;

    private List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private SoundPool soundPool;
    private int streamID;
    private AudioManager audioManager;
    private boolean plays = false, loaded = false;
    private float actVolume, maxVolume, volume;
    private int soundID;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        uid = getArguments().getString("uid");

        userMessageList = view.findViewById(R.id.message_list);
        messageInput = view.findViewById(R.id.message_input);
        messageSendBtn = view.findViewById(R.id.message_send_btn);
        userImage = view.findViewById(R.id.user_image);
        userName = view.findViewById(R.id.user_name);
        goBackBtn = view.findViewById(R.id.message_back_btn);


        messageUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        bottomNavView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavView.setVisibility(View.GONE);
        messageAdapter = new MessageAdapter(messagesList);


        linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(getActivity(), R.raw.send, 1);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        showUserInfo();
        getCurrentChat();

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        messageSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageInput.getText().toString().trim().equals("")){
                    sendMessage();
                }
            }
        });
        messageInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                }
            }
        });


    }

    public void playSound() {
        if (loaded && !plays) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
        }
    }

    private void getCurrentChat() {

        rootRef.child("Messages").child(myUid).child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);

                Log.d("LOG", message.getMessage());
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                Log.d("LOG", String.valueOf(messagesList.size()));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String readedMesID = dataSnapshot.child("mid").toString();

                for (Message message : messagesList){

                    try {
                        if (message.getMid().equals(readedMesID)){
                            int position = messagesList.indexOf(message);
                            Log.d("LOG", String.valueOf(position));
                            messagesList.set(position, dataSnapshot.getValue(Message.class));
                            messageAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showUserInfo() {
        messageUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("image").exists()) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(userImage);
                    }

                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();

                    userName.setText(firstName + " " + lastName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd:MM:yyyy");
        String currentDate = sdfDate.format(new Date());

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        String currentTime = sdfTime.format(new Date());

        String message = messageInput.getText().toString().trim();

        String messageSenderRef = "Messages/" + myUid + "/" + uid;
        String messageRecieverRef = "Messages/" + uid + "/" + myUid;

        DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(myUid).child(uid).push();
        String messagePushID = userMessageKeyRef.getKey();

        Map messageTextBody = new HashMap();
        messageTextBody.put("message", message);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", myUid);
        messageTextBody.put("time", currentTime);
        messageTextBody.put("date", currentDate);
        messageTextBody.put("readStatus", "sent");
        messageTextBody.put("mid", messagePushID);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put(messageRecieverRef + "/" + messagePushID, messageTextBody);

        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    //Toast.makeText(getContext(), "Sent", Toast.LENGTH_SHORT).show();

                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                    chatNotificationMap.put("from", myUid);
                    chatNotificationMap.put("type", "request");


                    playSound();

                    notificationRef.child(uid).push().setValue(chatNotificationMap);
                }else {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
                messageInput.setText("");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bottomNavView.setVisibility(View.VISIBLE);

    }
}

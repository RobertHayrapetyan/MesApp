package com.example.myapplication;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    int i = 0;

    public MessageAdapter(List<Message> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String messagesenderId = mAuth.getCurrentUser().getUid();
        final Message message = userMessagesList.get(position);

        final String fromUserID = message.getFrom();
        String resievedTime = message.getTime();
        String sentTime = message.getTime();
        String fromMessageType = message.getType();
        String readStatus = message.getReadStatus();
        String mid = message.getMid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){
                    String recieverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(recieverImage).placeholder(R.drawable.ic_account).into(holder.recievedImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {
            holder.recievedMessage.setVisibility(View.INVISIBLE);
            holder.recievedImage.setVisibility(View.INVISIBLE);
            holder.recievedTime.setVisibility(View.INVISIBLE);
            holder.receiveLin.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messagesenderId)){
                holder.sentLin.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.sentMessage.setTextColor(Color.WHITE);
                holder.sentMessage.setText(message.getMessage());
                Log.d("LOG", message.getMessage());


                holder.sentTime.setText(message.getTime());
                holder.sentTime.setTextColor(Color.GRAY);

                holder.sentLin.setVisibility(View.VISIBLE);
                holder.sentMessage.setVisibility(View.VISIBLE);
                holder.sentTime.setVisibility(View.VISIBLE);
                holder.sentIndicator.setVisibility(View.VISIBLE);
                if (readStatus.equals("readed")) {
                    holder.readIndicator.setVisibility(View.VISIBLE);
                }
            }else {
                holder.sentMessage.setVisibility(View.INVISIBLE);
                holder.sentTime.setVisibility(View.INVISIBLE);
                holder.readIndicator.setVisibility(View.INVISIBLE);
                holder.sentIndicator.setVisibility(View.INVISIBLE);
                holder.sentLin.setVisibility(View.INVISIBLE);

                holder.receiveLin.setBackgroundResource(R.drawable.reciever_messages_layout);
                holder.recievedMessage.setTextColor(Color.BLACK);
                holder.recievedMessage.setText(message.getMessage());
                Log.d("LOG", message.getMessage());

                holder.recievedTime.setText(message.getTime());
                holder.recievedTime.setTextColor(Color.WHITE);

                holder.recievedMessage.setVisibility(View.VISIBLE);
                holder.recievedImage.setVisibility(View.VISIBLE);
                holder.recievedTime.setVisibility(View.VISIBLE);
                holder.receiveLin.setVisibility(View.VISIBLE);


                FirebaseDatabase.getInstance().getReference()
                        .child("Messages").child(fromUserID).child(messagesenderId).child(mid)
                .child("readStatus").setValue("readed");

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView recievedMessage, sentMessage, recievedTime, sentTime;
        public CircleImageView recievedImage;
        public ImageView sentIndicator, readIndicator;
        public LinearLayout receiveLin, sentLin;

        public MessageViewHolder(@NonNull View itemView){
            super(itemView);

            recievedMessage = itemView.findViewById(R.id.recieved_message);
            sentMessage = itemView.findViewById(R.id.sent_message);
            recievedTime = itemView.findViewById(R.id.recieved_message_time);
            sentTime = itemView.findViewById(R.id.sent_message_time);
            recievedImage = itemView.findViewById(R.id.message_profile_image);
            readIndicator = itemView.findViewById(R.id.message_read_indicator);
            sentIndicator = itemView.findViewById(R.id.message_sent_indicator);
            receiveLin = itemView.findViewById(R.id.receive_mess_ll);
            sentLin = itemView.findViewById(R.id.sent_mess_ll);
        }
    }

}

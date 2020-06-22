package com.example.myapplication.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView messageText, messageTime;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        messageText = itemView.findViewById(R.id.recieved_message_text);
        messageTime = itemView.findViewById(R.id.message_time);

    }

    @Override
    public void onClick(View v) {

    }
}

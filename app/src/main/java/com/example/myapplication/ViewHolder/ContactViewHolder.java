package com.example.myapplication.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Interface.ItemClickListener;
import com.example.myapplication.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

public TextView contactName, contactEmail, contactPhone, contactAddress;
public CircleImageView contactImage;
public ItemClickListener listener;
public String uid;

public ContactViewHolder(@NonNull View itemView) {
        super(itemView);

        contactName = itemView.findViewById(R.id.contact_name);
        contactImage = itemView.findViewById(R.id.contact_image);

        }

public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
        }

@Override
public void onClick(View v) {

        listener.onClick(v, getAdapterPosition(), false);
        }
        }

package com.rwtcompany.chitchat.screen.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.model.Chat;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private ArrayList<Chat> list;
    private static final int left=1;
    private static final int right=2;

    public ChatAdapter(ArrayList<Chat> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        String user= FirebaseAuth.getInstance().getUid();
        if (list.get(position).getSender().equals(user)) {
            return right;
        }
        else
            return left;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==left)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_custom_layout, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_custom_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvMessage.setText(list.get(position).getMessage());
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        View view;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

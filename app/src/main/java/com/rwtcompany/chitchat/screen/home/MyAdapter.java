package com.rwtcompany.chitchat.screen.home;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.model.Message;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Message> list;
    private Context context;
    private Clicked frag;

    private Dialog dialog;
    private ImageView ivImage;


    public interface Clicked{
        void  pressed(int index);
        void longPressed(int index);
    }

    MyAdapter(ArrayList<Message> list, Context context,Clicked frag) {
        this.list = list;
        this.context=context;
        this.frag=frag;

        //Will show image on this dialog
        dialog=new Dialog(context);
        dialog.setContentView(R.layout.user_image_custom_dialog);
        ivImage = dialog.findViewById(R.id.ivImage);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_messages_custom_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.tvName.setText(list.get(position).getName());

        if(list.get(position).getImageUrl()!=null&&!list.get(position).getImageUrl().isEmpty())
            Glide.with(context).load(list.get(position).getImageUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivProfile);
        else
            Glide.with(context).load(R.drawable.person).into(holder.ivProfile);
        if(list.get(position).isSelf())
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray,null));
        else
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light,null));
        holder.tvMessage.setText(list.get(position).getMessage());
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                frag.longPressed(position);
                return true;
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag.pressed(position);
            }
        });
        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!list.get(position).getImageUrl().isEmpty())
                    Glide.with(context).load(list.get(position).getImageUrl()).into(ivImage);
                else
                    Glide.with(context).load(R.drawable.person).into(ivImage);
                dialog.show();
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivProfile;
        TextView tvName,tvMessage;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}

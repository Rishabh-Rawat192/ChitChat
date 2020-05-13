package com.rwtcompany.chitchat.screen.home;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.databinding.FragmentUsersBinding;
import com.rwtcompany.chitchat.model.User;
import com.rwtcompany.chitchat.screen.chat.ChatActivity;

import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    FragmentUsersBinding binding;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater);

        //Will show image on this dialog
        final Dialog dialog=new Dialog(binding.getRoot().getContext());
        dialog.setContentView(R.layout.user_image_custom_dialog);
        final ImageView ivImage = dialog.findViewById(R.id.ivImage);

        Query query = FirebaseFirestore.getInstance().collection("users").orderBy("name");
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();

        FirestoreRecyclerAdapter adapter=new FirestoreRecyclerAdapter<User, MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list_custom_layout,parent,false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull final User model) {
                if(!model.getImageUrl().isEmpty())
                    Glide.with(UsersFragment.this).load(model.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivUserProfile);
                else
                    Glide.with(UsersFragment.this).load(R.drawable.person).into(holder.ivUserProfile);
                holder.tvUserName.setText(model.getName());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("friendUid", model.getUid());
                        startActivity(intent);
                    }
                });

                holder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!model.getImageUrl().isEmpty())
                            Glide.with(UsersFragment.this).load(model.getImageUrl()).into(ivImage);
                        else
                            Glide.with(UsersFragment.this).load(R.drawable.person).into(ivImage);
                        dialog.show();
                    }
                });

                if(model.getActive().equals("Online"))
                    holder.ivActive.setVisibility(View.VISIBLE);
                else
                    holder.ivActive.setVisibility(View.INVISIBLE);
            }
        };
        binding.recycleView.setAdapter(adapter);

        return binding.getRoot();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView tvUserName;
        ImageView ivUserProfile,ivActive;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            tvUserName = itemView.findViewById(R.id.tvUserName);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            ivActive = itemView.findViewById(R.id.ivActive);
        }
    }
}

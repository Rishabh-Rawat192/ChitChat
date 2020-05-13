package com.rwtcompany.chitchat.screen.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.databinding.FragmentMessageBinding;
import com.rwtcompany.chitchat.model.Message;
import com.rwtcompany.chitchat.model.User;
import com.rwtcompany.chitchat.screen.chat.ChatActivity;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment implements MyAdapter.Clicked {

    public MessageFragment() {
        // Required empty public constructor
    }


    private FragmentMessageBinding binding;

    private FirebaseFirestore db;
    private Query query;
    private FirebaseAuth mAuth;

    private ArrayList<Message> messages;
    private RecyclerView.Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater);

        messages = new ArrayList<>();
        adapter = new MyAdapter(messages, this.getContext(), this);
        binding.recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        query = FirebaseFirestore.getInstance().collection("chats").orderBy("time");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                messages.clear();
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String sender = documentSnapshot.get("sender").toString();
                        String receiver = documentSnapshot.get("receiver").toString();
                        String cur = FirebaseAuth.getInstance().getUid();
                        String msg = documentSnapshot.get("message").toString();
                        if (cur.equals(sender)) {
                            messages.add(new Message(receiver, msg, true));
                        } else if (cur.equals(receiver)) {
                            messages.add(new Message(sender, msg, false));
                        }
                    }
                }

                for (int i = 0; i < messages.size(); i++) {
                    final DocumentReference docRef = db.collection("users").document(messages.get(i).getUid());
                    final int j = i;
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = document.get("name").toString();
                                    String imageUrl = document.get("imageUrl").toString();
                                    messages.get(j).setName(name);
                                    messages.get(j).setImageUrl(imageUrl);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void pressed(int index) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("friendUid", messages.get(index).getUid());
        startActivity(intent);
    }

    @Override
    public void longPressed(int index) {
        String uid = mAuth.getUid();
        String friendUid = messages.get(index).getUid();
        String messageId = "";
        if (uid.compareTo(friendUid) > 0)
            messageId = uid + friendUid;
        else
            messageId = friendUid + uid;
        final DocumentReference chatReference = FirebaseFirestore.getInstance().collection("chats").document(messageId);
        final CollectionReference ref = chatReference.collection(messageId);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete?");
        builder.setMessage("Will be deleted for both of you?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            ref.document(documentSnapshot.getId()).delete();
                        }
                    }
                });
                chatReference.delete();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}

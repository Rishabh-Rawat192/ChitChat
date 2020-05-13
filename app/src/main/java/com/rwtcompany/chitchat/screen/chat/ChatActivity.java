package com.rwtcompany.chitchat.screen.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.rwtcompany.chitchat.databinding.ActivityChatBinding;
import com.rwtcompany.chitchat.model.Chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    String friendUid;
    String uid;

    private ActivityChatBinding binding;
    private String messageId = "";
    private DocumentReference chatReference;
    private CollectionReference userReference;

    ArrayList<Chat> chats;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        uid = FirebaseAuth.getInstance().getUid();
        friendUid = getIntent().getStringExtra("friendUid");

        userReference = FirebaseFirestore.getInstance().collection("users");
        userReference.document(friendUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                String name = documentSnapshot.get("name").toString();
                String imageUrl = documentSnapshot.get("imageUrl").toString();
                String active=documentSnapshot.get("active").toString();

                binding.tvActive.setText(active);
                binding.tvFriendName.setText(name);
                if (!imageUrl.isEmpty())
                    Glide.with(getApplicationContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(binding.ivFriend);
            }
        });

        if (uid.compareTo(friendUid) > 0)
            messageId = uid + friendUid;
        else
            messageId = friendUid + uid;

        chatReference = FirebaseFirestore.getInstance().collection("chats").document(messageId);
        binding.ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    binding.etMessage.setText("");
                    Long time = -System.currentTimeMillis();
                    Map<String, Object> data = new HashMap<>();
                    data.put("sender", uid);
                    data.put("receiver", friendUid);
                    data.put("time", time);
                    data.put("message", message);
                    chatReference.set(data);

                    DocumentReference ref = chatReference.collection(messageId).document();
                    String id = ref.getId();
                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("id", id);
                    data2.put("sender", uid);
                    data2.put("receiver", friendUid);
                    data2.put("time", time);
                    data2.put("message", message);
                    ref.set(data2);
                }
            }
        });

        chats = new ArrayList<>();
        adapter = new ChatAdapter(chats);

        Query query = chatReference.collection(messageId).orderBy("time").limit(30);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                chats.clear();
                if(queryDocumentSnapshots!=null)
                {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String message=documentSnapshot.get("message").toString();
                        String sender=documentSnapshot.get("sender").toString();
                        chats.add(new Chat(message, sender));
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userReference.document(uid).update("active", "Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ");
        Date date = new Date();
        String active=dateFormat.format(date);
        userReference.document(uid).update("active", active);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

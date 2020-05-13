package com.rwtcompany.chitchat.screen.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rwtcompany.chitchat.MainActivity;
import com.rwtcompany.chitchat.screen.editUser.EditProfileActivity;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.databinding.ActivityMessageBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    private FragmentStateAdapter pagerAdapter;

    private FirebaseAuth mAuth;
    private DocumentReference userReference;

    private ActivityMessageBinding binding;

    //Navigation view's header vew
    TextView tvUserName;
    ImageView ivProfile;
    Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Instantiate a ViewPager2 and a PagerAdapter.
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        binding.pager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabs, binding.pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(position==0)
                {
                    tab.setText("Messages");
                }
                else{
                    tab.setText("Friends");
                }

            }
        }).attach();


        tvUserName=binding.navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        ivProfile=binding.navigationView.getHeaderView(0).findViewById(R.id.ivProfile);
        btnChange = binding.navigationView.getHeaderView(0).findViewById(R.id.btnChange);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, EditProfileActivity.class));
            }
        });



        mAuth = FirebaseAuth.getInstance();
        userReference= FirebaseFirestore.getInstance().collection("users").document(mAuth.getUid());

        userReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    return;
                }
                String name=documentSnapshot.get("name").toString();
                String imageUrl=documentSnapshot.get("imageUrl").toString();

                tvUserName.setText(name);
                if(!imageUrl.isEmpty())
                    Glide.with(getApplicationContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfile);
                else
                    Glide.with(getApplicationContext()).load(R.drawable.person).into(ivProfile);
            }
        });

        setSupportActionBar(binding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            binding.drawer.openDrawer(GravityCompat.START);
        } else if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            startActivity(new Intent(MessageActivity.this, MainActivity.class));
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ");
            Date date = new Date();
            String active=dateFormat.format(date);
            userReference.update("active", active);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        userReference.update("active", "Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ");
        Date date = new Date();
        String active=dateFormat.format(date);
        userReference.update("active", active);
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (binding.pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            binding.pager.setCurrentItem(binding.pager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment=new MessageFragment();
                    break;
                default:
                    fragment=new UsersFragment();
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}

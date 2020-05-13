package com.rwtcompany.chitchat.screen.editUser;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.databinding.ActivityEditProfileBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DocumentReference userRef;

    ActivityEditProfileBinding binding;
    String name="";
    String imageUrl="";
    Uri imageUri=null;

    AlertDialog.Builder builder;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth=FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("users").document(mAuth.getUid());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("saving...");
        progressDialog.setCancelable(false);

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    return;
                }
                name=documentSnapshot.get("name").toString();
                imageUrl=documentSnapshot.get("imageUrl").toString();

                binding.etName.setText(name);
                binding.etName.setSelection(name.length());
                if(!imageUrl.isEmpty())
                    Glide.with(getApplicationContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(binding.ibProfile);
            }
        });

        binding.ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });

        builder=new AlertDialog.Builder(this);
        builder.setTitle("Save?");
        builder.setTitle("Are you sure to save changes?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                name=binding.etName.getText().toString().trim();
                if(name.isEmpty())
                {
                    Toast.makeText(EditProfileActivity.this,"Enter your name",Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(imageUri==null)
                    {
                        userRef.update("name",name);
                        userRef.update("imageUrl", imageUrl);
                        Toast.makeText(EditProfileActivity.this,"Saved..",Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else
                    {
                        progressDialog.show();
                        final StorageReference ref = FirebaseStorage.getInstance().getReference().child(mAuth.getUid()).child("profile");
                        UploadTask uploadTask = ref.putFile(imageUri);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    userRef.update("name",name);
                                    userRef.update("imageUrl", downloadUri.toString());
                                    Toast.makeText(EditProfileActivity.this,"Saved..",Toast.LENGTH_LONG).show();
                                    finish();
                                }
                                progressDialog.dismiss();
                            }
                        });
                    }
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        binding.tvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUrl="";
                Glide.with(getApplicationContext()).load(R.drawable.person).into(binding.ibProfile);
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        binding.ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(binding.ibProfile);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

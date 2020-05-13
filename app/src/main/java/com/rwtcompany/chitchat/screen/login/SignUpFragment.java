package com.rwtcompany.chitchat.screen.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rwtcompany.chitchat.screen.home.MessageActivity;
import com.rwtcompany.chitchat.databinding.FragmentSignUpBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    public SignUpFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth;
    private CollectionReference reference;

    private Uri imageUri=null;
    private FragmentSignUpBinding binding;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentSignUpBinding.inflate(inflater);

        mAuth=FirebaseAuth.getInstance();
        reference= FirebaseFirestore.getInstance().collection("users");
        dialog=new ProgressDialog(getContext());
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);

        binding.ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(),SignUpFragment.this);
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name=binding.etName.getText().toString().trim();
                String email=binding.etEmail.getText().toString().trim();
                String password=binding.etPassword.getText().toString().trim();
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(),"enter all details!",Toast.LENGTH_LONG).show();
                }
                else {
                    dialog.show();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                if(imageUri!=null)
                                {
                                    final StorageReference ref= FirebaseStorage.getInstance().getReference().child(mAuth.getUid()).child("profile");
                                    UploadTask uploadTask = ref.putFile(imageUri);
                                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                                            dialog.dismiss();
                                            if (task.isSuccessful()) {
                                                String imageUrl= task.getResult().toString();
                                                final Map<String, String> map = new HashMap<>();
                                                map.put("name", name);
                                                map.put("imageUrl", imageUrl);
                                                map.put("uid",mAuth.getUid());
                                                map.put("active", "Online");
                                                reference.document(mAuth.getUid()).set(map);
                                                startActivity(new Intent(getActivity(), MessageActivity.class));
                                                getActivity().finish();
                                            } else if(task.getException()!=null) {
                                                Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    String imageUrl= "";
                                    final Map<String, String> map = new HashMap<>();
                                    map.put("name", name);
                                    map.put("imageUrl", imageUrl);
                                    map.put("uid",mAuth.getUid());
                                    map.put("active", "");
                                    reference.document(mAuth.getUid()).set(map);
                                    startActivity(new Intent(getActivity(), MessageActivity.class));
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            } else if (task.getException() != null) {
                                dialog.dismiss();
                                Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri= result.getUri();
                Glide.with(this)
                        .load(imageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ibProfile);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

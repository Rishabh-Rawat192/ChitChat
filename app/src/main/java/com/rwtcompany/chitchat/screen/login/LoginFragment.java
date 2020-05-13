package com.rwtcompany.chitchat.screen.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rwtcompany.chitchat.screen.home.MessageActivity;
import com.rwtcompany.chitchat.R;
import com.rwtcompany.chitchat.databinding.FragmentLoginBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private NavController navController;

    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentLoginBinding binding = FragmentLoginBinding.inflate(inflater);
        mAuth=FirebaseAuth.getInstance();
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");

        if(mAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getActivity(), MessageActivity.class));
            getActivity().finish();
        }

        binding.tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_loginFragment_to_signUpFragment);
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=binding.etEmail.getText().toString().trim();
                String password=binding.etPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(),"enter all details!",Toast.LENGTH_LONG).show();
                }
                else {
                    dialog.show();
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                startActivity(new Intent(getActivity(), MessageActivity.class));
                                getActivity().finish();
                            } else if (task.getException() != null) {
                                Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        binding.tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=binding.etEmail.getText().toString().trim();
                if(email.isEmpty())
                    Toast.makeText(getContext(),"enter your email...",Toast.LENGTH_LONG).show();
                else
                {
                    dialog.show();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            if(task.isSuccessful())
                                Toast.makeText(getContext(),"Password reset link send to your email...",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController= Navigation.findNavController(view);
    }
}

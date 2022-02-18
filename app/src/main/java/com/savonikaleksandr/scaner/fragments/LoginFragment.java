package com.savonikaleksandr.scaner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.savonikaleksandr.scaner.Admin_BaseActivity;
import com.savonikaleksandr.scaner.R;


public class LoginFragment extends Fragment {

    Context context;
    private FirebaseAuth mAuth;
    private EditText login, pass;
    private TextView button_login;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_login, container, false);
        setupUI(root);
        return root;
    }
    private void setupUI(View root) {
        mAuth = FirebaseAuth.getInstance();
        context = requireContext();

        login = root.findViewById(R.id.login_frag);
        pass = root.findViewById(R.id.password_frag);
        button_login = root.findViewById(R.id.button_login_fragment);

        button_login.setOnClickListener(this::LoginInAcc);
    }

    private void LoginInAcc(View view) {

        String email = login.getText().toString();
        String password = pass.getText().toString();
        if (TextUtils.isEmpty(email)) {
            login.setError(getResources().getString(R.string.error_email));
            return;
        } else if (TextUtils.isEmpty(password)) {
            pass.setError(getResources().getString(R.string.error_pass));
            return;
        }
        Snackbar.make(getContext(), view, getString(R.string.snack_succes), BaseTransientBottomBar.LENGTH_LONG).show();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(getActivity(),authResult -> {
            int index = email.indexOf("@");
            String admin = email.substring(0,index);
            Intent intent = new Intent(getActivity(), Admin_BaseActivity.class);
            intent.putExtra("admin", admin);
            intent.putExtra("email",email);
            startActivity(intent);

        }).addOnFailureListener(getActivity(), e ->
                Snackbar.make(getContext(), view,"Error! Please check your password or Email", BaseTransientBottomBar.LENGTH_LONG).show());
    }

}
package com.example.navigationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {
//  variables
    private FirebaseAuth mAuth;
    
    TextInputLayout  regName, regUsername, regEmail, regPassword;
    Button regBtn, regToLoginBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

//        Hooks
        regToLoginBtn = (Button) findViewById(R.id.ToLoginBtn);
        regBtn = (Button) findViewById(R.id.Btn);
        regName = findViewById(R.id.name);
        regUsername = findViewById(R.id.username);
        regEmail = findViewById(R.id.email);
        regPassword = findViewById(R.id.password);


//        Opens login.class
        regToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

//        Register button is clicked
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmail.getEditText().getText().toString().trim();
                String fullname = regName.getEditText().getText().toString().trim();
                String password = regPassword.getEditText().getText().toString().trim();
                String username = regUsername.getEditText().getText().toString().trim();

                if (fullname.isEmpty()) {
                    regName.setError("Full name is required");
                    regName.requestFocus();
                    return;

                }

                if (username.isEmpty()) {
                    regUsername.setError("Enter your student ID");
                    regUsername.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    regEmail.setError("Enter your email");
                    regEmail.requestFocus();
                    return;

                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    regEmail.setError("Please provide valid email");
                    regEmail.requestFocus();
                    return;
                }


                if (password.isEmpty()) {
                    regPassword.setError("Enter password");
                    regPassword.requestFocus();
                    return;

                }


                if (password.length() < 6) {
                    regPassword.setError("Min password length should be 6 characters");
                    regPassword.requestFocus();
                    return;
                }


//                Saves user data in Firebase as google as Authentication
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "User created!!!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUp.this, Login.class));

                        } else {
                            Toast.makeText(SignUp.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

}
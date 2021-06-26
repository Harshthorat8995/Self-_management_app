package com.example.navigationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button callSignUp, login_btn;
    ImageView image;
    TextView logoText, sloganText;
    TextInputLayout loginemail, logpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();


        ///Hooks
        callSignUp = findViewById(R.id.new_user);
        image = findViewById(R.id.LogoImage);
        logoText = findViewById(R.id.LogoName);
        sloganText = findViewById(R.id.Signin);
        loginemail = findViewById(R.id.email);
        logpassword = findViewById(R.id.password);
        login_btn = findViewById(R.id.GO_BTN);


        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);

                Pair[] pairs = new Pair[7];

                pairs[0] = new Pair<View, String>(image, "logo_image");
                pairs[1] = new Pair<View, String>(logoText, "logo_text");
                pairs[2] = new Pair<View, String>(sloganText, "logo_desc");
                pairs[3] = new Pair<View, String>(loginemail, "User_name");
                pairs[4] = new Pair<View, String>(logpassword, "password_tran");
                pairs[5] = new Pair<View, String>(login_btn, "button_tran");
                pairs[6] = new Pair<View, String>(callSignUp, "login_signup_tran");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
                    startActivity(intent, options.toBundle());
                }
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginemail.getEditText().getText().toString().trim();
                String password = logpassword.getEditText().getText().toString().trim();


                if (email.isEmpty()) {
                    loginemail.setError("Enter your student ID");
                    loginemail.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    logpassword.setError("Enter password");
                    logpassword.requestFocus();
                    return;

                }


                if (password.length() < 6) {
                    logpassword.setError("Min password length should be 6 characters");
                    logpassword.requestFocus();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user.isEmailVerified()) {
                                Toast.makeText(Login.this, "successfully logged in!!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, todolist.class));


                            } else {
                                user.sendEmailVerification();
                                Toast.makeText(Login.this, "Check your email to verify your account", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(Login.this, "Error!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}





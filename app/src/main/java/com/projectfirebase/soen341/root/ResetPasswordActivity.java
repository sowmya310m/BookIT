package com.projectfirebase.soen341.root;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText registered_emailid;
    TextView forgot_button;

    FirebaseAuth firebaseAuth;


    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forgot_password);


        registered_emailid = (EditText) findViewById(R.id.registered_emailid);
        forgot_button = (TextView) findViewById(R.id.forgot_button );

        firebaseAuth = FirebaseAuth.getInstance();

        forgot_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String email = registered_emailid.getText().toString();

                if (email.equals("")) {
                    Toast.makeText(ResetPasswordActivity.this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete( @NonNull Task<Void> task ) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "Please check your Email", Toast.LENGTH_SHORT).show();
                                 startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));

                            } else
                             //   String error = task.getException().getMessage();
                            Toast.makeText(ResetPasswordActivity.this, "Error", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        });
    }

}
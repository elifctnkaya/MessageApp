package com.example.messageapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText forgotEmail;
    private Button forgotButton;

    private ImageView loginGeriDon;
    private TextView forgotText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgotEmail = findViewById(R.id.forgotEmail);
        forgotButton = findViewById(R.id.forgotButton);
        forgotText = findViewById(R.id.forgotText);
        loginGeriDon = findViewById(R.id.loginGeriDon);

        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotEmail.setText("");
                Toast.makeText(ForgotPasswordActivity.this, "Email Adresinizi Kontrol Ediniz..", Toast.LENGTH_SHORT).show();
            }
        });

        loginGeriDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button Login_button;
    private EditText Login_email, Login_password;
    private TextView Forgot_password, New_signup;

    //private FirebaseUser gelenKullanici;

    private FirebaseAuth Yetki;
    private DatabaseReference kullaniciYolu;

    ProgressDialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login_button = findViewById(R.id.loginButton);
        Login_email = findViewById(R.id.loginEmail);
        Login_password = findViewById(R.id.loginPassword);
        Forgot_password = findViewById(R.id.ForgotPassword);
        New_signup = findViewById(R.id.newSignup);

        loginDialog = new ProgressDialog(this);

        Yetki = FirebaseAuth.getInstance();
        //gelenKullanici = Yetki.getCurrentUser();  //gelen kullanıcı boş değilse anasayfaya gönder
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");


        New_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recordActivityIntent = new Intent(LoginActivity.this, RecordActivity.class);
                startActivity(recordActivityIntent);
            }
        });

        Login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KullaniciGirisİzni();
            }
        });

        Forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sifreyenile = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(sifreyenile);
            }
        });

    }

    private void KullaniciGirisİzni()
    {
        String email = Login_email.getText().toString();
        String sifre = Login_password.getText().toString();
        if(email.length() == 0)
        {
            Login_email.setError("Email Boş Olamaz!");
        }
        if (sifre.length() == 0)
        {
            Login_password.setError("Şifre Boş Olamaz!");
        }
        else
        {
            loginDialog.setTitle("Giriş Yapılıyor");
            loginDialog.setMessage("Lütfen Bekleyiniz");
            loginDialog.setCanceledOnTouchOutside(true);
            loginDialog.show();

            Yetki.signInWithEmailAndPassword(email,sifre).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String aktifKullaniciId = Yetki.getCurrentUser().getUid();
                        //cihazlara bildirim için benzersiz bir iddir.
                        String cihazToken = FirebaseInstanceId.getInstance().getToken();

                        // Token numaralarını kullanıcı bilgilerine (veritabanında) ekleme
                        kullaniciYolu.child(aktifKullaniciId).child("cihaz_token").setValue(cihazToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Intent anaSayfaIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            anaSayfaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //geri tuşuna basınca giriş ekranına dönmemesi için
                                            startActivity(anaSayfaIntent);
                                            finish();

                                            Toast.makeText(LoginActivity.this, "Giriş Başarılı", Toast.LENGTH_SHORT).show();
                                            loginDialog.dismiss();
                                        }
                                    }
                                });
                    }
                    else
                    {
                        String mesaj = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Hata: "+mesaj+" Bilgileri Kontrol Edin", Toast.LENGTH_SHORT).show();
                        loginDialog.dismiss();
                    }
                }
            });
        }

    }

    /*@Override
    protected void onStart() {
        super.onStart();

        //gelen kullanıcı boş değilse ana ekrana gider
        if(gelenKullanici != null){
            KullaniciAnaEkranaGonder();
        }
    }*/

    private void KullaniciAnaEkranaGonder() {
        Intent AnaActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(AnaActivityIntent);
    }
}
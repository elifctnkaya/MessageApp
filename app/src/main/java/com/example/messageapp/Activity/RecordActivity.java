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

public class RecordActivity extends AppCompatActivity {

    private Button Record_button;
    private EditText Record_mail, Record_password;
    private TextView Recorded_account;

    private DatabaseReference kokReference;
    private FirebaseAuth Yetki;

    private ProgressDialog Yukleniyor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Yetki = FirebaseAuth.getInstance();
        kokReference = FirebaseDatabase.getInstance().getReference();


        Yukleniyor = new ProgressDialog(this);

        Record_button = findViewById(R.id.recordButton);
        Record_mail = findViewById(R.id.recordEmail);
        Record_password = findViewById(R.id.recordPassword);
        Recorded_account = findViewById(R.id.recordedAccount);

        //mevcut hesap varsa giriş ekranına yönlendirme işlemi
        Recorded_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivityIntent = new Intent(RecordActivity.this, LoginActivity.class);
                startActivity(loginActivityIntent);
            }
        });

        //Kayıt yapma işlemi(butona basılınca gerçekleşecek olanlar)
        Record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //yeni hesap oluşturma metodu
                NewAccountSignUp();
            }
        });
    }

    private void NewAccountSignUp() {
        String email = Record_mail.getText().toString();
        String sifre = Record_password.getText().toString();

        //mail-şifre boş girilirse uyarı verir
        if (email.length() == 0) {
            Record_mail.setError("Email Boş Olamaz!");
        }
        if (sifre.length() == 0) {
            Record_password.setError("Şifre Boş Olamaz!");
        }
        //boş değillerse kayıt yap
        else {
            Yukleniyor.setTitle("Hesap Oluşturuluyor");
            Yukleniyor.setMessage("Lütfen Bekleyiniz");
            Yukleniyor.setCanceledOnTouchOutside(true);
            Yukleniyor.show();

            Yetki.createUserWithEmailAndPassword(email, sifre).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        //benzersiz bildirim id
                        String cihazToken = FirebaseInstanceId.getInstance().getToken();

                        String mevcutKullaniciId = Yetki.getCurrentUser().getUid();
                        kokReference.child("Kullanicilar").child(mevcutKullaniciId).setValue("");

                        kokReference.child("Kullanicilar").child(mevcutKullaniciId).child("cihaz_token")
                                .setValue(cihazToken);

                        //işlem başarılı ise ayarlar sayfasına gider
                        Intent anaSayfaIntent = new Intent(RecordActivity.this, SettingsActivity.class);
                        anaSayfaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //geri tuşuna basılınca kayıt sayfasına dönmemesi için
                        startActivity(anaSayfaIntent);
                        finish();

                        Toast.makeText(RecordActivity.this, "Hesap Başarı İle Oluşturuldu", Toast.LENGTH_SHORT).show();
                        Yukleniyor.dismiss();
                    }
                    else
                    {
                        String mesaj = task.getException().toString();
                        Toast.makeText(RecordActivity.this, "Hata: "+mesaj+" Bilgileri Kontrol Edin", Toast.LENGTH_SHORT).show();
                        Yukleniyor.dismiss();
                    }
                }
            });
        }

    }
}
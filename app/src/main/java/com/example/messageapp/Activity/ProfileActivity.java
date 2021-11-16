package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.messageapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String alinanKullaniciId, aktifKullaniciId, aktifDurum;

    private CircleImageView kullaniciProfilResmi;
    private TextView kullaniciProfilAdi, kullaniciProfilDurumu;
    private Button mesajGondermeTalebi, mesajDegerlendirmeTalebi;

    private DatabaseReference kullaniciYolu, sohbetTalebiYolu, sohbetlerYolu, bildirimYolu;
    private FirebaseAuth Yetki;

    private ImageView findFriendsDon;
    private TextView findFriendsGoster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //arkadaş bulma aktivitesinden gönderileni alma  //buradaki isim findFriends activity'deki ile aynı olmalı
        alinanKullaniciId = getIntent().getExtras().get("tiklananKullaniciIdGoster").toString();

        aktifDurum = "yeni";

        //tanımlamalar
        kullaniciProfilResmi = findViewById(R.id.profilResmiZiyaret);
        kullaniciProfilAdi = findViewById(R.id.kullaniciAdiZiyaret);
        kullaniciProfilDurumu = findViewById(R.id.kullaniciDurumuZiyaret);
        mesajGondermeTalebi = findViewById(R.id.mesajGondermeTalebi);
        mesajDegerlendirmeTalebi = findViewById(R.id.mesajDegerlendirmeTalebi);
        findFriendsDon = findViewById(R.id.findFriendsDon);
        findFriendsGoster = findViewById(R.id.findFriendsGoster);

        findFriendsDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friends = new Intent(ProfileActivity.this, FindFriendsActivity.class);
                startActivity(friends);
            }
        });

        //Firebase
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        sohbetTalebiYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");
        bildirimYolu = FirebaseDatabase.getInstance().getReference().child("Bildirimler");
        Yetki = FirebaseAuth.getInstance();

        aktifKullaniciId = Yetki.getCurrentUser().getUid();

        kullaniciBilgisiAl();
    }

    private void kullaniciBilgisiAl() {

        kullaniciYolu.child(alinanKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() &&  (snapshot.hasChild("resim")))
                {
                    //Veritabanından verileri çekip değişkenlere aktarma
                    String kullaniciResmi = snapshot.child("resim").getValue().toString();
                    String kullaniciAdi = snapshot.child("ad").getValue().toString();
                    String kullaniciDurumu = snapshot.child("durum").getValue().toString();

                    //Verileri kontrollere aktarma
                    //resim yoksa var olan simge gösterilecek
                    Picasso.get().load(kullaniciResmi).placeholder(R.drawable.profile).into(kullaniciProfilResmi);
                    kullaniciProfilAdi.setText(kullaniciAdi);
                    kullaniciProfilDurumu.setText(kullaniciDurumu);

                    //mesaj talebi gönderme metodu
                    chatTalepleriniYonet();

                }
                else  //resim yoksa
                {
                    //Veritabanından verileri çekip değişkenlere aktarma
                    String kullaniciAdi = snapshot.child("ad").getValue().toString();
                    String kullaniciDurumu = snapshot.child("durum").getValue().toString();

                    //Verileri kontrollere aktarma
                    kullaniciProfilAdi.setText(kullaniciAdi);
                    kullaniciProfilDurumu.setText(kullaniciDurumu);

                    //mesaj talebi gönderme metodu
                    chatTalepleriniYonet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void chatTalepleriniYonet() {
        //talep gönderildiyse butonda talep iptal seçeneği yazması için
        sohbetTalebiYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(alinanKullaniciId))
                {
                    String talep_turu = snapshot.child(alinanKullaniciId).child("talep_turu").getValue().toString();

                    if(talep_turu.equals("gonderildi"))
                    {
                        aktifDurum = "talep_gönderildi";
                        mesajGondermeTalebi.setText("Mesaj Talebi İptal");
                    }
                    else
                    {
                        aktifDurum = "talep_alindi";
                        mesajGondermeTalebi.setText("Mesaj Talebi Kabul");
                        mesajDegerlendirmeTalebi.setVisibility(View.VISIBLE);
                        mesajDegerlendirmeTalebi.setEnabled(true);

                        mesajDegerlendirmeTalebi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatTalebiIptal();
                            }
                        });
                    }
                }
                else
                {
                    sohbetlerYolu.child(aktifKullaniciId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(alinanKullaniciId))
                                    {
                                        aktifDurum = "arkadaşlar";
                                        mesajGondermeTalebi.setText("Bu Sohbeti Sil");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(aktifKullaniciId.equals(alinanKullaniciId))
        {
            //butonu sakla(kendi profiline tıkladığında mesaj gönder butonu olmaması için)
            mesajGondermeTalebi.setVisibility(View.INVISIBLE);
        }
        else
        {
            //mesaj talebi gönderme
            mesajGondermeTalebi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mesajGondermeTalebi.setEnabled(false);   //mesaj gönderme butonuna tıkladıktan sonra tekrar göstermemesi için
                    if(aktifDurum.equals("yeni"))
                    {
                        chatTalebiGonder();
                    }
                    if(aktifDurum.equals("talep_gönderildi"))
                    {
                        chatTalebiIptal();
                    }
                    if(aktifDurum.equals("talep_alindi"))
                    {
                        chatTalebiKabul();
                    }
                    if(aktifDurum.equals("arkadaşlar"))
                    {
                        OzelChatiSil();
                    }
                }
            });

        }
    }

    private void OzelChatiSil() {
        //sohbeti sil
        sohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    //talep alandan silinen
                    sohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mesajGondermeTalebi.setEnabled(true);
                                aktifDurum = "yeni";   //tekrar istek atabilmesi için
                                mesajGondermeTalebi.setText("Mesaj Talebi Gönder");

                                mesajDegerlendirmeTalebi.setVisibility(View.INVISIBLE);
                                mesajDegerlendirmeTalebi.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void chatTalebiKabul() {
        sohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            sohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                //sohbet başladıysa talep ortadan kaldırılır
                                                sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                //gönderenden talep kaldırıldı alınandan da kaldırılmalı
                                                                if (task.isSuccessful())
                                                                {
                                                                    sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    mesajGondermeTalebi.setEnabled(true);
                                                                                    aktifDurum = "arkadaşlar";
                                                                                    mesajGondermeTalebi.setText("Bu Sohbeti Sil");
                                                                                    mesajDegerlendirmeTalebi.setVisibility(View.INVISIBLE);
                                                                                    mesajDegerlendirmeTalebi.setEnabled(false);
                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void chatTalebiIptal() {
        //talebi gönderenden silinen (yani butondaki değişiklik)
        sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    //talep alandan silinen
                    sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mesajGondermeTalebi.setEnabled(true);
                                aktifDurum = "yeni";   //tekrar istek atabilmesi için
                                mesajGondermeTalebi.setText("Mesaj Talebi Gönder");

                                mesajDegerlendirmeTalebi.setVisibility(View.INVISIBLE);
                                mesajDegerlendirmeTalebi.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void chatTalebiGonder() {
        //veritabanına veri gönderme
        sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("talep_turu").setValue("gonderildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            //Veritabanına veri gönderme
                            sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("talep_turu").setValue("alindi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                //Bildirim için
                                                HashMap<String,String> chatBildirimMap = new HashMap<>();
                                                chatBildirimMap.put("kimden",aktifKullaniciId);
                                                chatBildirimMap.put("tur","talep");

                                                //bildirim veritabanı yoluna veri gönderme
                                                bildirimYolu.child(alinanKullaniciId).push().setValue(chatBildirimMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            mesajGondermeTalebi.setEnabled(true);   //buton görünür yapıldı
                                                            aktifDurum= "talep_gönderildi";
                                                            mesajGondermeTalebi.setText("Mesaj Talebi İptal");
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
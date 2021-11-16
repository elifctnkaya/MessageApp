package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.messageapp.Adapter.SekmelerAdapter;
import com.example.messageapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar xToolbar;
    private ViewPager xViewPager;
    private TabLayout xTabLayout;
    private SekmelerAdapter xSekmelerAdapter;

    private FirebaseAuth Yetki;
    private DatabaseReference kullanicilarReference;

    ProgressDialog loginDialog;

    private String aktifKullaniciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xToolbar = findViewById(R.id.ana_sayfa_toolbar);
        setSupportActionBar(xToolbar);
        getSupportActionBar().setTitle("ChatBox");

        xViewPager = findViewById(R.id.anaSekmelerPager);
        xSekmelerAdapter = new SekmelerAdapter(getSupportFragmentManager());
        xViewPager.setAdapter(xSekmelerAdapter);

        xTabLayout = findViewById(R.id.anaSekmeler);
        xTabLayout.setupWithViewPager(xViewPager);

        Yetki = FirebaseAuth.getInstance();
        kullanicilarReference = FirebaseDatabase.getInstance().getReference();

        loginDialog = new ProgressDialog(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mevcutKullanici = Yetki.getCurrentUser();
        if(mevcutKullanici == null)
        {
            KullaniciGirisEkraninaGonder();
        }
        else
        {
            kullaniciDurumGuncelleme("Çevrimiçi");
            KullanicininVarliginiDogrula();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mevcutKullanici = Yetki.getCurrentUser();
        if(mevcutKullanici != null)
        {
            kullaniciDurumGuncelleme("Çevrimdışı");
        }
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //herhangibir sebepten dolayı program durursa
        /*FirebaseUser mevcutKullanici = Yetki.getCurrentUser();
         if(mevcutKullanici != null)
        {
               kullaniciDurumGuncelleme("Çevrimdışı");
        }*/
    }

    //ad durum vs bilgilerinin firebase'e gönderilmesi için
    private void KullanicininVarliginiDogrula() {

        String mevcutKullaniciId = Yetki.getCurrentUser().getUid();  //firebase'de bulunan id'yi alıyor
        //firebase'deki ağacın içine yazılacak bilgiler için yer belirtildi
        kullanicilarReference.child("Kullanicilar").child(mevcutKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child("ad").exists()))
                {
                    //giriş ekranında
                }
                else
                {
                    Intent ayarlar = new Intent(MainActivity.this, SettingsActivity.class);
                    ayarlar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ayarlar);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void KullaniciGirisEkraninaGonder()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.anamenu,menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.findFriends)
        {
            Intent arkadasBul = new Intent(MainActivity.this, FindFriendsActivity.class);
            startActivity(arkadasBul);
        }
        if(item.getItemId() == R.id.buildGroup)
        {
            grupTalebi();
        }
        if(item.getItemId() == R.id.setting)
        {
            Intent ayar = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(ayar);
        }
        if(item.getItemId() == R.id.signOut)
        {
            loginDialog.setTitle("Çıkış Yapılıyor");
            loginDialog.setMessage("Lütfen Bekleyiniz");
            loginDialog.setCanceledOnTouchOutside(true);
            loginDialog.show();

            kullaniciDurumGuncelleme("Çevrimdışı");
            Yetki.signOut();
            Intent giris = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(giris);
        }
        return true;
    }

    private void grupTalebi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Grup Adı Girin");
        builder.setIcon(R.drawable.signup);

        final EditText grupAdiAlani = new EditText(MainActivity.this);
        grupAdiAlani.setHint("Örnek: Super Girls");
        builder.setView(grupAdiAlani);

        builder.setPositiveButton("Oluştur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //oluşturulan grubun adını alıp firebase'e gönderecek

                String grupAdi = grupAdiAlani.getText().toString();

                //grup adı boşsa uyarı verir
                if(TextUtils.isEmpty(grupAdi))
                {
                    Toast.makeText(MainActivity.this, "Grup Adı Boş Olamaz", Toast.LENGTH_LONG).show();
                }
                else
                {
                    grupOlustur(grupAdi);
                }

            }
        });

        builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //iptale basınca alertdialog kapatılsın
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void grupOlustur(final String grupAdi) {
        kullanicilarReference.child("Gruplar").child(grupAdi).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, grupAdi + " isimli grup oluşturuldu", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void kullaniciDurumGuncelleme(String durum) {
        //saati ve tarihi alıp veritabanına gönderme
        String kaydedilenAktifZaman, kaydedilenAktifTarih;

        Calendar calendar = Calendar.getInstance();

        //Tarih formatı
        SimpleDateFormat aktifTarih  = new SimpleDateFormat("dd, MMM, yyyy");
        kaydedilenAktifTarih = aktifTarih.format(calendar.getTime());

        //Saat Formatı
        SimpleDateFormat aktifZaman  = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman = aktifZaman.format(calendar.getTime());

        HashMap<String,Object> cevrimiciDurumuMap = new HashMap<>();
        cevrimiciDurumuMap.put("zaman",kaydedilenAktifZaman);
        cevrimiciDurumuMap.put("tarih",kaydedilenAktifTarih);
        cevrimiciDurumuMap.put("durum",durum);

        aktifKullaniciId = Yetki.getCurrentUser().getUid();
        kullanicilarReference.child("Kullanicilar").child(aktifKullaniciId).child("kullaniciDurumu").updateChildren(cevrimiciDurumuMap);
    }

}
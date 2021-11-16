package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.Adapter.GrupMesajAdapter;
import com.example.messageapp.Adapter.MesajAdapter;
import com.example.messageapp.Model.GrupMesajlar;
import com.example.messageapp.Model.Mesajlar;
import com.example.messageapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GrupChatActivity extends AppCompatActivity {

    private ImageButton grup_mesaj_gonder;
    private EditText grup_mesaj_girdi;
    private ImageButton DosyaGonderButton;
    private ImageView grupsGeriDon;
    private TextView grupAdiGoster;

    private final List<GrupMesajlar> grupMesajlarList = new ArrayList<>();
    private LinearLayoutManager gruplinearLayoutManager;
    private GrupMesajAdapter grupMesajAdapter;
    private RecyclerView grupMesajlarListesi;

    private FirebaseAuth Yetki;
    private DatabaseReference kullaniciYolu, grupAdiYolu, grupMesajAnahtariYolu;

    // Intent Değişkeni (gönderilen grup adını almak için)
    private String mevcutGrupAdi, aktifKullaniciId, aktifKullaniciAdi, aktifTarih, aktifZaman;
    private String IdMesajGonderen;

    private String kontrolcu = "", myUrl = "";
    private StorageTask yuklemeGorevi;
    private Uri dosyaUri;

    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);

        //Intenti al
        mevcutGrupAdi = getIntent().getExtras().get("grupAdı").toString();

        Yetki = FirebaseAuth.getInstance();
        aktifKullaniciId = Yetki.getCurrentUser().getUid();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);
        IdMesajGonderen = Yetki.getCurrentUser().getUid();

        grupMesajAdapter = new GrupMesajAdapter(grupMesajlarList);
        grupMesajlarListesi = findViewById(R.id.grupMesajlarListesi);
        gruplinearLayoutManager = new LinearLayoutManager(this);
        grupMesajlarListesi.setLayoutManager(gruplinearLayoutManager);
        grupMesajlarListesi.setAdapter(grupMesajAdapter);

        grup_mesaj_gonder = findViewById(R.id.grupMesajGonder);
        grup_mesaj_girdi = findViewById(R.id.grupMesajGirdisi);
        grupsGeriDon = findViewById(R.id.grupsGeriDon);
        grupAdiGoster = findViewById(R.id.grupAdiGoster);
        DosyaGonderButton = findViewById(R.id.DosyaGonderButton);

        yuklemeBar = new ProgressDialog(this);


        grupsGeriDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gruplar = new Intent(GrupChatActivity.this, MainActivity.class);
                startActivity(gruplar);
            }
        });


        grupAdiGoster.setText(mevcutGrupAdi);

        // Kullanıcı bilgisi alma
        kullaniciBilgisiAl();

        // Mesajı veritabanına kaydet
        grup_mesaj_gonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesajiDatabaseKaydet();
                //mesajı gönderdikten sonra mesaj yazılan kutuyu boşalt
                grup_mesaj_girdi.setText("");

            }
        });

        DosyaGonderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence secenekler [] = new CharSequence[]
                        {
                                "Resimler",
                                "PDF"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GrupChatActivity.this);
                builder.setTitle("Dosya Seç");
                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0)
                        {
                            kontrolcu = "resim";
                            //galeriyi açma
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Resim Seçiniz"), 438);
                        }
                        if(which == 1)
                        {
                            kontrolcu = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "PDF Seçiniz"), 438);
                        }

                    }
                });

                builder.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Calendar tarih = Calendar.getInstance();
        SimpleDateFormat aktifTarihFormat = new SimpleDateFormat("dd MMM yyyy");
        aktifTarih = aktifTarihFormat.format(tarih.getTime());

        Calendar zaman = Calendar.getInstance();
        SimpleDateFormat aktifZamanFormat = new SimpleDateFormat("hh:mm:ss a");
        aktifZaman = aktifZamanFormat.format(zaman.getTime());

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //Progress
            yuklemeBar.setTitle("Dosya gönderiliyor");
            yuklemeBar.setMessage("Lütfen Bekleyiniz");
            yuklemeBar.setCanceledOnTouchOutside(false);
            yuklemeBar.show();

            dosyaUri = data.getData();

            if (!kontrolcu.equals("resim")) {
                StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Dokuman Dosyalari");

                //;
                //final String mesajAlanYolu = "Gruplar";
                //mesajları alta alta yazması için
                grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);
                final String mesajAnahtari = grupAdiYolu.push().getKey();

                grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);

                //DatabaseReference kullaniciGrupMesajAnahtarYolu = grupAdiYolu.child(mesajAnahtari).push();

                //final String mesajEklemeId = kullaniciGrupMesajAnahtarYolu.getKey();

                final StorageReference dosyaYolu = depolamaYolu.child(mesajAnahtari + "." + kontrolcu);

                yuklemeGorevi = dosyaYolu.putFile(dosyaUri);
                yuklemeGorevi.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException(); //neyin hatalı yapıldığını gösteren uyarı mesajı
                        }

                        return dosyaYolu.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri indirmeUrl = task.getResult();
                            myUrl = indirmeUrl.toString();

                            Map mesajMetniGovdesi = new HashMap();
                            mesajMetniGovdesi.put("mesaj", myUrl);
                            mesajMetniGovdesi.put("dosyaAdi", dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur", kontrolcu);
                            mesajMetniGovdesi.put("kimden", IdMesajGonderen);
                            mesajMetniGovdesi.put("mesajId", mesajAnahtari);
                            mesajMetniGovdesi.put("zaman", aktifZaman);
                            mesajMetniGovdesi.put("tarih", aktifTarih);

                           /* Map mesajGovdesiDetaylari = new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId, mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId, mesajMetniGovdesi);*/

                            grupMesajAnahtariYolu.updateChildren(mesajMetniGovdesi);
                            yuklemeBar.dismiss();

                        }
                    }
                });
            }
            else if(kontrolcu.equals("resim"))
            {
                final StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Resim Dosyalari");

                /*final String mesajGonderenYolu = "Mesajlar/" + IdMesajGonderen + "/" + IdMesajiAlici;
                final String mesajAlanYolu = "Mesajlar/" + IdMesajiAlici + "/" + IdMesajGonderen;*/
                //mesajları alta alta yazması için
                //DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

                //final String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);
                final String mesajAnahtari = grupAdiYolu.push().getKey();

                grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);

                final StorageReference dosyaYolu = depolamaYolu.child(mesajAnahtari + "." + "jpg");

                yuklemeGorevi = dosyaYolu.putFile(dosyaUri);
                yuklemeGorevi.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException(); //neyin hatalı yapıldığını gösteren uyarı mesajı
                        }

                        return dosyaYolu.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri indirmeUrl = task.getResult();
                            myUrl = indirmeUrl.toString();

                            Map mesajMetniGovdesi = new HashMap();
                            mesajMetniGovdesi.put("mesaj", myUrl);
                            mesajMetniGovdesi.put("dosyaAdi", dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur", kontrolcu);
                            mesajMetniGovdesi.put("kimden", IdMesajGonderen);
                            mesajMetniGovdesi.put("mesajId", mesajAnahtari);
                            mesajMetniGovdesi.put("zaman", aktifZaman);
                            mesajMetniGovdesi.put("tarih", aktifTarih);

                            /*Map mesajGovdesiDetaylari =new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId,mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId,mesajMetniGovdesi);*/

                            grupMesajAnahtariYolu.updateChildren(mesajMetniGovdesi);
                            yuklemeBar.dismiss();

                        }
                    }
                });

            }
            else
            {
                yuklemeBar.dismiss();
                Toast.makeText(this, "Hata: Öge Seçilemedi..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        grupAdiYolu.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                GrupMesajlar grupmesajlar = snapshot.getValue(GrupMesajlar.class);   //burada veriler toplu alınır

                //modeli listeye ekleme
                grupMesajlarList.add(grupmesajlar);

                grupMesajAdapter.notifyDataSetChanged();  //anlık değişimde kendini günceller
                //Scrollview ayarlama
                grupMesajlarListesi.smoothScrollToPosition(grupMesajlarListesi.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void MesajiDatabaseKaydet() {

        grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);

        String mesaj = grup_mesaj_girdi.getText().toString();
        String mesajAnahtari = grupAdiYolu.push().getKey();  //çakışma olmasın alt satıra geçip anahtarı alsın

        //mesaj boşsa gönderme
        if(TextUtils.isEmpty(mesaj))
        {
            Toast.makeText(this, "Mesaj Alanı Boş Olamaz", Toast.LENGTH_LONG).show();
        }
        else
        {
            Calendar tarih = Calendar.getInstance();
            SimpleDateFormat aktifTarihFormat = new SimpleDateFormat("dd MMM yyyy");
            aktifTarih = aktifTarihFormat.format(tarih.getTime());

            Calendar zaman = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormat = new SimpleDateFormat("hh:mm:ss a");
            aktifZaman = aktifZamanFormat.format(zaman.getTime());

            HashMap<String,Object>grupMesajAnahtari = new HashMap<>();
            grupAdiYolu.updateChildren(grupMesajAnahtari);

            grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);  //veritabanına mesajlar hangi grubaysa onun altına gitmesi için bi yol


            HashMap<String,Object>mesajBilgisiMap = new HashMap<>();
            mesajBilgisiMap.put("kimden", aktifKullaniciId);
            mesajBilgisiMap.put("ad", aktifKullaniciAdi);
            mesajBilgisiMap.put("tur", "metin");
            mesajBilgisiMap.put("mesaj", mesaj);
            mesajBilgisiMap.put("tarih", aktifTarih);
            mesajBilgisiMap.put("zaman", aktifZaman);


            grupMesajAnahtariYolu.updateChildren(mesajBilgisiMap);
        }
    }

    private void kullaniciBilgisiAl() {
        kullaniciYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //veritabanında böyle idli biri varsa adını al
                if(snapshot.exists())
                {
                    aktifKullaniciAdi = snapshot.child("ad").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
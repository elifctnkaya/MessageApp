package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.Adapter.MesajAdapter;
import com.example.messageapp.Model.Mesajlar;
import com.example.messageapp.Model.Persons;
import com.example.messageapp.Notification.APIService;
import com.example.messageapp.Notification.Client;
import com.example.messageapp.Notification.Data;
import com.example.messageapp.Notification.MyResponse;
import com.example.messageapp.Notification.Sender;
import com.example.messageapp.Notification.Token;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private NotificationCompat.Builder builder;

    private String IdMesajiAlici, AdMesajiAlici, ResimMesajiAlici, IdMesajGonderen;

    private TextView kullaniciAdi, kullaniciSonGorulme;
    private CircleImageView kullaniciResmi;
    private ImageView chatGeriDon;

    private ImageButton mesajGonderButton, dosyaGonderButton;
    private EditText girilenMesajMetni;


    //Firebase
    private FirebaseAuth Yetki;
    private DatabaseReference mesajYolu, kullaniciYolu, reference, kullanicilarReference;

    private final List<Mesajlar> mesajlarList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MesajAdapter mesajAdapter;
    private RecyclerView kullaniciMesajlariListesi;

    private String kaydedilenAktifZaman, kaydedilenAktifTarih;
    private String kontrolcu = "", myUrl = "";
    private StorageTask yuklemeGorevi;
    private Uri dosyaUri;

    APIService apiService;
    Boolean notify = false;

    Map mesajMetniGovdesi = new HashMap();


    //Progress
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Chat fragmentten gelen Intenti almak
        IdMesajiAlici = getIntent().getExtras().get("kullanici_id_ziyaret").toString();
        AdMesajiAlici = getIntent().getExtras().get("kullanici_adi_ziyaret").toString();
        ResimMesajiAlici = getIntent().getExtras().get("kullanici_resim_ziyaret").toString();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        kullaniciAdi = findViewById(R.id.kullaniciAdiChat);
        kullaniciSonGorulme = findViewById(R.id.kullaniciSonGorulmeChat);
        kullaniciResmi = findViewById(R.id.kullaniciResmiChat);
        chatGeriDon = findViewById(R.id.chatGeriDon);
        mesajGonderButton = findViewById(R.id.MesajGonderButton);
        dosyaGonderButton = findViewById(R.id.DosyaGonderButton);
        girilenMesajMetni = findViewById(R.id.girilenMesaj);

        mesajAdapter = new MesajAdapter(mesajlarList);
        kullaniciMesajlariListesi = findViewById(R.id.kullaniciMesajlarListesi);
        linearLayoutManager = new LinearLayoutManager(this);
        kullaniciMesajlariListesi.setLayoutManager(linearLayoutManager);
        kullaniciMesajlariListesi.setAdapter(mesajAdapter);

        kullanicilarReference = FirebaseDatabase.getInstance().getReference();

        yuklemeBar = new ProgressDialog(this);

        //Takvim(mesajların saat ve tarihi için)
        Calendar calendar = Calendar.getInstance();
        //Tarih formatı
        SimpleDateFormat aktifTarih  = new SimpleDateFormat("dd, MMM, yyyy");
        kaydedilenAktifTarih = aktifTarih.format(calendar.getTime());
        //Saat Formatı
        SimpleDateFormat aktifZaman  = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman = aktifZaman.format(calendar.getTime());

        //Firebase
        Yetki = FirebaseAuth.getInstance();
        mesajYolu = FirebaseDatabase.getInstance().getReference();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference();
        IdMesajGonderen = Yetki.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();


        chatGeriDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sohbetler = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(sohbetler);
            }
        });


        // Kontrollere Intentle gelenleri aktarma
        kullaniciAdi.setText(AdMesajiAlici);       //resmi yoksa varsayılan resmi koyar
        Picasso.get().load(ResimMesajiAlici).placeholder(R.drawable.profile).into(kullaniciResmi);

        // Mesaj gönderme butonuna tıklandığında yapılacak olanlar
        mesajGonderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                MesajGonder();
            }
        });

        //dosya gönderme butonuna tıklandığında
        dosyaGonderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence secenekler [] = new CharSequence[]
                        {
                                "Resimler",
                                "PDF"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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
        //SonGorulmeGoster();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            //Progress
            yuklemeBar.setTitle("Dosya gönderiliyor");
            yuklemeBar.setMessage("Lütfen Bekleyiniz");
            yuklemeBar.setCanceledOnTouchOutside(false);
            yuklemeBar.show();

            dosyaUri = data.getData();

            if(!kontrolcu.equals("resim"))
            {
                StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Dokuman Dosyalari");

                final String mesajGonderenYolu = "Mesajlar/" + IdMesajGonderen + "/" + IdMesajiAlici;
                final String mesajAlanYolu = "Mesajlar/" + IdMesajiAlici + "/" + IdMesajGonderen;
                //mesajları alta alta yazması için
                DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

                final String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                final StorageReference dosyaYolu = depolamaYolu.child(mesajEklemeId + "." + kontrolcu);

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
                            mesajMetniGovdesi.put("ad", dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur", kontrolcu);
                            mesajMetniGovdesi.put("kimden", IdMesajGonderen);
                            mesajMetniGovdesi.put("kime", IdMesajiAlici);
                            mesajMetniGovdesi.put("mesajId", mesajEklemeId);
                            mesajMetniGovdesi.put("zaman", kaydedilenAktifZaman);
                            mesajMetniGovdesi.put("tarih", kaydedilenAktifTarih);

                            Map mesajGovdesiDetaylari =new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId,mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId,mesajMetniGovdesi);

                            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi..", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderme Başarısız..", Toast.LENGTH_SHORT).show();
                                    }

                                    girilenMesajMetni.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else if(kontrolcu.equals("resim"))
            {
                final StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Resim Dosyalari");

                final String mesajGonderenYolu = "Mesajlar/" + IdMesajGonderen + "/" + IdMesajiAlici;
                final String mesajAlanYolu = "Mesajlar/" + IdMesajiAlici + "/" + IdMesajGonderen;
                //mesajları alta alta yazması için
                DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

                final String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                final StorageReference dosyaYolu = depolamaYolu.child(mesajEklemeId + "." + "jpg");

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
                            mesajMetniGovdesi.put("ad", dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur", kontrolcu);
                            mesajMetniGovdesi.put("kimden", IdMesajGonderen);
                            mesajMetniGovdesi.put("kime", IdMesajiAlici);
                            mesajMetniGovdesi.put("mesajId", mesajEklemeId);
                            mesajMetniGovdesi.put("zaman", kaydedilenAktifZaman);
                            mesajMetniGovdesi.put("tarih", kaydedilenAktifTarih);

                            Map mesajGovdesiDetaylari =new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId,mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId,mesajMetniGovdesi);

                            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi..", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderme Başarısız..", Toast.LENGTH_SHORT).show();
                                    }

                                    girilenMesajMetni.setText("");
                                }
                            });
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


    private void SonGorulmeGoster(){

        Calendar calendar = Calendar.getInstance();
        //Tarih formatı
        SimpleDateFormat aktifTarih  = new SimpleDateFormat("dd, MMM, yyyy");
        kaydedilenAktifTarih = aktifTarih.format(calendar.getTime());
        //Saat Formatı
        SimpleDateFormat aktifZaman  = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman = aktifZaman.format(calendar.getTime());

        kullaniciYolu.child("Kullanicilar").child(IdMesajiAlici).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Veritabanından kullanıcı durumuna yönelik verileri çekme
                if(snapshot.child("kullaniciDurumu").hasChild("durum"))
                {
                    String durum = snapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                    String tarih = snapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                    String zaman = snapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                    if(durum.equals("Çevrimiçi"))
                    {
                        kullaniciSonGorulme.setText("Çevrimiçi");
                    }
                    else if(durum.equals("Çevrimdışı"))
                    {
                        kullaniciSonGorulme.setText("Son görülme: " + tarih + " " + zaman);
                    }
                }
                else
                {
                    kullaniciSonGorulme.setText("Çevrimdışı");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        SonGorulmeGoster();
        //veritabanından verileri çekme
        mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        //veritabanından veriyi alıp modele aktarma
                        Mesajlar mesajlar = snapshot.getValue(Mesajlar.class);   //burada veriler toplu alınır

                        //modeli listeye ekleme
                        mesajlarList.add(mesajlar);

                        mesajAdapter.notifyDataSetChanged();  //anlık değişimde kendini günceller
                        //Scrollview ayarlama
                        kullaniciMesajlariListesi.smoothScrollToPosition(kullaniciMesajlariListesi.getAdapter().getItemCount());
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


    private void MesajGonder() {
        //Mesajı kontrolden alma
        final String mesajMetni = girilenMesajMetni.getText().toString();

        if(TextUtils.isEmpty(mesajMetni))
        {   //mesaj yazılan alan boşsa uyarı verir
            Toast.makeText(this, "Boş Mesaj Gönderilemez..", Toast.LENGTH_SHORT).show();
        }
        else  //değilse firebase'e kaydeder
        {
            String mesajGonderenYolu = "Mesajlar/" + IdMesajGonderen + "/" + IdMesajiAlici;
            String mesajAlanYolu = "Mesajlar/" + IdMesajiAlici + "/" + IdMesajGonderen;
            //mesajları alta alta yazması için
            DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

            String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();


            mesajMetniGovdesi.put("mesaj", mesajMetni);
            mesajMetniGovdesi.put("tur", "metin");
            mesajMetniGovdesi.put("kimden", IdMesajGonderen);
            mesajMetniGovdesi.put("kime", IdMesajiAlici);
            mesajMetniGovdesi.put("mesajId", mesajEklemeId);
            mesajMetniGovdesi.put("zaman", kaydedilenAktifZaman);
            mesajMetniGovdesi.put("tarih", kaydedilenAktifTarih);

            Map mesajGovdesiDetaylari =new HashMap();
            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId,mesajMetniGovdesi);
            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId,mesajMetniGovdesi);

            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi..", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Mesaj Gönderme Başarısız..", Toast.LENGTH_SHORT).show();
                    }

                    girilenMesajMetni.setText("");
                }
            });

            reference = FirebaseDatabase.getInstance().getReference("Kullanicilar").child(Yetki.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Persons persons = snapshot.getValue(Persons.class);
                    if (notify) {
                        sendNotification(IdMesajiAlici, persons.getAd(), mesajMetni);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendNotification(final String IdMesajAlici, final String username, final String message)
    {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(IdMesajiAlici);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren())
                {
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(Yetki.getUid(), R.drawable.message, username+": " + mesajMetniGovdesi.get("mesaj"), "New Message", IdMesajAlici);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200 ){
                                        if(response.body().succes == 1){
                                            Toast.makeText(ChatActivity.this, "Hata", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
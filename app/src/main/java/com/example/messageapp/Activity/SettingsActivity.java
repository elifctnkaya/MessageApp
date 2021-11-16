package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update_setting;
    private EditText user_name_set, profile_status_set;
    private CircleImageView profile_photo_set;

    private FirebaseAuth Yetki;
    private DatabaseReference veriYoluu;
    private StorageReference kullaniciResimYolu;
    private StorageTask yuklemeGorevi;

    private String mevcutKullaniciId;

    //resim seçme
    private static final int GaleriSecme = 1;

    //progress dialog
    private ProgressDialog yukleniyor;

    //Uri
    Uri resimUri;
    String myUri = "";

    //Toolbar
    private Toolbar settingsToolbar;

    private ImageView mainGeriDon;
    private TextView settingGoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Yetki = FirebaseAuth.getInstance();
        mevcutKullaniciId = Yetki.getCurrentUser().getUid();
        veriYoluu = FirebaseDatabase.getInstance().getReference();
        kullaniciResimYolu = FirebaseStorage.getInstance().getReference().child("Profil Resimleri");


        update_setting = findViewById(R.id.updateSetting);
        user_name_set = findViewById(R.id.userNameSet);
        profile_status_set = findViewById(R.id.profileStatusSet);
        profile_photo_set = findViewById(R.id.profilePhotoSet);
        mainGeriDon = findViewById(R.id.mainGeriDon);
        settingGoster = findViewById(R.id.settingGoster);


        mainGeriDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ayarlar = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(ayarlar);
            }
        });



        //progress dialog
        yukleniyor = new ProgressDialog(this);

        update_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AyarlarıGuncelle();
            }
        });

        user_name_set.setVisibility(View.INVISIBLE);

        KullaniciBilgisiAl();

        profile_photo_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Crop(Kırpma activity) açma
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(SettingsActivity.this);
            }
        });
    }


    //dosyanın uzantısını ayarlama metodu
    private String dosyaUzantisiAl(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();  //içerik çözücü
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Resim Seçme kodu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            //resim seçiliyorsa yapılacak şey
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            resimUri = result.getUri();
            profile_photo_set.setImageURI(resimUri);   //seçtiğini alıp yuvarlak yerdeki profil resmi yapacak
        }
        else
        {
            //seçilmiyorsa yapılacak şey
            Toast.makeText(this, "Resim Seçilemedi", Toast.LENGTH_SHORT).show();
        }

    }

    private void KullaniciBilgisiAl() {
        veriYoluu.child("Kullanicilar").child(mevcutKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //veri varsa                //ad diye çocuk varsa              //resim diye çocuk varsa
                if((snapshot.exists()) && (snapshot.hasChild("ad") && (snapshot.hasChild("resim"))))
                {  //database'de olan verileri çekmek
                    String kullaniciAdiAl = snapshot.child("ad").getValue().toString();
                    String kullaniciDurumuAl = snapshot.child("durum").getValue().toString();
                    String kullaniciResmiAl = snapshot.child("resim").getValue().toString();

                    //veritabanından çekilen değerler ilgili yerlere yazılıyor
                    user_name_set.setText(kullaniciAdiAl);
                    profile_status_set.setText(kullaniciDurumuAl);
                    //resmi alıp ayarlarda gösterme
                    Picasso.get().load(kullaniciResmiAl).into(profile_photo_set);  //picasso kütüphanenin adı
                    user_name_set.setVisibility(View.VISIBLE);

                }
                else if((snapshot.exists()) && (snapshot.hasChild("ad")))
                {  //resim yoksa
                    String kullaniciAdiAl = snapshot.child("ad").getValue().toString();
                    String kullaniciDurumuAl = snapshot.child("durum").getValue().toString();

                    //veritabanından çekilen değerler ilgili yerlere yazılıyor
                    user_name_set.setText(kullaniciAdiAl);
                    profile_status_set.setText(kullaniciDurumuAl);
                    user_name_set.setVisibility(View.VISIBLE);

                }
                else   //hiçbiri yoksa
                {
                    user_name_set.setVisibility(View.VISIBLE);   // bilgisi yoksa ismi görüsün
                    Toast.makeText(SettingsActivity.this, "Lütfen Profil Bilgilerinizi Ayarlayın", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AyarlarıGuncelle() {
        String kullaniciAdiAyarla = user_name_set.getText().toString();
        String kullaniciDurumAyarla = profile_status_set.getText().toString();

        if(kullaniciAdiAyarla != null || kullaniciDurumAyarla != null)
        {
            Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
            startActivity(intent);
        }
        //ad ve durum boş girilirse uyarı verir
        if(TextUtils.isEmpty(kullaniciAdiAyarla))
        {
            Toast.makeText(this, "Adınızı Yazınız", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(kullaniciDurumAyarla))
        {
            Toast.makeText(this, "Durumunuzu Yazınız", Toast.LENGTH_SHORT).show();
        }
        else
        {
            resimYukle();
        }

    }

    private void resimYukle() {

        yukleniyor.setTitle("Bilgi Aktarma");
        yukleniyor.setMessage("Lütfen Bekleyiniz");
        yukleniyor.setCanceledOnTouchOutside(false);
        yukleniyor.show();

        if(resimUri == null)
        {
            DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

            String gonderiId = veriYolu.push().getKey(); // anahtarı alıp gönderiid değişkenine aktırır
            //iki değişkeni alıp veritabanına gönderecek
            String kullaniciAdiAl = user_name_set.getText().toString();
            String kullaniciDurumuAl = profile_status_set.getText().toString();

            HashMap<String,Object> profilAyari = new HashMap<>();
            profilAyari.put("uid",gonderiId);
            profilAyari.put("ad",kullaniciAdiAl);
            profilAyari.put("durum",kullaniciDurumuAl);

            veriYolu.child(mevcutKullaniciId).updateChildren(profilAyari);

            yukleniyor.dismiss();
        }
        else
        {
            final StorageReference resimYolu = kullaniciResimYolu.child(mevcutKullaniciId + "." + dosyaUzantisiAl(resimUri));

            yuklemeGorevi = resimYolu.putFile(resimUri);
            // şu görevle devam et
            yuklemeGorevi.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    //görev başarılı değilse
                    if(!task.isSuccessful())
                    {   //görev neden başarısız onu gösteren mesaj
                        throw task.getException();
                    }
                    //başarılı olursa resimYolundaki urlyi döndürür
                    return resimYolu.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                //görev tamamlandığında bunu yapar
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        //başarılıysa
                        Uri indirmeUrisi = task.getResult();
                        myUri = indirmeUrisi.toString();

                        //veritabanına yol ayarlama
                        DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

                        String gonderiId = veriYolu.push().getKey(); // anahtarı alıp gönderiid değişkenine aktırır
                        //iki değişkeni alıp veritabanına gönderecek
                        String kullaniciAdiAl = user_name_set.getText().toString();
                        String kullaniciDurumuAl = profile_status_set.getText().toString();

                        HashMap<String,Object> profilAyari = new HashMap<>();
                        profilAyari.put("uid",gonderiId);
                        profilAyari.put("ad",kullaniciAdiAl);
                        profilAyari.put("durum",kullaniciDurumuAl);
                        profilAyari.put("resim",myUri);

                        veriYolu.child(mevcutKullaniciId).updateChildren(profilAyari);

                        yukleniyor.dismiss();
                    }
                    else
                    {
                        //başarısızsa
                        String mesaj = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Hata: " + mesaj, Toast.LENGTH_SHORT).show();
                        yukleniyor.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                //başarısızsa bunu yapar
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(SettingsActivity.this, "Hata: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    yukleniyor.dismiss();
                }
            });
        }

    }
}
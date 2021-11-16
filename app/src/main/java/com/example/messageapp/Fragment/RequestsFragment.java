package com.example.messageapp.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messageapp.Model.Persons;
import com.example.messageapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private View RequestsView;

    private RecyclerView chatTalepleriList;

    //firebase
    private DatabaseReference SohbetTalepleriYolu, KullanicilarYolu, SohbetlerYolu;
    private FirebaseAuth Yetki;

    private String aktifKullaniciId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsView =  inflater.inflate(R.layout.fragment_requests, container, false);

        //Firebase
        Yetki = FirebaseAuth.getInstance();
        aktifKullaniciId = Yetki.getCurrentUser().getUid();
        SohbetTalepleriYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        KullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        SohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");

        //Recycler
        chatTalepleriList = RequestsView.findViewById(R.id.chatTalepleriList);
        chatTalepleriList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Persons> secenekler = new FirebaseRecyclerOptions.Builder<Persons>()
                .setQuery(SohbetTalepleriYolu.child(aktifKullaniciId),Persons.class)
                .build();

        FirebaseRecyclerAdapter<Persons,TaleplerViewHolder> adapter = new FirebaseRecyclerAdapter<Persons, TaleplerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final TaleplerViewHolder taleplerViewHolder, int i, @NonNull Persons persons) {


                //taleplerin hepsini alma
                final String kullanici_id_listesi = getRef(i).getKey();

                DatabaseReference talepTuruAl = getRef(i).child("talep_turu").getRef();
                talepTuruAl.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //eğer talep varsa(firebaseden bakılıyor)
                        if(snapshot.exists())
                        {
                            String tur = snapshot.getValue().toString();
                            if(tur.equals("alindi"))
                            {
                                KullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("resim"))
                                        {
                                            //veritabanından resim varsa resim çekip değişkene aktarma işlemi
                                            final String talepProfilResmi = snapshot.child("resim").getValue().toString();

                                            //çekilen resmi ilgili kontrole aktarma
                                            Picasso.get().load(talepProfilResmi).into(taleplerViewHolder.profilResmi);
                                        }
                                        //veritabanından verileri çekip değişkenlere aktarma işlemi
                                        final String talepKullaniciAdi = snapshot.child("ad").getValue().toString();
                                        final String talepKullaniciDurumu = snapshot.child("durum").getValue().toString();

                                        //çekilen verileri ilgili kontrollere aktarma
                                        taleplerViewHolder.kullaniciAdi.setText(talepKullaniciAdi);
                                        taleplerViewHolder.kullaniciDurumu.setText(" kullanıcı seninle iletişim kurmak istiyor");


                                        //istek geldikten sonra requests sayfasına düşen isteğin üzerine tıklayınca alertdialog çıkacak
                                        //her satıra tıklandığında
                                        taleplerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence secenekler[] = new CharSequence[]
                                                        {
                                                                "Kabul",     //kabul 0, iptal 1'dir
                                                                "İptal"
                                                        };
                                                //Alertdialog
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(talepKullaniciAdi + " Chat Talebi");

                                                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if(which == 0)    //kabulken bunu yapar
                                                        {
                                                            SohbetlerYolu.child(aktifKullaniciId).child(kullanici_id_listesi).child("Sohbetler").setValue("Kaydedildi")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                SohbetlerYolu.child(kullanici_id_listesi).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if(task.isSuccessful())
                                                                                                                    {
                                                                                                                        SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                                                                .removeValue()
                                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                        Toast.makeText(getContext(), "Sohbet Kaydedildi", Toast.LENGTH_LONG).show();
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

                                                        if(which == 1)    //iptalken bunu yapar
                                                        {
                                                            SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(getContext(), "Sohbet Silindi", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            //talep gönderildiğinde yapılacak olanlar(talepler ekranında görünecek olanlar)
                            else if(tur.equals("gonderildi"))
                            {

                                KullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("resim"))
                                        {
                                            //veritabanından resim varsa resim çekip değişkene aktarma işlemi
                                            final String talepProfilResmi = snapshot.child("resim").getValue().toString();

                                            //çekilen resmi ilgili kontrole aktarma
                                            Picasso.get().load(talepProfilResmi).into(taleplerViewHolder.profilResmi);
                                        }
                                        //veritabanından verileri çekip değişkenlere aktarma işlemi
                                        final String talepKullaniciAdi = snapshot.child("ad").getValue().toString();
                                        final String talepKullaniciDurumu = snapshot.child("durum").getValue().toString();

                                        //çekilen verileri ilgili kontrollere aktarma
                                        taleplerViewHolder.kullaniciAdi.setText(talepKullaniciAdi);
                                        taleplerViewHolder.kullaniciDurumu.setText(talepKullaniciAdi+" adlı kullanıcıya talep gönderdin");


                                        //istek geldikten sonra requests sayfasına düşen isteğin üzerine tıklayınca alertdialog çıkacak
                                        //her satıra tıklandığında
                                        taleplerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence secenekler[] = new CharSequence[]
                                                        {
                                                                "Chat Talebini İptal Et"
                                                        };
                                                //Alertdialog
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(talepKullaniciAdi + " Mevcut Chat Talebi Var");

                                                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if(which == 0)
                                                        {
                                                            SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(getContext(), "Chat Talebiniz Silindi", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public TaleplerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_gosterme_layout,parent,false);

                TaleplerViewHolder holder = new TaleplerViewHolder(view);

                return holder;
            }
        };

        chatTalepleriList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class TaleplerViewHolder extends RecyclerView.ViewHolder{

        TextView kullaniciAdi, kullaniciDurumu;
        CircleImageView profilResmi;


        public TaleplerViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullaniciAdi);
            kullaniciDurumu = itemView.findViewById(R.id.kullaniciDurumu);
            profilResmi = itemView.findViewById(R.id.kullaniciProfilResmi);
        }
    }

}
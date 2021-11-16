package com.example.messageapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.messageapp.Activity.ChatActivity;
import com.example.messageapp.Model.Persons;
import com.example.messageapp.Notification.Token;
import com.example.messageapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private View OzelSohbetlerView;
    private RecyclerView sohbetlerList;

    //Firebase
    private DatabaseReference sohbetYolu, kullaniciYolu;
    private FirebaseAuth Yetki;
    private String aktifKullaniciId;

    public ChatFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        OzelSohbetlerView =  inflater.inflate(R.layout.fragment_chat, container, false);

        //Firebase
        Yetki = FirebaseAuth.getInstance();
        aktifKullaniciId = Yetki.getCurrentUser().getUid();
        sohbetYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        //Recycler
        sohbetlerList = OzelSohbetlerView.findViewById(R.id.sohbetlerList);
        sohbetlerList.setLayoutManager(new LinearLayoutManager(getContext()));

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return OzelSohbetlerView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Persons> secenekler = new FirebaseRecyclerOptions.Builder<Persons>()
                .setQuery(sohbetYolu,Persons.class)
                .build();

        FirebaseRecyclerAdapter<Persons, SohbetlerViewHolder> adapter = new FirebaseRecyclerAdapter<Persons, SohbetlerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final SohbetlerViewHolder sohbetlerViewHolder, int i, @NonNull Persons persons) {

                //kullanicilardan veri alınacağı için böyle bir değişken oluşturuldu
                final String kullaniciIdleri = getRef(i).getKey();
                final String[] resimAl = {"Varsayılan Resim"};

                //veritabanından veri çağırma
                kullaniciYolu.child(kullaniciIdleri).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {
                            if(snapshot.hasChild("resim"))
                            {   // Resim varsa resmi al
                                resimAl[0] = snapshot.child("resim").getValue().toString();

                                //Veritabanından gelen resmi kontrole aktarma
                                Picasso.get().load(resimAl[0]).into(sohbetlerViewHolder.profilResmi);
                            }

                            final String adAl = snapshot.child("ad").getValue().toString();
                            //final String durumAl = snapshot.child("durum").getValue().toString();

                            //Veritabanından gelen ad kontrole aktarma
                            sohbetlerViewHolder.kullaniciAdi.setText(adAl);


                            // Veritabanından kullanıcı durumuna yönelik verileri çekme
                            if(snapshot.child("kullaniciDurumu").hasChild("durum"))
                            {
                                String durum = snapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                                String tarih = snapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                                String zaman = snapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                                if(durum.equals("Çevrimiçi"))
                                {
                                    sohbetlerViewHolder.kullaniciDurumu.setText("Çevrimiçi");
                                }
                                else if(durum.equals("Çevrimdışı"))
                                {
                                    sohbetlerViewHolder.kullaniciDurumu.setText("Son görülme: " + tarih + " " + zaman);
                                }
                            }
                            else
                            {
                                sohbetlerViewHolder.kullaniciDurumu.setText("Çevrimdışı");
                            }

                            //her satıra tıklandığında chat sayfasını açma
                            sohbetlerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //chat activity'sine gidecek ve intentle veri gönderecek
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("kullanici_id_ziyaret", kullaniciIdleri);
                                    chatIntent.putExtra("kullanici_adi_ziyaret", adAl);
                                    chatIntent.putExtra("kullanici_resim_ziyaret", resimAl[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public SohbetlerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_gosterme_layout,parent,false);

                return new SohbetlerViewHolder(view);
            }
        };

        sohbetlerList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    public static class SohbetlerViewHolder extends RecyclerView.ViewHolder
    {
        TextView kullaniciAdi, kullaniciDurumu;
        CircleImageView profilResmi;

        public SohbetlerViewHolder(@NonNull View itemView) {
            super(itemView);
            kullaniciAdi = itemView.findViewById(R.id.kullaniciAdi);
            kullaniciDurumu = itemView.findViewById(R.id.kullaniciDurumu);
            profilResmi = itemView.findViewById(R.id.kullaniciProfilResmi);
        }

    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(Yetki.getUid()).setValue(token1);
    }
}
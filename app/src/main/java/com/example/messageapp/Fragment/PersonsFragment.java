package com.example.messageapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.messageapp.Model.Persons;
import com.example.messageapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonsFragment extends Fragment {

    private View PersonsView;

    private RecyclerView personsList;

    //Firebase
    private DatabaseReference sohbetlerYolu, kullanicilarYolu;
    private FirebaseAuth Yetki;

    private String aktifKullaniciId;

    public PersonsFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PersonsView =  inflater.inflate(R.layout.fragment_persons, container, false);

        //Recycler
        personsList = PersonsView.findViewById(R.id.personsList);
        personsList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase
        Yetki = FirebaseAuth.getInstance();

        aktifKullaniciId = Yetki.getCurrentUser().getUid();

        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");


        return PersonsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //bu fragment başladığında yapılacaklar
        FirebaseRecyclerOptions secenekler = new FirebaseRecyclerOptions.Builder<Persons>()
                .setQuery(sohbetlerYolu, Persons.class)
                .build();

        //Adapter
        FirebaseRecyclerAdapter<Persons,PersonsViewHolder> adapter = new FirebaseRecyclerAdapter<Persons, PersonsViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final PersonsViewHolder personsViewHolder, int i, @NonNull Persons persons) {

                String tiklananKullaniciId = getRef(i).getKey();

                kullanicilarYolu.child(tiklananKullaniciId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {
                            // Veritabanından kullanıcı durumuna yönelik verileri çekme
                            if(snapshot.child("kullaniciDurumu").hasChild("durum"))
                            {
                                String durum = snapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                                String tarih = snapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                                String zaman = snapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                                if(durum.equals("Çevrimiçi"))
                                {
                                    personsViewHolder.cevrimIciIcon.setVisibility(View.VISIBLE);
                                }
                                else if(durum.equals("Çevrimdışı"))
                                {
                                    personsViewHolder.cevrimIciIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                personsViewHolder.cevrimIciIcon.setVisibility(View.INVISIBLE);
                            }

                            if(snapshot.hasChild("resim"))
                            {
                                //Verileri firebaseden çekme
                                String profilResmi = snapshot.child("resim").getValue().toString();
                                String kullaniciAdi = snapshot.child("ad").getValue().toString();
                                String kullaniciDurumu = snapshot.child("durum").getValue().toString();

                                //Kontrollere veri aktarımı
                                personsViewHolder.kullaniciAdi.setText(kullaniciAdi);
                                personsViewHolder.kullaniciDurumu.setText(kullaniciDurumu);
                                Picasso.get().load(profilResmi).placeholder(R.drawable.profile).into(personsViewHolder.profilResmi);
                            }
                            else
                            {
                                //Verileri firebaseden çekme
                                String kullaniciAdi = snapshot.child("ad").getValue().toString();
                                String kullaniciDurumu = snapshot.child("durum").getValue().toString();

                                //Kontrollere veri aktarımı
                                personsViewHolder.kullaniciAdi.setText(kullaniciAdi);
                                personsViewHolder.kullaniciDurumu.setText(kullaniciDurumu);
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
            public PersonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_gosterme_layout,parent,false);
                PersonsViewHolder viewHolder = new PersonsViewHolder(view);

                return viewHolder;
            }
        };

        personsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class PersonsViewHolder extends RecyclerView.ViewHolder{

        TextView kullaniciAdi, kullaniciDurumu;
        CircleImageView profilResmi;
        ImageView cevrimIciIcon;

        public PersonsViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullaniciAdi);
            kullaniciDurumu = itemView.findViewById(R.id.kullaniciDurumu);
            profilResmi = itemView.findViewById(R.id.kullaniciProfilResmi);
            cevrimIciIcon = itemView.findViewById(R.id.kullanicionline);

        }
    }
}
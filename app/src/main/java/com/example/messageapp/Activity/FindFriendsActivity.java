package com.example.messageapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.messageapp.Model.Persons;
import com.example.messageapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar xToolbar;
    private RecyclerView recycler_liste;

    //firebase
    private DatabaseReference kullaniciYolu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        //Recycler
        recycler_liste = findViewById(R.id.RecyclerListe);
        recycler_liste.setLayoutManager(new LinearLayoutManager(this));

        //Toolbar
        xToolbar = findViewById(R.id.arkadas_bul_toolbar);
        setSupportActionBar(xToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        //Firebase Tanımlama (yol için)
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

    }

    @Override
    protected void onStart() {
        //activity başladığında (verileri çekebilmek için)
        super.onStart();

        //Sorgu yapılıyor
        FirebaseRecyclerOptions<Persons> secenekler =
                new FirebaseRecyclerOptions.Builder<Persons>()
                        .setQuery(kullaniciYolu, Persons.class)
                        .build();

        FirebaseRecyclerAdapter<Persons,ArkadasBulViewHolder> adapter = new FirebaseRecyclerAdapter<Persons, ArkadasBulViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull ArkadasBulViewHolder arkadasBulViewHolder, final int i, @NonNull Persons persons) {

                arkadasBulViewHolder.kullaniciAdi.setText(persons.getAd());
                arkadasBulViewHolder.kullaniciDurumu.setText(persons.getDurum());
                Picasso.get().load(persons.getResim()).placeholder(R.drawable.profile).into(arkadasBulViewHolder.kullaniciProfilResmi);

                //her kişiye tıklandığında o kişinin profiline gidilecek
                arkadasBulViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String tiklananKullaniciIdGoster = getRef(i).getKey();

                        Intent profilIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profilIntent.putExtra("tiklananKullaniciIdGoster",tiklananKullaniciIdGoster);
                        startActivity(profilIntent);
                    }
                });

            }

            @NonNull
            @Override
            public ArkadasBulViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_gosterme_layout, parent, false);    //her satırda ne olsun ona bağlanıyor
                ArkadasBulViewHolder viewHolder = new ArkadasBulViewHolder(view);

                return viewHolder;
            }
        };

        recycler_liste.setAdapter(adapter);
        adapter.notifyDataSetChanged();    //hemen yenilenmesi için
        adapter.startListening();

    }

    public static class ArkadasBulViewHolder extends RecyclerView.ViewHolder
    {
        TextView kullaniciAdi, kullaniciDurumu;
        CircleImageView kullaniciProfilResmi;

        public ArkadasBulViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullaniciAdi);
            kullaniciDurumu = itemView.findViewById(R.id.kullaniciDurumu);
            kullaniciProfilResmi = itemView.findViewById(R.id.kullaniciProfilResmi);

        }
    }
}
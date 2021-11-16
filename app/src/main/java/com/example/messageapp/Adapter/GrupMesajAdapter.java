package com.example.messageapp.Adapter;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messageapp.Model.GrupMesajlar;
import com.example.messageapp.Model.Mesajlar;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class GrupMesajAdapter extends RecyclerView.Adapter<GrupMesajAdapter.GrupMesajlarViewHolder>  {

    private List<GrupMesajlar> grupMesajlarListesi;

    private FirebaseAuth Yetki;
    private DatabaseReference kullanicilarYolu;

    public GrupMesajAdapter(List<GrupMesajlar> grupMesajlarListesi){

        this.grupMesajlarListesi = grupMesajlarListesi;
    }

    public class GrupMesajlarViewHolder extends RecyclerView.ViewHolder{


        public TextView gonderenGrupMesajMetni, aliciGrupMesajMetni;
        public CircleImageView grupProfilResmi;
        public ImageView grupMesajGonderenImageView, grupMesajAlanImageView;

        public GrupMesajlarViewHolder(@NonNull View itemView) {
            super(itemView);

            //ozel grup mesajlar layouttaki kontrol tanımlamaları
            gonderenGrupMesajMetni = itemView.findViewById(R.id.gonderenGrupMesajMetni);
            aliciGrupMesajMetni = itemView.findViewById(R.id.aliciGrupMesajMetni);
            grupProfilResmi = itemView.findViewById(R.id.grupProfilResmi);
            grupMesajGonderenImageView = itemView.findViewById(R.id.grupMesajGonderenImageView);
            grupMesajAlanImageView = itemView.findViewById(R.id.grupMesajAlanImageView);
        }
    }

    @NonNull
    @Override
    public GrupMesajAdapter.GrupMesajlarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ozel_grup_mesajlar_layout,parent,false);

        //Firebase tanımlama
        Yetki = FirebaseAuth.getInstance();

        return new GrupMesajlarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GrupMesajlarViewHolder holder, final int position) {
        String mesajGonderenId = Yetki.getCurrentUser().getUid();
        System.out.println(mesajGonderenId);

        GrupMesajlar Grupmesajlar = grupMesajlarListesi.get(position);

        String kimdenKullaniciId = Grupmesajlar.getKimden();
        String kimdenMesajTuru = Grupmesajlar.getTur();


        kullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(kimdenKullaniciId);

        kullanicilarYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("resim")) { //veritabanından resmi değişkene aktarma
                    String resimAlici = snapshot.child("resim").getValue().toString();

                    //kontrole resmi aktarma
                    Picasso.get().load(resimAlici).placeholder(R.drawable.profile).into(holder.grupProfilResmi);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //gorunmez yapma
        holder.aliciGrupMesajMetni.setVisibility(View.GONE);
        holder.grupProfilResmi.setVisibility(View.GONE);
        holder.gonderenGrupMesajMetni.setVisibility(View.GONE);
        holder.grupMesajGonderenImageView.setVisibility(View.GONE);
        holder.grupMesajAlanImageView.setVisibility(View.GONE);

        if (kimdenMesajTuru.equals("metin")) {
            if (kimdenKullaniciId.equals(mesajGonderenId)) {
                holder.gonderenGrupMesajMetni.setVisibility(View.VISIBLE);   //gönderen kendisiyse görünür yapmak
                holder.gonderenGrupMesajMetni.setBackgroundResource(R.drawable.gonderen_mesajlari_layout);
                holder.gonderenGrupMesajMetni.setTextColor(Color.BLACK);
                holder.gonderenGrupMesajMetni.setText(Grupmesajlar.getMesaj() + "\n\n" + Grupmesajlar.getZaman() + "-" + Grupmesajlar.getTarih());
            }
            else
            {
                //görünür yapma
                holder.grupProfilResmi.setVisibility(View.VISIBLE);
                holder.aliciGrupMesajMetni.setVisibility(View.VISIBLE);

                holder.aliciGrupMesajMetni.setBackgroundResource(R.drawable.alici_mesajlari_layout);
                holder.aliciGrupMesajMetni.setTextColor(Color.BLACK);
                holder.aliciGrupMesajMetni.setText(Grupmesajlar.getAd()+ ":\n" + Grupmesajlar.getMesaj()+ "\n\n" + Grupmesajlar.getZaman()+ "-" + Grupmesajlar.getTarih());
            }
        }
        else if (kimdenMesajTuru.equals("resim"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId)) {
                holder.grupMesajGonderenImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(Grupmesajlar.getMesaj()).into(holder.grupMesajGonderenImageView);
            }
            else {
                holder.grupProfilResmi.setVisibility(View.VISIBLE);
                holder.grupMesajAlanImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(Grupmesajlar.getMesaj()).into(holder.grupMesajAlanImageView);
            }
        }

        else if(kimdenMesajTuru.equals("pdf"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                holder.grupMesajGonderenImageView.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-2c016.appspot.com/o/dosya.png?alt=media&token=6dc63eb7-8eb8-42f9-b2c4-846905a1c869")
                        .into(holder.grupMesajGonderenImageView);

            }
            else
            {
                holder.grupProfilResmi.setVisibility(View.VISIBLE);
                holder.grupMesajAlanImageView.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-2c016.appspot.com/o/dosya.png?alt=media&token=6dc63eb7-8eb8-42f9-b2c4-846905a1c869")
                        .into(holder.grupMesajAlanImageView);

            }
        }

    }




    @Override
    public int getItemCount() {
        return grupMesajlarListesi.size();
    }
}

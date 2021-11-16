package com.example.messageapp.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messageapp.Activity.MainActivity;
import com.example.messageapp.Activity.PictureShowActivity;
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

public class MesajAdapter extends RecyclerView.Adapter<MesajAdapter.MesajlarViewHolder> {

    private List<Mesajlar> kullaniciMesajlarListesi;

    //Firebase
    private FirebaseAuth Yetki;
    private DatabaseReference kullanicilarYolu;

    //Adapter
    public MesajAdapter (List<Mesajlar> kullaniciMesajlarListesi)
    {
        this.kullaniciMesajlarListesi = kullaniciMesajlarListesi;

    }

    //ViewHolder
    public class MesajlarViewHolder extends RecyclerView.ViewHolder
    {
        //ozel mesajlar layouttaki kontroller
        public TextView gonderenMesajMetni, aliciMesajMetni;
        public CircleImageView mesajProfilResmi;
        public ImageView mesajGonderenResim, mesajAlanResim;

        public MesajlarViewHolder(@NonNull View itemView) {
            super(itemView);

            //ozel mesajlar layouttaki kontrol tanımlamaları
            gonderenMesajMetni = itemView.findViewById(R.id.gonderenMesajMetni);
            aliciMesajMetni = itemView.findViewById(R.id.aliciMesajMetni);
            mesajProfilResmi = itemView.findViewById(R.id.mesajProfilResmi);
            mesajGonderenResim = itemView.findViewById(R.id.mesajGonderenImageView);
            mesajAlanResim = itemView.findViewById(R.id.mesajAlanImageView);

        }
    }
    @NonNull
    @Override
    public MesajlarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ozel_mesajlar_layout,parent,false);

        //Firebase tanımlama
        Yetki = FirebaseAuth.getInstance();

        return new MesajlarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MesajlarViewHolder holder, final int position)  {
        String mesajGonderenId = Yetki.getCurrentUser().getUid();

        // model tanımlama
        Mesajlar mesajlar = kullaniciMesajlarListesi.get(position);

        String kimdenKullaniciId = mesajlar.getKimden();
        String kimdenMesajTuru = mesajlar.getTur();

        //Veritabanı yolu
        kullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(kimdenKullaniciId);

        //Firebaseden veri çekme
        kullanicilarYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild("resim"))
                { //veritabanından resmi değişkene aktarma
                    String resimAlici = snapshot.child("resim").getValue().toString();

                    //kontrole resmi aktarma
                    Picasso.get().load(resimAlici).placeholder(R.drawable.profile).into(holder.mesajProfilResmi);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //görünmez yapma
        holder.aliciMesajMetni.setVisibility(View.GONE);
        holder.mesajProfilResmi.setVisibility(View.GONE);
        holder.gonderenMesajMetni.setVisibility(View.GONE);
        holder.mesajGonderenResim.setVisibility(View.GONE);
        holder.mesajAlanResim.setVisibility(View.GONE);

        if(kimdenMesajTuru.equals("metin"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                holder.gonderenMesajMetni.setVisibility(View.VISIBLE);   //gönderen kendisiyse görünür yapmak
                holder.gonderenMesajMetni.setBackgroundResource(R.drawable.gonderen_mesajlari_layout);
                holder.gonderenMesajMetni.setTextColor(Color.BLACK);
                holder.gonderenMesajMetni.setText(mesajlar.getMesaj()+ "\n\n" + mesajlar.getZaman()+ "-" + mesajlar.getTarih());
            }
            else
            {
                //görünür yapma
                holder.mesajProfilResmi.setVisibility(View.VISIBLE);
                holder.aliciMesajMetni.setVisibility(View.VISIBLE);

                holder.aliciMesajMetni.setBackgroundResource(R.drawable.alici_mesajlari_layout);
                holder.aliciMesajMetni.setTextColor(Color.BLACK);
                holder.aliciMesajMetni.setText(mesajlar.getMesaj()+ "\n\n" + mesajlar.getZaman()+ "-" + mesajlar.getTarih());
            }
        }

        else if(kimdenMesajTuru.equals("resim"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                holder.mesajGonderenResim.setVisibility(View.VISIBLE);
                Picasso.get().load(mesajlar.getMesaj()).into(holder.mesajGonderenResim);
            }
            else
            {
                holder.mesajProfilResmi.setVisibility(View.VISIBLE);
                holder.mesajAlanResim.setVisibility(View.VISIBLE);
                Picasso.get().load(mesajlar.getMesaj()).into(holder.mesajAlanResim);
            }
        }

        else if(kimdenMesajTuru.equals("pdf"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                holder.mesajGonderenResim.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-2c016.appspot.com/o/dosya.png?alt=media&token=6dc63eb7-8eb8-42f9-b2c4-846905a1c869")
                        .into(holder.mesajGonderenResim);

            }
            else
            {
                holder.mesajProfilResmi.setVisibility(View.VISIBLE);
                holder.mesajAlanResim.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-2c016.appspot.com/o/dosya.png?alt=media&token=6dc63eb7-8eb8-42f9-b2c4-846905a1c869")
                        .into(holder.mesajAlanResim);

            }
        }

        if(kimdenKullaniciId.equals(mesajGonderenId))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(kullaniciMesajlarListesi.get(position).getTur().equals("pdf"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu Belgeyi İndir ve Görüntüle",
                                        "İptal",
                                        "Herkesten Sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    gonderilenMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if(which == 1)
                                {
                                    //bu belgeyi indir ve görüntüle
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajlarListesi.get(position).getMesaj()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if(which == 2)
                                {
                                    //iptal

                                }
                                if(which == 3)
                                {
                                    //herkesten sil
                                    mesajiHerkestenSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });
                        builder.show();
                    }
                    else if(kullaniciMesajlarListesi.get(position).getTur().equals("metin"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "İptal",
                                        "Herkesten Sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    gonderilenMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(which == 1)
                                {
                                    //İptal
                                }
                                else if(which == 2)
                                {
                                    //herkesten sil
                                    mesajiHerkestenSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if(kullaniciMesajlarListesi.get(position).getTur().equals("resim"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu Resmi Görüntüle",
                                        "İptal",
                                        "Herkesten Sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    gonderilenMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 1)
                                {
                                    //Bu resmi görüntüle
                                    Intent intent = new Intent(holder.itemView.getContext(), PictureShowActivity.class);
                                    intent.putExtra("url",kullaniciMesajlarListesi.get(position).getMesaj());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 3)
                                {
                                    //herkesten sil
                                    mesajiHerkestenSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                }
            });
        }

        else
        {  //alan kişi kısmı
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(kullaniciMesajlarListesi.get(position).getTur().equals("pdf"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu Belgeyi İndir ve Görüntüle",
                                        "İptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    alinanMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(which == 1)
                                {
                                    //bu belgeyi indir ve görüntüle
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajlarListesi.get(position).getMesaj()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if(which == 2)
                                {
                                    //iptal

                                }
                            }
                        });
                        builder.show();
                    }
                    else if(kullaniciMesajlarListesi.get(position).getTur().equals("metin"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "İptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    alinanMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(kullaniciMesajlarListesi.get(position).getTur().equals("resim"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu Resmi Görüntüle",
                                        "İptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0)
                                {
                                    //Benden Sil
                                    alinanMesajiSil(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if(which == 1)
                                {
                                    //Bu resmi görüntüle
                                    Intent intent = new Intent(holder.itemView.getContext(), PictureShowActivity.class);
                                    intent.putExtra("url",kullaniciMesajlarListesi.get(position).getMesaj());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return kullaniciMesajlarListesi.size();
    }

    private void gonderilenMesajiSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlarListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlarListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlarListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme İşlemi Başarılı", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme Hatası", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void alinanMesajiSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlarListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlarListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlarListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme İşlemi Başarılı", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme Hatası", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mesajiHerkestenSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        final DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlarListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlarListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlarListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    mesajYolu.child("Mesajlar")
                            .child(kullaniciMesajlarListesi.get(pozisyon).getKimden())
                            .child(kullaniciMesajlarListesi.get(pozisyon).getKime())
                            .child(kullaniciMesajlarListesi.get(pozisyon).getMesajId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Silme İşlemi Başarılı", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme Hatası", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
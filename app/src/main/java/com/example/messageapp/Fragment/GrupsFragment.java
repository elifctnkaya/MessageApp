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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.messageapp.Activity.ChatActivity;
import com.example.messageapp.Activity.GrupChatActivity;
import com.example.messageapp.Model.Grup;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupsFragment extends Fragment {

    private View grupCerceveView;
    private ListView listView;
    private ArrayAdapter<String>arrayAdapter;
    private ArrayList<String>grupListeleri = new ArrayList<>();
    //private RecyclerView gruplarList;


    private DatabaseReference grupYolu;
    private FirebaseAuth Yetki;
    private String aktifKullaniciId;

    public GrupsFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        grupCerceveView =  inflater.inflate(R.layout.fragment_grups, container, false);

        //Firebase tanımlama
        Yetki = FirebaseAuth.getInstance();
        grupYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar");

        //Recycler
        // //gruplarList = grupCerceveView.findViewById(R.id.gruplarList);
        //gruplarList.setLayoutManager(new LinearLayoutManager(getContext()));


        listView = grupCerceveView.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, grupListeleri);
        listView.setAdapter(arrayAdapter);

        //grupları alma kodları
        GruplarıAlGoster();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //parent adapter'ı temsil ediyor
                String mevcutGrupAdi = parent.getItemAtPosition(position).toString();

                Intent grupChatActivity = new Intent(getContext(), GrupChatActivity.class);
                grupChatActivity.putExtra("grupAdı", mevcutGrupAdi); //grup adını gönderme
                startActivity(grupChatActivity);
            }
        });


        return grupCerceveView;
    }

    /*@Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Grup> secenekler = new FirebaseRecyclerOptions.Builder<Grup>()
                .setQuery(grupYolu,Grup.class)
                .build();

        final FirebaseRecyclerAdapter<Grup, GruplarViewHolder> adapter = new FirebaseRecyclerAdapter<Grup, GruplarViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final GruplarViewHolder gruplarViewHolder, int i, @NonNull Grup grup) {

                grupYolu.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            final String adAl = snapshot.getValue().toString();

                            //Veritabanından gelen ad kontrole aktarma
                            gruplarViewHolder.grupAdi.setText(adAl);


                            //her satıra tıklandığında chat sayfasını açma
                            gruplarViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), GrupChatActivity.class);
                                    chatIntent.putExtra("kullanici_adi_ziyaret", adAl);
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
            public GruplarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }
        };
    }


    public static class GruplarViewHolder extends RecyclerView.ViewHolder
    {
        TextView grupAdi;
        CircleImageView grupProfilResmi;

        public GruplarViewHolder(@NonNull View itemView) {
            super(itemView);
            grupAdi = itemView.findViewById(R.id.grupAdi);
            grupProfilResmi = itemView.findViewById(R.id.grupProfilResmi);
        }

    }*/

    private void GruplarıAlGoster() {
        grupYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String>set = new HashSet<>();
                //verileri satır satır alma işlemi(satır satır işlemini iterator yapıyor)
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    //hashsete ekle (iterator bir sonrakine giderken) anahtarı
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                //veriler üst üste yazılmasın diye
                grupListeleri.clear();
                grupListeleri.addAll(set);
                arrayAdapter.notifyDataSetChanged();  //eş zamanlı yenileme için

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.example.messageapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.messageapp.R;
import com.squareup.picasso.Picasso;

public class PictureShowActivity extends AppCompatActivity {
    private ImageView resimGoruntuleyici;
    private String resimUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);

        resimGoruntuleyici = findViewById(R.id.resimGoruntuleyici);
        resimUrl = getIntent().getStringExtra("url");

        Picasso.get().load(resimUrl).into(resimGoruntuleyici);

    }
}
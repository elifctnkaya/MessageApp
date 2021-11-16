package com.example.messageapp.Model;

public class Persons {

    //çoklu veri çekmek için oluşturulan bir sınıf

    String ad, durum, resim;    //firebasede yazan isimlendirme ile aynı olmalı

    public Persons(){
    }

    public Persons(String ad, String durum, String resim) {
        this.ad = ad;
        this.durum = durum;
        this.resim = resim;
    }


    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }
}

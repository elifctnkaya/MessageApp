package com.example.messageapp.Model;

public class GrupMesajlar {

    String  kimden, ad, mesaj, tarih, tur, zaman;

    public GrupMesajlar(){

    }

    public GrupMesajlar(String kimden, String ad, String mesaj, String tarih, String tur, String zaman) {
        this.kimden = kimden;
        this.ad = ad;
        this.mesaj = mesaj;
        this.tarih = tarih;
        this.tur = tur;
        this.zaman = zaman;
    }

    public String getKimden() {
        return kimden;
    }

    public void setKimden(String kimden) {
        this.kimden = kimden;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }

    public String getTur() {
        return tur;
    }

    public void setTur(String tur) {
        this.tur = tur;
    }

    public String getZaman() {
        return zaman;
    }

    public void setZaman(String zaman) {
        this.zaman = zaman;
    }
}

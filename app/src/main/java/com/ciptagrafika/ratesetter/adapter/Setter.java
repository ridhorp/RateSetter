package com.ciptagrafika.ratesetter.adapter;

/**
 * Created by IT on 10/31/2017.
 */

public class Setter {
    private String foto, nama, quotes, id;

    public Setter(String foto, String nama, String quotes, String id) {
        this.foto = foto;
        this.nama = nama;
        this.quotes = quotes;
        this.id = id;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getQuotes() {
        return quotes;
    }

    public void setQuotes(String quotes) {
        this.quotes = quotes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

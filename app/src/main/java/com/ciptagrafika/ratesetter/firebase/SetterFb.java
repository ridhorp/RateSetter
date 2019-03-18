package com.ciptagrafika.ratesetter.firebase;

/**
 * Created by IT on 10/31/2017.
 */

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SetterFb {
    public String nama, foto, quotes, id;
    public Double rate;


    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public SetterFb() {
    }

    public SetterFb(String nama, String foto, String quotes, String id, Double rate) {
        this.nama = nama;
        this.foto = foto;
        this.quotes = quotes;
        this.id = id;
        this.rate = rate;
    }
}

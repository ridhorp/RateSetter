package com.ciptagrafika.ratesetter.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Setting {
    public int jmlsetter;


    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Setting() {
    }

    public Setting(int jmlsetter) {
        this.jmlsetter = jmlsetter;
    }
}

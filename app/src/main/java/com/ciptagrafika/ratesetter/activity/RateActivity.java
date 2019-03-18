package com.ciptagrafika.ratesetter.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ciptagrafika.ratesetter.R;
import com.ciptagrafika.ratesetter.adapter.AdapterSetter;
import com.ciptagrafika.ratesetter.adapter.Setter;
import com.ciptagrafika.ratesetter.firebase.SetterFb;
import com.ciptagrafika.ratesetter.firebase.Setting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RateActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView rvSetter;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ArrayList<Setter> setters = new ArrayList<>();
    private AdapterSetter adapter;
    private int jmlSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        rvSetter = (RecyclerView) findViewById(R.id.rv_setter);
        rvSetter.setLayoutManager(new LinearLayoutManager(this));
        rvSetter.setItemAnimator(new DefaultItemAnimator());
        adapter = new AdapterSetter(this, setters);

        retrieveFromFirebase();
    }

    private void retrieveFromFirebase() {

        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'blanko' node
        mFirebaseDatabase = mFirebaseInstance.getReference("setting");
        mFirebaseDatabase.child("setting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Setting setting = dataSnapshot.getValue(Setting.class);

                // Check for null
                if (setting == null) {
                    Log.e(TAG, "Firebase Respon, Setting data is null!");
                    return;
                }

                jmlSetter = setting.jmlsetter;
                Log.d(TAG, "Firebase Respon, jml setter:" + setting.jmlsetter);
                retrievetoAdapter(jmlSetter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Firebase Respon, Failed to read setting", error.toException());
            }
        });
    }

    private void retrievetoAdapter(int jmlMenu) {
        setters.clear();
        for (int cc = 0; cc <= jmlMenu; cc++) {

            mFirebaseInstance = FirebaseDatabase.getInstance();
            // get reference to 'blanko' node
            mFirebaseDatabase = mFirebaseInstance.getReference("setter");
            mFirebaseDatabase.child("setter" + cc).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SetterFb set = dataSnapshot.getValue(SetterFb.class);

                    // Check for null
                    if (set == null) {
                        Log.e(TAG, "Firebase Respon, Setter data is null!");
                        return;
                    }

                    String foto = set.foto;
                    String nama = set.nama;
                    String quotes = set.quotes;
                    String id = set.id;

                    addtoAdapter(foto, nama, quotes, id);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "Firebase Respon, Failed to read setter", error.toException());
                }
            });
        }
    }

    void addtoAdapter(String foto, String nama, String quotes, String id) {

        Setter p = new Setter(foto, nama, quotes, id);
        Log.d(TAG, "Firebase Respon, " + nama + " add to adapter");
        setters.add(p);
        rvSetter.setAdapter(adapter);
    }
}

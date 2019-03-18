package com.ciptagrafika.ratesetter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ciptagrafika.ratesetter.R;
import com.ciptagrafika.ratesetter.firebase.SetterFb;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RatingActivity extends AppCompatActivity {

    private static final String TAG = RatingActivity.class.getSimpleName();
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String foto, nama, quotes, id;
    private ImageView fotoSetter;
    private TextView txtQuotes, txtNama;
    private RatingBar rateBar;
    private Button btnRate;
    private double rate = 0.0, rate2 = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        Intent in = getIntent();
        foto = in.getStringExtra("foto");
        nama = in.getStringExtra("nama");
        id = in.getStringExtra("id");

        fotoSetter = (ImageView) findViewById(R.id.foto_setter);
        txtNama = (TextView) findViewById(R.id.txt_nama_setter2);
        txtQuotes = (TextView) findViewById(R.id.txt_quotes_setter2);
        rateBar = (RatingBar) findViewById(R.id.ratingBar);
        btnRate = (Button) findViewById(R.id.btn_rate);

        txtNama.setText(nama);
        txtQuotes.setText(quotes);
        Glide.with(RatingActivity.this).load(foto).into(fotoSetter);

        cekRate();

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rate2 = rateBar.getRating();
                rate = rate + rate2;
                //Toast.makeText(RatingActivity.this, "Rate Value: "+rate, Toast.LENGTH_SHORT).show();

                savetoFirebase(rate);
            }
        });
    }

    private void cekRate() {
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'blanko' node
        mFirebaseDatabase = mFirebaseInstance.getReference("setter");
        mFirebaseDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SetterFb set = dataSnapshot.getValue(SetterFb.class);

                // Check for null
                if (set == null) {
                    Log.e(TAG, "Firebase Respon, Setter data is null!");
                    return;
                }

                rate = set.rate;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Firebase Respon, Failed to read setter", error.toException());
            }
        });
    }

    private void savetoFirebase(final double rate) {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("setter");
        myRef.child(id).child("rate").setValue(rate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Firebase Respon, " + rate + " add to firebase");
                Intent intent;
                intent = new Intent(RatingActivity.this, MainActivity.class);
                Toast.makeText(RatingActivity.this, "Terimakasih Atas Partisipasinya", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });


    }

}

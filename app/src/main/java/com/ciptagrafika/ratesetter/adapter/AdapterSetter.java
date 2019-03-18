package com.ciptagrafika.ratesetter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ciptagrafika.ratesetter.R;
import com.ciptagrafika.ratesetter.activity.RatingActivity;

import java.util.List;

/**
 * Created by IT on 10/31/2017.
 */

public class AdapterSetter extends RecyclerView.Adapter<AdapterSetter.MyViewHolder> {

    private Context mContext;
    private List<Setter> albumList;
    private String nama, quotes;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgProfil;
        public TextView txtNama, txtQuotes;
        public RatingBar rateSetter;
        public Button btnRate;

        public MyViewHolder(View view) {
            super(view);
            imgProfil = (ImageView) view.findViewById(R.id.img_profil);
            txtNama = (TextView) view.findViewById(R.id.txt_nama_setter);
            txtQuotes = (TextView) view.findViewById(R.id.txt_quotes_setter);
            //btnRate = (Button) view.findViewById(R.id.btn_rate);
            rateSetter = (RatingBar) view.findViewById(R.id.ratingBar);

        }
    }

    public AdapterSetter(Context mContext, List<Setter> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_setter, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Setter set = albumList.get(position);

        nama = set.getNama();
        quotes = set.getQuotes();

        holder.txtNama.setText(nama);
        holder.txtQuotes.setText(quotes);

        // loading album cover using Glide library
        Glide.with(mContext).load(set.getFoto()).into(holder.imgProfil);


        holder.imgProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent in = new Intent(mContext, RatingActivity.class);
                in.putExtra("foto", set.getFoto());
                in.putExtra("nama", set.getNama());
                in.putExtra("quotes", set.getQuotes());
                in.putExtra("id", set.getId());
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(in);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}

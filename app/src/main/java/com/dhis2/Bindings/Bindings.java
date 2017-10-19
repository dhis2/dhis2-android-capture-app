package com.dhis2.Bindings;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 * Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    @BindingAdapter("date")
    public static void setDate(TextView textView, String date) {
        SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat formatOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date dateIn = formatIn.parse(date);
            String dateOut = formatOut.format(dateIn);
            textView.setText(dateOut);
        } catch (ParseException e) {
            Timber.e(e);
        }

    }

    @BindingAdapter("initGrid")
    public static void setLayoutManager(RecyclerView recyclerView, boolean addLayout) {
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 4));
    }

    @BindingAdapter("randomColor")
    public static void setRandomColor(ImageView imageView, boolean addRandomColor) {
        Random rnd = new Random();

        int red = 255;
        int green = 255;
        int blue = 255;

        switch ((int) Math.floor(Math.random() * (3) + 1)) {
            case 1:
                red = (int) Math.floor(Math.random() * (223 - 163) + 163);
                if (rnd.nextInt(2) == 1) {
                    green = 163;
                    blue = 223;
                } else {
                    green = 223;
                    blue = 163;
                }
                break;
            case 2:
                green = (int) Math.floor(Math.random() * (223 - 163) + 163);
                if (rnd.nextInt(2) == 1) {
                    red = 163;
                    blue = 223;
                } else {
                    red = 223;
                    blue = 163;
                }
                break;
            case 3:
                blue = (int) Math.floor(Math.random() * (223 - 163) + 163);
                if (rnd.nextInt(2) == 1) {
                    green = 163;
                    red = 223;
                } else {
                    green = 223;
                    red = 163;
                }
                break;
        }


        int color = Color.argb(255, red, green, blue);
        imageView.setBackgroundColor(color);
    }

}

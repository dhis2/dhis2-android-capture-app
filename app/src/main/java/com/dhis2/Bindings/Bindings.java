package com.dhis2.Bindings;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

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

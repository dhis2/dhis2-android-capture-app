package com.dhis2.Bindings;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dhis2.usescases.programDetail.ProgramRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    static ProgramRepository programRepository;

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
    public static void setLayoutManager(RecyclerView recyclerView, boolean horizontal) {
        RecyclerView.LayoutManager recyclerLayout;
        if (!horizontal)
            recyclerLayout = new GridLayoutManager(recyclerView.getContext(), 2, LinearLayoutManager.VERTICAL, false);
        else
            recyclerLayout = new GridLayoutManager(recyclerView.getContext(), 4, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(recyclerLayout);

    }

    @BindingAdapter("randomColor")
    public static void setRandomColor(ImageView imageView, String textToColor) {

        String color = String.format("#%X", textToColor.hashCode());

        imageView.setBackgroundColor(Color.parseColor(color));
    }

    @BindingAdapter("progressColor")
    public static void setProgressColor(ProgressBar progressBar, int color) {
        progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @BindingAdapter("programStage")
    public static void getStageName(TextView textView, String stageId) {
        programRepository.programStage(stageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageModel -> textView.setText(programStageModel.displayName()),
                        Timber::d
                );
    }

    public static void setProgramRepository(ProgramRepository mprogramRepository) {
        programRepository = mprogramRepository;
    }

}

package org.dhis2.utils.custom_views;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by Pablo Pajuelo on 06/03/2017.
 */

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float to;
    private float initialFrom;
    private boolean rotate;
    private OnUpdate listener;

    public ProgressBarAnimation(ProgressBar progressBar, float from, float to, boolean rotate, OnUpdate listener) {
        super();
        this.progressBar = progressBar;
        this.initialFrom = from;
        this.from = 0;
        this.to = to - from;
        this.rotate = rotate;
        this.listener = listener;
    }

    public ProgressBarAnimation(ProgressBar progressBar, float initial, float from, float to, boolean rotate, OnUpdate listener) {
        super();
        this.progressBar = progressBar;
        this.initialFrom = initial;
        this.from = 0;
        this.to = to - from;
        this.rotate = rotate;
        this.listener = listener;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
//        float value = initialFrom + from + (to - from) * interpolatedTime;
        float value = initialFrom + (to - initialFrom)*interpolatedTime;
        progressBar.setProgress((int) value);

        if (rotate) {
            float rotationValue = (initialFrom) * interpolatedTime * 18f / 5f;
            progressBar.setRotation(rotationValue);
        }

        if ((int) initialFrom == 0)
            listener.onUpdate(false, value);
        else
            listener.onUpdate(true, value);
    }

    public interface OnUpdate {
        void onUpdate(boolean lost, float value);
    }
}

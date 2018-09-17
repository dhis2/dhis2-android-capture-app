package org.dhis2.utils.CustomViews;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 *
 * Created by Pablo Pajuelo on 06/03/2017.
 */

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float  to;
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

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);

        if(rotate) {
            float rotationValue = (initialFrom) * interpolatedTime * 18f / 5f;
            progressBar.setRotation(rotationValue);
        }

        if((int)initialFrom == 0)
            listener.onUpdate(false, interpolatedTime);
        else
            listener.onUpdate(true, interpolatedTime);
    }

    public interface OnUpdate{
        void onUpdate(boolean lost, float interpolatedTime);
    }
}

package org.dhis2.utils.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.dhis2.R;

public class CircularCompletionView extends View {

    private float completionPercent = 0;
    private float secondaryPercent = 0;
    private Paint paint = new Paint();
    private int radius = 100;
    private int strokeSize = 20;
    private int textSize = 10;
    private int diameter = radius * 2;
    private int completionColor;
    private int secondaryColor;
    private int percentageColor;
    private int circleColor;

    public CircularCompletionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialDefaultValues(attrs);
    }

    public CircularCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialDefaultValues(attrs);
    }

    public CircularCompletionView(Context context) {
        super(context);
        initialDefaultValues(null);
    }

    private void initialDefaultValues(AttributeSet attrs) {

        //Default Colors
        completionColor = ContextCompat.getColor(getContext(), R.color.completionColor);
        secondaryColor = ContextCompat.getColor(getContext(), R.color.secondaryColor);
        percentageColor = ContextCompat.getColor(getContext(), R.color.percentageColor);
        circleColor = ContextCompat.getColor(getContext(), R.color.circleColor);

        if (attrs == null) {
            return;
        }

        // Get values from xml attributes
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CircularCompletionView, 0, 0);
        try {
            // Dimensions
            textSize = (int) a.getDimension(R.styleable.CircularCompletionView_percentageSize, textSize);
            strokeSize = (int) a.getDimension(R.styleable.CircularCompletionView_strokeSize, strokeSize);

            // Colors
            completionColor = a.getColor(R.styleable.CircularCompletionView_completionColor, completionColor);
            secondaryColor = a.getColor(R.styleable.CircularCompletionView_secondaryColor, secondaryColor);
            percentageColor = a.getColor(R.styleable.CircularCompletionView_percentageColor, percentageColor);
            circleColor = a.getColor(R.styleable.CircularCompletionView_circleColor, circleColor);

            //Initial values
            completionPercent = a.getFloat(R.styleable.CircularCompletionView_initialPercentage,0f);
            secondaryPercent = a.getFloat(R.styleable.CircularCompletionView_initialSecondaryPercentage,0f);

        } finally {
            a.recycle();
        }
    }

    public void setCompletionPercentage(float completion){
        completionPercent = completion;//size should change for different Resolution screens
        invalidate();
    }

    public void setSecondaryPercentage(float secondary){
        secondaryPercent = secondary;
        invalidate();
    }

    public void setTextSize(int size){
        textSize = size;
        invalidate();
    }

    public void setStrokeSize(int size){
        strokeSize = size;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        if(width > height){
            width = height;
        }
        else{
            height = width;
        }

        diameter = width;
        radius = diameter/2;

        int newWidthMeasureSpec=MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int newHeightMeasureSpec=MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(circleColor);  // circle stroke
        paint.setStrokeWidth(strokeSize);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(radius, radius, radius-10, paint);

        paint.setColor(completionColor);  // circle stroke color(indicating completion Percentage)
        paint.setStrokeWidth(strokeSize);
        paint.setStyle(Paint.Style.FILL);

        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);
        oval.set(10,10,(diameter)-10,(diameter)-10);

        canvas.drawArc(oval, 270, completionPercent * 360, false, paint);


        paint.setColor(secondaryColor); //circle for secondary completion percentage
        canvas.drawArc(oval, 270 + completionPercent * 360, secondaryPercent * 360, false, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(percentageColor);  // text color

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);

        int x = (getWidth() / 2);
        int y = (int) ((getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText((int) ((completionPercent + secondaryPercent) * 100) + "%", x, y, paint);
    }

}

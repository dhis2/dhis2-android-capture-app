package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import java.util.Calendar;
import java.util.Date;

/**
 * QUADRAM. Created by ppajuelo on 29/01/2019.
 */
@SuppressWarnings({"squid:S1172", "squid:CommentedOutCodeLine"})
public abstract class FieldLayout extends RelativeLayout {

    protected boolean isBgTransparent;
    protected LayoutInflater inflater;

    public FieldLayout(Context context) {
        super(context);
    }

    public FieldLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context) {
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        inflater = LayoutInflater.from(context);
    }

    public abstract void performOnFocusAction();

   /* @Override TODO: DISABLED FOR 1.1.0
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            setBackgroundColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_LIGHT));
            performOnFocusAction();
        } else if (isBgTransparent) {
            setBackgroundColor(0x00000000);
        } else
            setBackgroundColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
    }*/

    protected DatePickerDialog setUpDatePickerDialog(Date date, Calendar selectedCalendar, boolean allowFutureDates,
                                                     DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (
                onDateSetListener),
                year,
                month,
                day);

        if (!allowFutureDates) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        return dateDialog;
    }
}

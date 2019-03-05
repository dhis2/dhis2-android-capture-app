package org.dhis2.Bindings;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    private Bindings() {
        // hide public constructor
    }

    @BindingAdapter("date")
    public static void setDate(TextView textView, String date) {
        SimpleDateFormat formatIn = DateUtils.databaseDateFormat();
        SimpleDateFormat formatOut = DateUtils.uiDateFormat();
        try {
            Date dateIn = formatIn.parse(date);
            String dateOut = formatOut.format(dateIn);
            textView.setText(dateOut);
        } catch (ParseException e) {
            Timber.e(e);
        }

    }

    @BindingAdapter("date")
    public static void parseDate(TextView textView, Date date) {
        if (date != null) {
            SimpleDateFormat formatOut = DateUtils.uiDateFormat();
            String dateOut = formatOut.format(date);
            textView.setText(dateOut);
        }

    }

    @BindingAdapter("drawableEnd")
    public static void setDrawableEnd(TextView textView, Drawable drawable) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) drawable).start();
        }
    }

    @BindingAdapter(value = {"initGrid", "spanCount"}, requireAll = false)
    public static void setLayoutManager(RecyclerView recyclerView, boolean horizontal, int spanCount) {
        RecyclerView.LayoutManager recyclerLayout;
        if (spanCount == -1)
            spanCount = 1;

        recyclerLayout = new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(recyclerLayout);

    }

    @BindingAdapter("spanSize")
    public static void setSpanSize(RecyclerView recyclerView, boolean setSpanSize) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int itemViewType = recyclerView.getAdapter().getItemViewType(position);
                    return (itemViewType == 4 || itemViewType == 8) ? 2 : 1;
                }
            });
        }
    }

    @BindingAdapter("progressColor")
    public static void setProgressColor(ProgressBar progressBar, int color) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = progressBar.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int newColor = a.getColor(0, 0);
        a.recycle();
        progressBar.getIndeterminateDrawable().setColorFilter(newColor, PorterDuff.Mode.SRC_IN);
    }

    @BindingAdapter("enrolmentIcon")
    public static void setEnrolmentIcon(ImageView view, EnrollmentStatus status) {
        Drawable lock;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_open_green);
                break;
            case COMPLETED:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_completed);
                break;
            case CANCELLED:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_inactive);
                break;
            default:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_read_only);
                break;
        }

        view.setImageDrawable(lock);

    }

    @BindingAdapter("enrolmentText")
    public static void setEnrolmentText(TextView view, EnrollmentStatus status) {
        String text;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                text = view.getContext().getString(R.string.event_open);
                break;
            case COMPLETED:
                text = view.getContext().getString(R.string.completed);
                break;
            case CANCELLED:
                text = view.getContext().getString(R.string.cancelled);
                break;
            default:
                text = view.getContext().getString(R.string.read_only);
                break;
        }

        view.setText(text);
    }

    @BindingAdapter(value = {"eventStatusIcon", "enrollmentStatusIcon", "eventProgramStage", "eventProgram"}, requireAll = false)
    public static void setEventIcon(ImageView view, EventModel event, EnrollmentModel enrollmentModel, ProgramStage eventProgramStage, ProgramModel program) {
        EventStatus status = event.status();
        EnrollmentStatus enrollmentStatus = enrollmentModel.enrollmentStatus();

        if (status == null)
            status = EventStatus.ACTIVE;

        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE;

        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            switch (status) {
                case ACTIVE:
                    if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                        view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_eye_red));
                    } else {
                        view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    }
                    break;
                case OVERDUE:
                case COMPLETED:
                case SKIPPED:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
                    break;
                case SCHEDULE:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
                case VISITED:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
                default:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
            }
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
        } else { //EnrollmentStatus = CANCELLED
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
        }
    }

    @BindingAdapter(value = {"eventStatusText", "enrollmentStatus", "eventProgramStage", "eventProgram"})
    public static void setEventText(TextView view, EventModel event, EnrollmentModel enrollmentModel,
                                    ProgramStage eventProgramStage, ProgramModel program) {
        EventStatus status = event.status();
        EnrollmentStatus enrollmentStatus = enrollmentModel.enrollmentStatus();
        if (status == null)
            status = EventStatus.ACTIVE;
        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE;


        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            setEnrollmentActionActive(view, event, status, program, eventProgramStage);
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            view.setText(view.getContext().getString(R.string.program_completed));
        } else { //EnrollmentStatus = CANCELLED
            view.setText(view.getContext().getString(R.string.program_inactive));
        }
    }

    private static void setEnrollmentActionActive(TextView view, EventModel event, EventStatus status, ProgramModel program, ProgramStage eventProgramStage) {
        switch (status) {
            case ACTIVE:
                if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                    view.setText(view.getContext().getString(R.string.event_expired));
                } else {
                    view.setText(view.getContext().getString(R.string.event_open));
                }
                break;
            case COMPLETED:
                if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                    view.setText(view.getContext().getString(R.string.event_expired));
                } else {
                    view.setText(view.getContext().getString(R.string.event_completed));
                }
                break;
            case SCHEDULE:
                if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                    view.setText(view.getContext().getString(R.string.event_expired));
                } else {
                    view.setText(view.getContext().getString(R.string.event_schedule));
                }
                break;
            case SKIPPED:
                view.setText(view.getContext().getString(R.string.event_skipped));
                break;
            case OVERDUE:
                view.setText(R.string.event_overdue);
                break;
            default:
                view.setText(view.getContext().getString(R.string.read_only));
                break;
        }
    }

    @BindingAdapter(value = {"eventColor", "eventProgramStage", "eventProgram"})
    public static void setEventColor(View view, EventModel event, ProgramStage programStage, ProgramModel program) {
        int bgColor;
        if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
            bgColor = R.drawable.item_event_dark_gray_ripple;
        } else {
            bgColor = getEventBgColor(event, programStage, program);
        }
        view.setBackground(ContextCompat.getDrawable(view.getContext(), bgColor));
    }

    private static int getEventBgColor(EventModel event, ProgramStage programStage, ProgramModel program) {
        int bgColor;
        switch (event.status()) {
            case ACTIVE:
                if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType())) {
                    bgColor = R.drawable.item_event_dark_gray_ripple;
                } else
                    bgColor = R.drawable.item_event_yellow_ripple;
                break;
            case COMPLETED:
                if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                    bgColor = R.drawable.item_event_dark_gray_ripple;
                } else
                    bgColor = R.drawable.item_event_gray_ripple;
                break;
            case SCHEDULE:
                if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType())) {
                    bgColor = R.drawable.item_event_dark_gray_ripple;
                } else
                    bgColor = R.drawable.item_event_green_ripple;
                break;
            case VISITED:
            case SKIPPED:
            default:
                bgColor = R.drawable.item_event_red_ripple;
                break;
        }
        return bgColor;
    }

    @BindingAdapter("eventWithoutRegistrationStatusText")
    public static void setEventWithoutRegistrationStatusText(TextView textView, ProgramEventViewModel event) {
        switch (event.eventStatus()) {
            case ACTIVE:
                if (event.isExpired()) {
                    textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                    textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                } else {
                    textView.setText(textView.getContext().getString(R.string.event_open));
                    textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.yellow_fdd));
                }
                break;
            case COMPLETED:
                if (event.isExpired()) {
                    textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                    textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                } else {
                    textView.setText(textView.getContext().getString(R.string.event_completed));
                    textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.gray_b2b));
                }
                break;
            case SKIPPED:
                textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                break;
            default:
                textView.setText(textView.getContext().getString(R.string.read_only));
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.green_7ed));
                break;
        }
    }

    @BindingAdapter("eventWithoutRegistrationStatusIcon")
    public static void setEventWithoutRegistrationStatusIcon(ImageView imageView, ProgramEventViewModel event) {
        switch (event.eventStatus()) {
            case ACTIVE:
                if (event.isExpired()) {
                    imageView.setImageResource(R.drawable.ic_eye_red);
                } else
                    imageView.setImageResource(R.drawable.ic_edit_yellow);
                break;
            case COMPLETED:
                if (event.isExpired()) {
                    imageView.setImageResource(R.drawable.ic_eye_red);
                } else
                    imageView.setImageResource(R.drawable.ic_eye_grey);
                break;
            default:
                // TODO CRIS: HERE CHECK THE EVENT APPROVAL
                imageView.setImageResource(R.drawable.ic_eye_green);
                break;

        }
    }

    @BindingAdapter("stateText")
    public static void setStateText(TextView textView, State state) {
        switch (state) {
            case TO_POST:
                textView.setText(textView.getContext().getString(R.string.state_to_post));
                break;
            case TO_UPDATE:
                textView.setText(textView.getContext().getString(R.string.state_to_update));
                break;
            case TO_DELETE:
                textView.setText(textView.getContext().getString(R.string.state_to_delete));
                break;
            case ERROR:
                textView.setText(textView.getContext().getString(R.string.state_error));
                break;
            case SYNCED:
                textView.setText(textView.getContext().getString(R.string.state_synced));
                break;
            default:
                break;
        }
    }

    @BindingAdapter("stateIcon")
    public static void setStateIcon(ImageView imageView, State state) {
        if (state != null) {
            switch (state) {
                case TO_POST:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case TO_UPDATE:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case TO_DELETE:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case ERROR:
                    imageView.setImageResource(R.drawable.ic_sync_problem_red);
                    break;
                case SYNCED:
                    imageView.setImageResource(R.drawable.ic_sync);
                    break;
                default:
                    break;
            }
        }
    }

    @BindingAdapter("spinnerOptions")
    public static void setSpinnerOptions(Spinner spinner, List<CategoryOptionComboModel> options) {
        CatComboAdapter adapter = new CatComboAdapter(spinner.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                options,
                "",
                R.color.white_faf);
        spinner.setAdapter(adapter);
    }

    @BindingAdapter("fromResBgColor")
    public static void setFromResBgColor(View view, int color) {
        int tintedColor = ColorUtils.getContrastColor(color);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(tintedColor);
        }
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null)
                drawable.setColorFilter(tintedColor, PorterDuff.Mode.SRC_IN);
            ((ImageView) view).setImageDrawable(drawable);
        }
    }

    public static void setObjectStyle(View view, View itemView, ObjectStyleModel objectStyle) {
        if (objectStyle.icon() != null) {
            Resources resources = view.getContext().getResources();
            String iconName = objectStyle.icon().startsWith("ic_") ? objectStyle.icon() : "ic_" + objectStyle.icon();
            int icon = resources.getIdentifier(iconName, "drawable", view.getContext().getPackageName());
            if (view instanceof ImageView)
                ((ImageView) view).setImageResource(icon);
        }

        if (objectStyle.color() != null) {
            String color = objectStyle.color().startsWith("#") ? objectStyle.color() : "#" + objectStyle.color();
            int colorRes = Color.parseColor(color);
            itemView.setBackgroundColor(colorRes);
            setFromResBgColor(view, colorRes);
        }
    }

    @BindingAdapter("imageBackground")
    public static void setImageBackground(ImageView imageView, Drawable drawable) {

        TypedValue typedValue = new TypedValue();
        TypedArray a = imageView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
        int colorPrimaryDark = a.getColor(0, 0);

        int px = (int) (1 * Resources.getSystem().getDisplayMetrics().density);
        ((GradientDrawable) drawable.mutate()).setStroke(px, colorPrimaryDark);
        imageView.setBackground(drawable);

    }
}
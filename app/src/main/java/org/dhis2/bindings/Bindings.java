package org.dhis2.bindings;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Date;


public class Bindings {

    @BindingAdapter("drawableEnd")
    public static void setDrawableEnd(TextView textView, Drawable drawable) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

        if (drawable instanceof AnimatedVectorDrawable)
            ((AnimatedVectorDrawable) drawable).start();
    }

    @BindingAdapter(value = {"initGrid", "spanCount"}, requireAll = false)
    public static void setLayoutManager(RecyclerView recyclerView, boolean horizontal, int spanCount) {
        RecyclerView.LayoutManager recyclerLayout;
        if (spanCount == -1)
            spanCount = 1;

        recyclerLayout = new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(recyclerLayout);

    }

    @BindingAdapter("enrolmentIcon")
    public static void setEnrolmentIcon(ImageView view, EnrollmentStatus status) {
        Drawable lock;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                lock = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_lock_open_green);
                break;
            case COMPLETED:
                lock = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_lock_completed);
                break;
            case CANCELLED:
                lock = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_lock_inactive);
                break;
            default:
                lock = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_lock_read_only);
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
    public static void setEventIcon(ImageView view, Event event, Enrollment enrollment, ProgramStage eventProgramStage, Program program) {
        if (event != null) {
            EventStatus status = event.status();
            EnrollmentStatus enrollmentStatus = EnrollmentStatus.ACTIVE;
            if (enrollment != null) {
                enrollmentStatus = enrollment.status();
            }
            if (status == null)
                status = EventStatus.ACTIVE;
            if (enrollmentStatus == null)
                enrollmentStatus = EnrollmentStatus.ACTIVE;

            int drawableResource;
            switch (status) {
                case ACTIVE:
                    Date eventDate = event.eventDate();
                    if (eventProgramStage.periodType() != null && eventProgramStage.periodType().name().contains(PeriodType.Weekly.name())) {
                        eventDate = DateUtils.getInstance().getNextPeriod(eventProgramStage.periodType(), eventDate, 0, true);
                    }
                    boolean isExpired = DateUtils.getInstance().isEventExpired(eventDate, null, event.status(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType(), program.expiryDays());
                    drawableResource = (enrollmentStatus == EnrollmentStatus.ACTIVE && !isExpired) ? R.drawable.ic_event_status_open : R.drawable.ic_event_status_open_read;
                    break;
                case OVERDUE:
                    drawableResource = enrollmentStatus == EnrollmentStatus.ACTIVE ? R.drawable.ic_event_status_overdue : R.drawable.ic_event_status_overdue_read;
                    break;
                case COMPLETED:
                    drawableResource = enrollmentStatus == EnrollmentStatus.ACTIVE ? R.drawable.ic_event_status_complete : R.drawable.ic_event_status_complete_read;
                    break;
                case SKIPPED:
                    drawableResource = enrollmentStatus == EnrollmentStatus.ACTIVE ? R.drawable.ic_event_status_skipped : R.drawable.ic_event_status_skipped_read;
                    break;
                case SCHEDULE:
                    drawableResource = enrollmentStatus == EnrollmentStatus.ACTIVE ? R.drawable.ic_event_status_schedule : R.drawable.ic_event_status_schedule_read;
                    break;
                default:
                    drawableResource = R.drawable.ic_event_status_open_read;
                    break;
            }

            view.setImageDrawable(
                    AppCompatResources.getDrawable(
                            view.getContext(),
                            drawableResource
                    )
            );
            view.setTag(drawableResource);
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

    @BindingAdapter("versionVisibility")
    public static void setVisibility(LinearLayout linearLayout, boolean check) {
        if (check && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            linearLayout.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("settingIcon")
    public static void setSettingIcon(ImageView view, int drawableReference) {
        Drawable drawable = AppCompatResources.getDrawable(view.getContext(), drawableReference);
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("iconTint")
    public static void setIconTint(ImageView view, boolean followUp) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getDrawable());
        Drawable mutableDrawable = wrappedDrawable.mutate();
        if (followUp)
            DrawableCompat.setTint(mutableDrawable, ContextCompat.getColor(view.getContext(), R.color.white));
        else
            DrawableCompat.setTint(mutableDrawable, ContextCompat.getColor(view.getContext(), R.color.text_black_333));

    }

    @BindingAdapter("networkVisibility")
    public static void setNetworkVisibility(View view, boolean checkNetwork) {
        if (checkNetwork) {
            view.setVisibility(NetworkUtils.isOnline(view.getContext()) ? View.VISIBLE : View.GONE);
        }
    }

    @BindingAdapter(value = {"dataSetStatus"})
    public static void setDataSetStatusIcon(ImageView view, Boolean isComplete) {
        int drawableResource = isComplete ? R.drawable.ic_event_status_complete : R.drawable.ic_event_status_open;
        view.setImageDrawable(
                AppCompatResources.getDrawable(
                        view.getContext(),
                        drawableResource
                )
        );
        view.setTag(drawableResource);
    }

    @BindingAdapter("clipCorners")
    public static void setClipCorners(View view, int cornerRadiusInDp) {
        ViewExtensionsKt.clipWithRoundedCorners(view, ExtensionsKt.getDp(cornerRadiusInDp));
    }

    @BindingAdapter("viewVisibility")
    public static void setViewVisibility(View view, boolean isVisible) {
        if (isVisible) {
            ViewAnimationsKt.show(view);
        } else {
            ViewAnimationsKt.hide(view);
        }
    }
}
package com.dhis2.Bindings;

import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.utils.CatComboAdapter;
import com.dhis2.utils.DateUtils;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    private static ProgramRepository programRepository;
    private static MetadataRepository metadataRepository;

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

    @BindingAdapter("date")
    public static void parseDate(TextView textView, Date date) {
        if (date != null) {
            SimpleDateFormat formatOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateOut = formatOut.format(date);
            textView.setText(dateOut);
        }

    }

    @BindingAdapter("lastEventDate")
    public static void setLastEventDate(TextView textView, Observable<List<EventModel>> listObservable) {
        listObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> textView.setText(DateUtils.getInstance().formatDate(data.get(0).lastUpdated())),
                        Timber::d);
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

    @BindingAdapter("randomColor")
    public static void setRandomColor(ImageView imageView, String textToColor) {
        String color;
        if (textToColor != null)
            color = String.format("#%X", String.valueOf(textToColor).hashCode());
        else
            color = "#FFFFFF";

        imageView.setBackgroundColor(Color.parseColor(color));
    }


    @BindingAdapter("tintRandomColor")
    public static void setTintRandomColor(ImageView imageView, String textToColor) {
        String color;
        if (textToColor != null)
            color = String.format("#%X", textToColor.hashCode());
        else
            color = "#FFFFFF";

        Drawable drawable = ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_program).mutate();
        drawable.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        imageView.setImageDrawable(drawable);
    }

    @BindingAdapter("programTypeIcon")
    public static void setProgramIcon(ImageView view, ProgramType programType) {
        if (programType.equals(ProgramType.WITH_REGISTRATION))
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_with_registration));
        else
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_without_reg));

    }


    @BindingAdapter("progressColor")
    public static void setProgressColor(ProgressBar progressBar, int color) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = progressBar.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        color = a.getColor(0, 0);
        a.recycle();
        progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @BindingAdapter("programStage")
    public static void getStageName(TextView textView, String stageId) {
        metadataRepository.programStage(stageId)
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

    @BindingAdapter("srcBackGround")
    public static void setBackGroundCompat(View view, int drawableId) {
        view.setBackground(ContextCompat.getDrawable(view.getContext(), drawableId));
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

    @BindingAdapter("enrolmentAction")
    public static void setEnrolmentAction(TextView textView, EnrollmentStatus status) {
        String action;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                action = "Complete";
                break;
            case COMPLETED:
                action = "re-open";
                break;
            case CANCELLED:
                action = "activate";
                break;
            default:
                action = "";
                break;
        }
        textView.setText(action);
    }

    @BindingAdapter("enrolmentText")
    public static void setEnrolmentText(TextView view, EnrollmentStatus status) {
        String text;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                text = "Open";
                break;
            case COMPLETED:
                text = "Completed";
                break;
            case CANCELLED:
                text = "Cancelled";
                break;
            default:
                text = "Read only";
                break;
        }

        view.setText(text);
    }

    @BindingAdapter("eventIcon")
    public static void setEventIcon(ImageView view, EventStatus status) {
        Drawable lock;
        if (status == null)
            status = EventStatus.ACTIVE;
        switch (status) {
            default:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_open_green);
                break;
            case COMPLETED:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_completed);
                break;
        }

        view.setImageDrawable(lock);

    }

    @BindingAdapter("eventText")//TODO: IT NEEDS ENROLLMENTSTATUS
    public static void setEventText(TextView view, EventStatus status) {
        String text;
        if (status == null)
            status = EventStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                text = "Open";
                break;
            case COMPLETED:
                text = "Program completed";
                break;
            case SCHEDULE:
                text = "Program inactive";
                break;
            default:
                text = "Read only";
                break;
        }

        view.setText(text);
    }

    @BindingAdapter("eventColor")
    public static void setEventText(CardView view, EventStatus status) {
        int eventColor;
        if (status == null)
            status = EventStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                eventColor = R.color.event_yellow;
                break;
            case COMPLETED:
                eventColor = R.color.event_gray;
                break;
            case SCHEDULE:
                eventColor = R.color.event_green;
                break;
            default:
                eventColor = R.color.event_red;
                break;
        }

        view.setCardBackgroundColor(ContextCompat.getColor(view.getContext(), eventColor));
    }

    @BindingAdapter("scheduleColor")
    public static void setScheduleColor(ImageView view, EventStatus status) {
        int drawable;
        if (status == null)
            status = EventStatus.ACTIVE;
        switch (status) {
            case SCHEDULE:
                drawable = R.drawable.schedule_circle_green;
                break;
            default:
                drawable = R.drawable.schedule_circle_red;
                break;
        }

        view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), drawable));
    }

    @BindingAdapter("programName")
    public static void setProgramName(TextView textView, String programUid) {
        metadataRepository.getProgramWithId(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programModel -> textView.setText(programModel.displayShortName()),
                        Timber::d
                );
    }


    public static void setMetadataRepository(MetadataRepository metadata) {
        metadataRepository = metadata;
    }

    @BindingAdapter("dataElementHint")
    public static void setDataElementName(TextInputLayout view, String dataElementUid) {
        metadataRepository.getDataElement(dataElementUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        dataModel -> view.setHint(dataModel.displayShortName()),
                        Timber::d
                );
    }

    @BindingAdapter("spinnerOptions")
    public static void setSpinnerOptions(Spinner spinner, ArrayList<CategoryOptionComboModel> options) {
        CatComboAdapter adapter = new CatComboAdapter(spinner.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                options,
                "");
        spinner.setAdapter(adapter);

    }
}

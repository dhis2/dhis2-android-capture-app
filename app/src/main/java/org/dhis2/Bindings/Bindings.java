package org.dhis2.Bindings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dhis2.R;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.form.model.LegendValue;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.databinding.DataElementLegendBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.commons.filters.CatOptionComboFilter;
import org.dhis2.commons.resources.ResourceManager;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.dhis2.Bindings.ViewExtensionsKt.openKeyboard;


public class Bindings {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof AnimatedVectorDrawable)
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

    public static String enrollmentText(Context context, EnrollmentStatus status) {
        String text;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                text = context.getString(R.string.event_open);
                break;
            case COMPLETED:
                text = context.getString(R.string.completed);
                break;
            case CANCELLED:
                text = context.getString(R.string.cancelled);
                break;
            default:
                text = context.getString(R.string.read_only);
                break;
        }
        return text;
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

    @BindingAdapter(value = {"eventStatusText", "enrollmentStatus", "eventProgramStage", "eventProgram"})
    public static void setEventText(TextView view, Event event, Enrollment enrollment, ProgramStage eventProgramStage, Program program) {
        if (event != null) {
            EventStatus status = event.status();
            EnrollmentStatus enrollmentStatus = enrollment.status();
            if (status == null)
                status = EventStatus.ACTIVE;
            if (enrollmentStatus == null)
                enrollmentStatus = EnrollmentStatus.ACTIVE;


            if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
                switch (status) {
                    case ACTIVE:
                        Date eventDate = event.eventDate();
                        if (eventProgramStage.periodType() != null && eventProgramStage.periodType().name().contains(PeriodType.Weekly.name()))
                            eventDate = DateUtils.getInstance().getNextPeriod(eventProgramStage.periodType(), eventDate, 0, true);
                        if (DateUtils.getInstance().isEventExpired(eventDate, null, event.status(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType(), program.expiryDays())) {
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
                        if (DateUtils.getInstance().isEventExpired(
                                event.dueDate(),
                                null,
                                status,
                                program.completeEventsExpiryDays(),
                                eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType(),
                                program.expiryDays())) {
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
            } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
                view.setText(view.getContext().getString(R.string.program_completed));
            } else { //EnrollmentStatus = CANCELLED
                view.setText(view.getContext().getString(R.string.program_inactive));
            }
        }
    }

    @BindingAdapter(value = {"eventColor", "eventProgramStage", "eventProgram"})
    public static void setEventColor(View view, Event event, ProgramStage programStage, Program program) {
        if (event != null) {
            int bgColor;
            if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                bgColor = R.drawable.item_event_dark_gray_ripple;
            } else if (event.status() != null) {
                switch (event.status()) {
                    case ACTIVE:
                        Date eventDate = event.eventDate();
                        if (programStage.periodType() != null && programStage.periodType().name().contains(PeriodType.Weekly.name()))
                            eventDate = DateUtils.getInstance().getNextPeriod(programStage.periodType(), eventDate, 0, true);
                        if (DateUtils.getInstance().isEventExpired(eventDate, null, event.status(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType(), program.expiryDays())) {
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
                        if (DateUtils.getInstance().isEventExpired(
                                event.dueDate(),
                                null,
                                event.status(),
                                program.completeEventsExpiryDays(),
                                programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType(),
                                program.expiryDays())) {
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
            } else {
                bgColor = R.drawable.item_event_red_ripple;
            }
            view.setBackground(AppCompatResources.getDrawable(view.getContext(), bgColor));
        }
    }

    @BindingAdapter("eventWithoutRegistrationStatusText")
    public static void setEventWithoutRegistrationStatusText(TextView textView, ProgramEventViewModel event) {
        switch (event.eventStatus()) {
            case ACTIVE:
                if (event.isExpired()) {
                    textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                } else {
                    textView.setText(textView.getContext().getString(R.string.event_open));
                }
                break;
            case COMPLETED:
                if (event.isExpired()) {
                    textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                } else {
                    textView.setText(textView.getContext().getString(R.string.event_completed));
                }
                break;
            case SKIPPED:
                textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                break;
            default:
                textView.setText(textView.getContext().getString(R.string.read_only));
                break;
        }
    }

    @BindingAdapter("eventWithoutRegistrationStatusIcon")
    public static void setEventWithoutRegistrationStatusIcon(ImageView imageView, ProgramEventViewModel event) {
        int drawableResource;
        switch (event.eventStatus()) {
            case COMPLETED:
                drawableResource = event.canBeEdited() ? R.drawable.ic_event_status_complete : R.drawable.ic_event_status_complete_read;
                break;
            default:
                drawableResource = event.canBeEdited() ? R.drawable.ic_event_status_open : R.drawable.ic_event_status_open_read;
                break;
        }
        imageView.setImageResource(drawableResource);
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

    @BindingAdapter(value = {"stateIcon", "showSynced"}, requireAll = false)
    public static void setStateIcon(ImageView imageView, State state, boolean showSynced) {
        if (state != null) {
            switch (state) {
                case TO_POST:
                case TO_UPDATE:
                case UPLOADING:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(R.drawable.ic_sync_problem_grey);
                    break;
                case ERROR:
                    imageView.setImageResource(R.drawable.ic_sync_problem_red);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(R.drawable.ic_sync_problem_red);
                    break;
                case SYNCED:
                    imageView.setImageResource(R.drawable.ic_sync);
                    if (!showSynced) {
                        imageView.setVisibility(View.GONE);
                    }
                    imageView.setTag(R.drawable.ic_sync);
                    break;
                case WARNING:
                    imageView.setImageResource(R.drawable.ic_sync_warning);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(R.drawable.ic_sync_warning);
                    break;
                case SENT_VIA_SMS:
                case SYNCED_VIA_SMS:
                    imageView.setImageResource(R.drawable.ic_sync_sms);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(R.drawable.ic_sync_sms);
                default:
                    break;
            }
        }
    }

    @BindingAdapter("fromResBgColor")
    public static void setFromResBgColor(View view, int color) {
        String tintedColor;

        ArrayList<Double> rgb = new ArrayList<>();
        rgb.add(Color.red(color) / 255.0d);
        rgb.add(Color.green(color) / 255.0d);
        rgb.add(Color.blue(color) / 255.0d);

        Double r = null;
        Double g = null;
        Double b = null;
        for (Double c : rgb) {
            if (c <= 0.03928d)
                c = c / 12.92d;
            else
                c = Math.pow(((c + 0.055d) / 1.055d), 2.4d);

            if (r == null)
                r = c;
            else if (g == null)
                g = c;
            else
                b = c;
        }

        double L = 0.2126d * r + 0.7152d * g + 0.0722d * b;


        if (L > 0.179d)
            tintedColor = "#000000"; // bright colors - black font
        else
            tintedColor = "#FFFFFF"; // dark colors - white font

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.parseColor(tintedColor));
        }
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null)
                drawable.setColorFilter(Color.parseColor(tintedColor), PorterDuff.Mode.SRC_IN);
            ((ImageView) view).setImageDrawable(drawable);
        }
    }

    public static void setObjectStyle(View view, View itemView, ObjectStyle objectStyle) {
        Resources resources = view.getContext().getResources();

        if (objectStyle != null) {
            int icon = new ResourceManager(view.getContext())
                    .getObjectStyleDrawableResource(objectStyle.icon(), R.drawable.ic_default_outline);
            if (view instanceof ImageView)
                ((ImageView) view).setImageResource(icon);
        }

        if (objectStyle != null && objectStyle.color() != null) {
            String color = objectStyle.color().startsWith("#") ? objectStyle.color() : "#" + objectStyle.color();
            int colorRes;
            if (color.length() == 4)
                colorRes = ColorUtils.getPrimaryColor(view.getContext(), ColorUtils.ColorType.PRIMARY);
            else
                colorRes = Color.parseColor(color);

            itemView.setBackgroundColor(colorRes);
            setFromResBgColor(view, colorRes);
        } else if (objectStyle != null && objectStyle.color() == null) {
            int colorRes = ColorUtils.getPrimaryColor(view.getContext(), ColorUtils.ColorType.PRIMARY);
            itemView.setBackgroundColor(colorRes);
            setFromResBgColor(view, colorRes);
        }

        if (objectStyle == null) {
            Drawable drawable = resources.getDrawable(R.drawable.ic_default_outline);
            if (view instanceof ImageView)
                ((ImageView) view).setImageDrawable(drawable);
            int colorRes = ColorUtils.getPrimaryColor(view.getContext(), ColorUtils.ColorType.PRIMARY);
            itemView.setBackgroundColor(colorRes);
            setFromResBgColor(view, colorRes);
        }
    }

    @BindingAdapter("imageBackground")
    public static void setImageBackground(ImageView imageView, Drawable drawable) {

        TypedValue typedValue = new TypedValue();
        TypedArray a = imageView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
        TypedArray b = imageView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryLight});
        int colorPrimaryDark = a.getColor(0, 0);
        int colorPrimaryLight = b.getColor(0, 0);

        int px = (int) (1 * Resources.getSystem().getDisplayMetrics().density);
        ((GradientDrawable) drawable.mutate()).setStroke(px, colorPrimaryDark);
        //((GradientDrawable) drawable.mutate()).setColor(colorPrimaryLight);

        imageView.setBackground(drawable);

    }

    @BindingAdapter("searchOrAdd")
    public static void setFabIcoin(FloatingActionButton fab, boolean needSearch) {
        Drawable drawable;
        if (needSearch) {
            drawable = AppCompatResources.getDrawable(fab.getContext(), R.drawable.ic_search_add);
        } else {
            drawable = AppCompatResources.getDrawable(fab.getContext(), R.drawable.ic_add_accent);
        }
        TypedValue typedValue = new TypedValue();
        TypedArray a = fab.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int colorPrimary = a.getColor(0, 0);
        fab.setColorFilter(colorPrimary);
        fab.setImageDrawable(drawable);
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

    @BindingAdapter("tableScaleTextSize")
    public static void setTabeScaleTextSize(TextView textView, DataSetTableAdapter.TableScale tableScale) {
        switch (tableScale) {
            case LARGE:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                break;
            case SMALL:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                break;
            default:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                break;
        }
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

    @BindingAdapter(value = {"catComboAdapterData", "catComboAdapterTitle"})
    public static void setCatComboAdapter(AppCompatSpinner spinner, List<CategoryOptionCombo> catComboAdapterData, String catComboAdapterTitle) {
        CatComboAdapter spinnerAdapter = new CatComboAdapter(spinner.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                catComboAdapterData != null ? catComboAdapterData : new ArrayList<>(),
                catComboAdapterTitle,
                R.color.white_faf);

        spinner.setAdapter(spinnerAdapter);
    }

    @BindingAdapter("onCatComboSelected")
    public static void setOnCatComboSelected(AppCompatSpinner spinner, CatOptionComboFilter itemFilter) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    itemFilter.selectCatOptionCombo(position - 1);
                    spinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    @BindingAdapter("textStyle")
    public static void setTextStyle(TextView textView, int style) {
        switch (style) {
            case Typeface.BOLD:
                textView.setTypeface(null, Typeface.BOLD);
                break;
            default:
                textView.setTypeface(null, Typeface.NORMAL);
                break;

        }
    }

    @BindingAdapter("setTextColor")
    public static void setTextColorRadioButton(RadioButton radioButton, boolean isBgTransparent) {
        radioButton.setTextColor(getColorStateViewChecked(radioButton.getContext(), isBgTransparent));
    }

    @BindingAdapter("tintRadioButton")
    public static void tintRadioButton(RadioButton radioButton, boolean isBg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(getColorStateViewChecked(radioButton.getContext(), isBg));
            radioButton.invalidate();
        }
    }

    @BindingAdapter("setTextColor")
    public static void setTextColorCheckbox(MaterialCheckBox checkbox, boolean isBgTransparent) {
        checkbox.setTextColor(getColorStateViewChecked(checkbox.getContext(), isBgTransparent));
    }

    @BindingAdapter("tintCheckboxButton")
    public static void tintCheckbox(MaterialCheckBox radioButton, boolean isBg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(getColorStateViewChecked(radioButton.getContext(), isBg));
            radioButton.invalidate();
        }
    }

    @BindingAdapter("setTextColor")
    public static void setTextColor(TextView textView, boolean isBgTransparent) {
        textView.setTextColor(getColorStateViewChecked(textView.getContext(), isBgTransparent));
    }

    private static ColorStateList getColorStateViewChecked(Context context, boolean isBackground) {
        int colorStateChecked;
        int colorStateUnchecked;

        if (isBackground) {
            colorStateChecked = ColorUtils.getPrimaryColor(context,
                    ColorUtils.ColorType.PRIMARY);
            colorStateUnchecked = ContextCompat.getColor(context, R.color.textPrimary);
        } else {
            colorStateChecked = ColorUtils.getPrimaryColor(context,
                    ColorUtils.ColorType.ACCENT);
            colorStateUnchecked = colorStateChecked;
        }


        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        colorStateChecked,
                        colorStateUnchecked
                }
        );
    }

    @BindingAdapter("requestFocus")
    public static void requestFocus(EditText editText, boolean focused) {
        if (focused) {
            editText.requestFocus();
            editText.setCursorVisible(true);
            openKeyboard(editText);
        } else {
            editText.clearFocus();
            editText.setCursorVisible(false);
        }
    }


    @BindingAdapter("checkListener")
    public static void checkListener(RadioGroup radioGroup, RadioButtonViewModel viewModel) {
        radioGroup.setOnCheckedChangeListener(null);
        if (viewModel.isAffirmativeChecked()) {
            radioGroup.check(R.id.yes);
        } else if (viewModel.isNegativeChecked()) {
            radioGroup.check(R.id.no);
        } else {
            radioGroup.clearCheck();
        }
        radioGroup.setOnCheckedChangeListener((radioGroup1, checkedId) -> {
            if (checkedId == R.id.yes) {
                viewModel.onValueChanged(true);
            } else if (checkedId == R.id.no) {
                viewModel.onValueChanged(false);
            }
        });
    }

    @BindingAdapter("clipCorners")
    public static void setClipCorners(View view, int cornerRadiusInDp) {
        ViewExtensionsKt.clipWithRoundedCorners(view, ExtensionsKt.getDp(cornerRadiusInDp));
    }

    @BindingAdapter("clipAllCorners")
    public static void setAllClipCorners(View view, int cornerRadiusInDp) {
        ViewExtensionsKt.clipWithAllRoundedCorners(view, ExtensionsKt.getDp(cornerRadiusInDp));
    }

    @BindingAdapter("legendValue")
    public static void setLegend(TextView textView, LegendValue legendValue) {
        if (legendValue != null) {
            Drawable bg = textView.getBackground();
            DrawableCompat.setTint(bg, ColorUtils.withAlpha(legendValue.getColor(), 38));
            Drawable[] drawables = textView.getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable != null)
                    DrawableCompat.setTint(drawable, legendValue.getColor());
            }
        }
    }

    @BindingAdapter("fabVisibility")
    public static void setFabVisibility(FloatingActionButton fab, boolean isVisible) {
        if (isVisible) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @BindingAdapter("viewVisibility")
    public static void setViewVisibility(View view, boolean isVisible) {
        if (isVisible) {
            ViewAnimationsKt.show(view);
        } else {
            ViewAnimationsKt.hide(view);
        }
    }

    @BindingAdapter("legendBadge")
    public static void setLegendBadge(FrameLayout legendLayout, LegendValue legendValue) {
        legendLayout.setVisibility(
                legendValue != null ? View.VISIBLE : View.GONE
        );
        if (legendValue != null) {
            DataElementLegendBinding legendBinding = DataElementLegendBinding.inflate(LayoutInflater.from(legendLayout.getContext()));
            legendBinding.setLegend(legendValue);
            legendLayout.removeAllViews();
            legendLayout.addView(legendBinding.getRoot());
        }
    }
}
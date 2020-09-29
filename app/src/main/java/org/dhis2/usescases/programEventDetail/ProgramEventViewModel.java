package org.dhis2.usescases.programEventDetail;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.uicomponents.map.model.CarouselItemModel;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;

/**
 * QUADRAM. Created by ppajuelo on 31/01/2019.
 */
@AutoValue
public abstract class ProgramEventViewModel implements CarouselItemModel {

    @NonNull
    public abstract String orgUnitUid();

    @NonNull
    public abstract String orgUnitName();

    @NonNull
    public abstract Date date();

    @NonNull
    public abstract State eventState();

    @NonNull
    public abstract List<Pair<String, String>> eventDisplayData();

    @NonNull
    public abstract EventStatus eventStatus();

    @NonNull
    public abstract Boolean isExpired();

    @NonNull
    public abstract String attributeOptionComboName();

    @Nullable
    public abstract Geometry geometry();

    @NonNull
    public abstract Boolean canBeEdited();

    public boolean openedAttributeList = false;

    public void toggleAttributeList() {
        this.openedAttributeList = !this.openedAttributeList;
    }

    @NonNull
    public static ProgramEventViewModel create(@NonNull String uid, @NonNull String orgUnitUid, @NonNull String orgUnitName, @NonNull Date date,
                                               @NonNull State eventState, @NonNull List<Pair<String, String>> data, @NonNull EventStatus status,
                                               @NonNull Boolean isExpired, @NonNull String attributeOptionComboName, Geometry geometry, Boolean canBeEdited) {
        return new AutoValue_ProgramEventViewModel(uid, orgUnitUid, orgUnitName, date, eventState, data, status, isExpired, attributeOptionComboName, geometry, canBeEdited);
    }

    public SpannableStringBuilder getSpannableStringValues() {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (Pair<String, String> nameValuePair : eventDisplayData()) {
            if (!Objects.equals(nameValuePair.component2(), "-")) {
                SpannableString value = new SpannableString(nameValuePair.component2());
                int colorToUse = eventDisplayData().indexOf(nameValuePair) % 2 == 0 ? Color.parseColor("#8A333333") : Color.parseColor("#61333333");
                value.setSpan(new ForegroundColorSpan(colorToUse), 0, value.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                stringBuilder.append(value);
                if (eventDisplayData().indexOf(nameValuePair) != eventDisplayData().size() - 1) {
                    stringBuilder.append(" ");
                }
            }
        }
        return stringBuilder;
    }

}

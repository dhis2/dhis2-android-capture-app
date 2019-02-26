package org.dhis2.usescases.eventsWithoutRegistration;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class EventUtils {

    private EventUtils() {
        // hide public constructor
    }

    @NonNull
    public static Map<String, FieldViewModel> toMap(@NonNull List<FieldViewModel> fieldViewModels) {
        Map<String, FieldViewModel> map = new LinkedHashMap<>();
        for (FieldViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.uid(), fieldViewModel);
        }
        return map;
    }
}

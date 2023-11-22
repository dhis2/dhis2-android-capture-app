package org.dhis2.utils;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 25/09/2018.
 */

public class ValueUtils {

    private ValueUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static TrackedEntityAttributeValue transform(D2 d2, TrackedEntityAttributeValue attributeValue, ValueType valueType, String optionSetUid) {
        String transformedValue = transformValue(d2, attributeValue.value(), valueType, optionSetUid);

        if (!Objects.equals(transformedValue, attributeValue.value())) {
            return attributeValue.toBuilder()
                    .value(transformedValue)
                    .build();
        } else {
            return attributeValue;
        }
    }

    public static String transformValue(D2 d2, String value, ValueType valueType, String optionSetUid) {
        String teAttrValue = value;
        if (valueType.equals(ValueType.ORGANISATION_UNIT)) {
            if (!d2.organisationUnitModule().organisationUnits().byUid().eq(value).blockingIsEmpty()) {
                String orgUnitName = d2.organisationUnitModule().organisationUnits()
                        .byUid().eq(value)
                        .one().blockingGet().displayName();
                teAttrValue = orgUnitName;
            }
        } else if (optionSetUid != null) {
            String optionCode = value;
            if (optionCode != null) {
                Option option = d2.optionModule().options().byOptionSetUid().eq(optionSetUid).byCode().eq(optionCode).one().blockingGet();
                if (option != null && (Objects.equals(option.code(), optionCode) || Objects.equals(option.name(), optionCode))) {
                    teAttrValue = option.displayName();
                }
            }
        }
        return teAttrValue;
    }
}

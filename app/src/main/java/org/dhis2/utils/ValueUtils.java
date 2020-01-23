package org.dhis2.utils;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

/**
 * QUADRAM. Created by ppajuelo on 25/09/2018.
 */

public class ValueUtils {

    public static TrackedEntityAttributeValue transform(D2 d2, TrackedEntityAttributeValue attributeValue, ValueType valueType, String optionSetUid) {
        TrackedEntityAttributeValue teAttrValue = attributeValue;
        if (valueType.equals(ValueType.ORGANISATION_UNIT.name())) {
            if (!d2.organisationUnitModule().organisationUnits().byUid().eq(attributeValue.value()).blockingIsEmpty()) {
                String orgUnitName = d2.organisationUnitModule().organisationUnits()
                        .byUid().eq(attributeValue.value())
                        .one().blockingGet().displayName();
                teAttrValue = TrackedEntityAttributeValue.builder()
                        .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                        .lastUpdated(teAttrValue.lastUpdated())
                        .created(teAttrValue.created())
                        .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                        .value(orgUnitName)
                        .build();
            }
        } else if (optionSetUid != null) {
            String optionCode = attributeValue.value();
            if (!d2.optionModule().options().byOptionSetUid().eq(optionSetUid).byCode().eq(optionCode).blockingIsEmpty()) {
                Option option = d2.optionModule().options().byOptionSetUid().eq(optionSetUid).byCode().eq(optionCode).one().blockingGet();
                if (option.code().equals(optionCode) || option.name().equals(optionCode)) {
                    teAttrValue = TrackedEntityAttributeValue.builder()
                            .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                            .lastUpdated(teAttrValue.lastUpdated())
                            .created(teAttrValue.created())
                            .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                            .value(option.displayName())
                            .build();
                }
            }
        }
        return teAttrValue;
    }
}

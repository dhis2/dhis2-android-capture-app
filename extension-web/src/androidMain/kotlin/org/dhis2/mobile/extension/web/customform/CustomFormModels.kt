package org.dhis2.mobile.extension.web.customform

/** A data element of the data set, with what the form needs to render its inputs. */
data class CustomFormDataElement(
    val uid: String,
    val name: String,
    val valueType: String,
    val options: List<CustomFormOption>,
)

data class CustomFormOption(
    val code: String,
    val name: String,
)

/** What a custom form renders with: its HTML, and the instance's metadata, values and indicators. */
data class CustomFormData(
    val html: String,
    val dataElements: List<CustomFormDataElement>,
    val values: Map<CustomFormFieldId, String>,
    val indicators: Map<String, String>,
)

/** A field of a custom form: the input `{dataElement}-{categoryOptionCombo}-val` in its HTML. */
data class CustomFormFieldId(
    val dataElement: String,
    val categoryOptionCombo: String,
) {
    val key: String get() = "$dataElement-$categoryOptionCombo"
}

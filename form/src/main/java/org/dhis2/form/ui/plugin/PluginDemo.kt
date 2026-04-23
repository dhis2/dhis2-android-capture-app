package org.dhis2.form.ui.plugin

object PluginDemo {
    // Dev switch. Flip to false (or delete the whole object) once the form wires real
    // fields to plugins via dataStore config in Phase D.
    const val ENABLED: Boolean = true
    const val PLUGIN_ID: String = "simple-capture-plugin"
    const val PLUGIN_VERSION: String = "1.0.0"

    fun mockProps(): PluginProps = PluginProps(
        values = mapOf(
            "field1" to "Test Value 1",
            "field2" to 42,
            "field3" to null,
        ),
        errors = mapOf(
            "field2" to listOf("Value must be less than 40"),
        ),
        warnings = mapOf(
            "field1" to listOf("Consider updating this value"),
        ),
        formSubmitted = false,
        fieldsMetadata = mapOf(
            "field1" to FieldMetadata(
                id = "field1",
                name = "First Name",
                shortName = "FName",
                formName = "First Name",
                disabled = false,
                compulsory = true,
                description = "Enter your first name",
                type = "TEXT",
                optionSet = null,
                displayInForms = true,
                displayInReports = true,
                icon = null,
                unique = null,
                searchable = true,
                url = null,
            ),
            "field2" to FieldMetadata(
                id = "field2",
                name = "Age",
                shortName = "Age",
                formName = "Age",
                disabled = false,
                compulsory = false,
                description = "Enter your age",
                type = "INTEGER",
                optionSet = null,
                displayInForms = true,
                displayInReports = true,
                icon = null,
                unique = null,
                searchable = false,
                url = null,
            ),
            "field3" to FieldMetadata(
                id = "field3",
                name = "Email",
                shortName = "Email",
                formName = "Email Address",
                disabled = false,
                compulsory = false,
                description = "Enter your email address",
                type = "EMAIL",
                optionSet = null,
                displayInForms = true,
                displayInReports = false,
                icon = null,
                unique = true,
                searchable = true,
                url = null,
            ),
        ),
    )
}

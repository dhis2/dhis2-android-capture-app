package org.dhis2.usescases.troubleshooting.ui

import android.graphics.Color
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.R
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.development.ProgramRuleValidation
import org.dhis2.usescases.development.RuleValidation
import org.hisp.dhis.rules.models.Rule
import java.util.Locale

@Preview
@Composable
fun ConfItemPreview() {
    ConfigurationItem(
        R.drawable.ic_settings_language,
        "Languages",
        "Tap here to change the language of the application",
    )
}

@ExperimentalAnimationApi
@Preview
@Composable
fun LanguageSelectorPreview() {
    LanguageSelector(currentLocale = Locale.ENGLISH, languages = emptyList(), visible = true) {}
}

@Preview(showBackground = true)
@Composable
fun LoadingValidations() {
    LoadingContent(loadingDescription = "Validating program rules...")
}

@Preview(showBackground = true)
@Composable
fun ValidationPassMessagePreview() {
    ValidationPassMessage()
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun ProgramRuleValidations() {
    ProgramRuleConfigurationItem(
        programRuleValidation = ProgramRuleValidation(
            programUid = "programUid",
            programName = "Antenatal care visiting",
            metadataIconData = MetadataIconData.Resource(
                programColor = Color.parseColor("#4CAF50"),
                iconResource = R.drawable.ic_home_outline,
                sizeInDp = 24,

            ),
            validations = listOf(
                RuleValidation(
                    rule = Rule(
                        "#{Hello} == hello",
                        emptyList(),
                        "uid1",
                        "Rule 1",
                        null,
                    ),
                    conditionError = "Condition error 1",
                    actionsError = listOf("Action error 11", "Action error 12"),
                ),
            ),
        ),
        showValidationList = true,
    ) {}
}

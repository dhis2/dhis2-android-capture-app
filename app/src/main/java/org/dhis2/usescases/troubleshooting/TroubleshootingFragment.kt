package org.dhis2.usescases.troubleshooting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.usescases.development.RuleValidation
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.models.Rule
import java.util.Locale

class TroubleshootingFragment : FragmentGlobalAbstract() {

    private val troubleshootingViewModel: TroubleshootingViewModel by viewModels {
        TroubleshootingViewModelFactory(
            LocaleSelector(requireContext(), app().serverComponent.getD2()),
            TroubleshootingRepository(app().serverComponent.getD2())
        )
    }

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                MdcTheme {
                    TroubleshootingScreen(
                        troubleshootingViewModel
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun TroubleshootingScreen(
    troubleshootingViewModel: TroubleshootingViewModel
) {
    var languageSelectorVisible by remember {
        mutableStateOf(false)
    }
    var rulesValidationVisible by remember {
        mutableStateOf(false)
    }
    val currentLocale by troubleshootingViewModel.currentLocale.observeAsState(Locale.getDefault())
    val ruleValidations by troubleshootingViewModel.ruleValidations.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(top = 8.dp)
    ) {
        ConfigurationItem(
            itemIcon = R.drawable.ic_setting_sms,
            itemTitle = stringResource(R.string.troubleshooting_language_title),
            itemDescription = stringResource(R.string.troubleshooting_language_description),
            onClick = {
                languageSelectorVisible = !languageSelectorVisible
            }
        )
        Divider(startIndent = 72.dp)
        LanguageSelector(
            currentLocale = currentLocale,
            languages = troubleshootingViewModel.supportedLocales,
            visible = languageSelectorVisible
        ) { newLocale ->
            troubleshootingViewModel.updateLocale(newLocale)
        }
        ConfigurationItem(
            itemIcon = R.drawable.ic_setting_sms,
            itemTitle = stringResource(R.string.troubleshooting_rules_title),
            itemDescription = stringResource(R.string.troubleshooting_rules_description),
            onClick = {
                rulesValidationVisible = !rulesValidationVisible
                troubleshootingViewModel.fetchRuleValidations()
            }
        )
        Divider(startIndent = 72.dp)
        ProgramRuleConfigurationItemList(
            visible = rulesValidationVisible,
            configurationErrors = ruleValidations
        )
    }
}

@Composable
fun ConfigurationItem(
    @DrawableRes itemIcon: Int,
    itemTitle: String,
    itemDescription: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .background(color = Color.White)
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(id = itemIcon),
            contentDescription = ""
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = itemTitle,
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_black_333).copy(alpha = 0.87f),
                style = TextStyle.Default.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular))
                )
            )
            Text(
                text = itemDescription,
                fontSize = 12.sp,
                color = colorResource(id = R.color.text_black_333).copy(alpha = 0.87f),
                style = TextStyle.Default.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_light))
                )
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun LanguageSelector(
    currentLocale: Locale,
    languages: List<Locale>,
    visible: Boolean,
    onLanguageChanged: (Locale) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(modifier = Modifier
            .clickable {
                expanded = !expanded
            }
            .fillMaxWidth()
            .height(84.dp)
            .background(color = colorResource(id = R.color.form_field_background))
            .clip(shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                value = currentLocale.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                expanded = !expanded
                            },
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = ""
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                textStyle = TextStyle.Default.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular))
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                languages.forEach { locale ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onLanguageChanged(locale)
                    }) {
                        Text(text = locale.displayName)
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ProgramRuleConfigurationItemList(
    modifier: Modifier = Modifier,
    configurationErrors: List<RuleValidation> = emptyList(),
    visible: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.form_field_background))
            .clip(shape = RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            AnimatedVisibility(
                visible = visible && configurationErrors.isEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            AnimatedVisibility(
                visible = visible && configurationErrors.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                LazyColumn() {
                    configurationErrors.groupBy { it.program }
                        .forEach { (program, ruleValidations) ->
                            stickyHeader {
                                Text(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                    text = program.displayName()!!
                                )
                            }
                            items(ruleValidations) { ruleValidation ->
                                Spacer(modifier = Modifier.size(32.dp))
                                ProgramRuleConfigurationTitle(ruleValidation)
                                ruleValidation.errors().forEach { errorMessage ->
                                    Spacer(modifier = Modifier.size(8.dp))
                                    ProgramRuleConfigurationMessage(errorMessage)
                                }
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun ProgramRuleConfigurationTitle(ruleValidation: RuleValidation) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {

        }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_warning),
            contentDescription = "", tint = colorResource(id = R.color.warning_color)
        )
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            text = ruleValidation.title() ?: ruleValidation.uid(),
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.rubik_regular))
        )
    }
}

@Composable
fun ProgramRuleConfigurationMessage(errorMessage: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_circle_indicator),
            contentDescription = ""
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
                .fillMaxWidth(),
            text = errorMessage,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.54f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_light))
            )
        )
    }
}

@Preview
@Composable
fun ConfItemPreview() {
    ConfigurationItem(
        R.drawable.ic_setting_sms,
        "Languages",
        "Tap here to change the language of the application"
    )
}

@ExperimentalAnimationApi
@Preview
@Composable
fun languageSelectorPreview() {
    LanguageSelector(currentLocale = Locale.ENGLISH, languages = emptyList(), visible = true) {}
}


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Preview
@Composable
fun PRConfigurationPreview() {
    ProgramRuleConfigurationItemList(
        configurationErrors = listOf(
            RuleValidation(
                rule = Rule.create(null, null, "#{Hello} == hello", emptyList(), "Rule 1", "uid1"),
                program = Program.builder().displayName("Program1").uid("p1").build(),
                conditionError = "Condition error 1",
                actionsError = listOf("Action error 11", "Action error 12")
            ),
            RuleValidation(
                rule = Rule.create(null, null, "#{Hello} == hello", emptyList(), "Rule 2", "uid2"),
                program = Program.builder().displayName("Program1").uid("p1").build(),
                conditionError = "Condition error 2",
                actionsError = listOf("Action error 21", "Action error 22")
            ),
            RuleValidation(
                rule = Rule.create(null, null, "#{Hello} == hello", emptyList(), "Rule 3", "uid3"),
                program = Program.builder().displayName("Program1").uid("p1").build(),
                conditionError = "Condition error 3",
                actionsError = listOf("Action error 31", "Action error 32")
            )
        ),
        visible = true
    )
}
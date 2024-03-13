package org.dhis2.usescases.troubleshooting.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.development.ProgramRuleValidation
import org.dhis2.usescases.development.RuleValidation
import org.dhis2.usescases.troubleshooting.TroubleshootingViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import java.util.Locale

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun TroubleshootingScreen(
    troubleshootingViewModel: TroubleshootingViewModel,
    onLanguageChanged: () -> Unit,
) {
    var languageSelectorVisible by remember {
        mutableStateOf(troubleshootingViewModel.openLanguageSection)
    }
    var rulesValidationVisible by remember {
        mutableStateOf(false)
    }
    val currentLocale by troubleshootingViewModel.currentLocale.observeAsState(Locale.getDefault())
    val ruleValidations by troubleshootingViewModel.ruleValidations.observeAsState()
    val visibleProgram by troubleshootingViewModel.visibleProgram.observeAsState(null)
    val localesToShow by troubleshootingViewModel.localesToDisplay.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(top = 8.dp),
    ) {
        ConfigurationItem(
            itemIcon = R.drawable.ic_settings_language,
            itemTitle = stringResource(R.string.troubleshooting_language_title),
            itemDescription = stringResource(R.string.troubleshooting_language_description),
            onClick = {
                languageSelectorVisible = !languageSelectorVisible
            },
        )
        Divider(startIndent = 72.dp, thickness = if (languageSelectorVisible) 0.dp else 1.dp)
        LanguageSelector(
            currentLocale = currentLocale,
            languages = localesToShow,
            visible = languageSelectorVisible,
        ) { newLocale ->
            troubleshootingViewModel.updateLocale(newLocale)
            onLanguageChanged()
        }
        ConfigurationItem(
            itemIcon = R.drawable.ic_settings_rules,
            itemTitle = stringResource(R.string.troubleshooting_rules_title),
            itemDescription = stringResource(R.string.troubleshooting_rules_description),
            onClick = {
                rulesValidationVisible = !rulesValidationVisible
                troubleshootingViewModel.fetchRuleValidations()
            },
        )
        Divider(startIndent = 72.dp, thickness = if (rulesValidationVisible) 0.dp else 1.dp)
        ProgramRuleConfigurationItemList(
            visible = rulesValidationVisible,
            configurationErrors = ruleValidations,
            visibleProgram = visibleProgram,
        ) { selectedProgramUid ->
            troubleshootingViewModel.onProgramSelected(selectedProgramUid)
        }
    }
}

@Composable
fun ConfigurationItem(
    @DrawableRes itemIcon: Int,
    itemTitle: String,
    itemDescription: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .background(color = Color.White)
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),

    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(id = itemIcon),
            contentDescription = "",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
        ) {
            Text(
                text = itemTitle,
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_black_333).copy(alpha = 0.87f),
                style = TextStyle.Default.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular)),
                ),
            )
            Text(
                text = itemDescription,
                fontSize = 12.sp,
                color = colorResource(id = R.color.text_black_333).copy(alpha = 0.87f),
                style = TextStyle.Default.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_light)),
                ),
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
    onLanguageChanged: (Locale) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .background(
                    color = colorResource(id = R.color.form_field_background),
                    shape = RoundedCornerShape(6.dp),
                ),
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.inner_shadow_top),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
            )
            Image(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.inner_shadow_bottom),
                contentDescription = "",
                alignment = Alignment.BottomCenter,
                contentScale = ContentScale.FillWidth,
            )
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                    }
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .align(Alignment.CenterVertically),
                    text = currentLocale.displayName.replaceFirstChar { it.uppercase() },
                    maxLines = 1,
                    style = TextStyle.Default.copy(
                        fontFamily = FontFamily(Font(R.font.rubik_regular)),
                    ),
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "",
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                ) {
                    languages.forEach { locale ->
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onLanguageChanged(locale)
                            },
                        ) {
                            Text(text = locale.displayName.replaceFirstChar { it.uppercase() })
                        }
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
    configurationErrors: List<ProgramRuleValidation>?,
    visible: Boolean,
    visibleProgram: String?,
    onProgramClick: (programUid: String) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.form_field_background),
                shape = RoundedCornerShape(6.dp),
            ),
    ) {
        if (visible) {
            Image(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.inner_shadow_top),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
            )
            Image(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.inner_shadow_bottom),
                contentDescription = "",
                alignment = Alignment.BottomCenter,
                contentScale = ContentScale.FillWidth,
            )
        }
        Column {
            AnimatedVisibility(
                visible = visible && configurationErrors == null,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                LoadingContent(
                    loadingDescription = stringResource(
                        R.string.troubleshooting_ongoing_rule_validation,
                    ),
                )
            }
            AnimatedVisibility(
                visible = visible && configurationErrors?.isNotEmpty() == true,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                LazyColumn {
                    items(configurationErrors!!) { ruleValidation ->
                        ProgramRuleConfigurationItem(
                            programRuleValidation = ruleValidation,
                            showValidationList = ruleValidation.programUid == visibleProgram,
                        ) { selectedProgramUid ->
                            onProgramClick(selectedProgramUid)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = visible && configurationErrors?.isEmpty() == true,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                ValidationPassMessage()
            }
        }
    }
}

@Composable
fun LoadingContent(loadingDescription: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = loadingDescription,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.87f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun ValidationPassMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.padding(top = 64.dp, bottom = 64.dp),
            painter = painterResource(id = R.drawable.ic_validation_pass),
            contentDescription = "",
        )
        Text(
            text = stringResource(R.string.troobleshooting_pass_rule_validation_title),
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.87f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.troubleshooting_pass_rule_validation),
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.54f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun ProgramRuleConfigurationItem(
    programRuleValidation: ProgramRuleValidation,
    showValidationList: Boolean,
    onProgramClick: (programUid: String) -> Unit,
) {
    Column {
        ProgramRuleConfigurationProgram(
            programRuleValidation.programUid,
            programRuleValidation.programName,
            programRuleValidation.metadataIconData,
            showValidationList,
        ) { selectedProgramUid ->
            onProgramClick(selectedProgramUid)
        }
        AnimatedVisibility(
            visible = showValidationList,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                programRuleValidation.validations.forEach { ruleValidation ->
                    Spacer(modifier = Modifier.size(16.dp))
                    ProgramRuleConfigurationTitle(ruleValidation)
                    ruleValidation.errors().forEach { errorMessage ->
                        Spacer(modifier = Modifier.size(8.dp))
                        ProgramRuleConfigurationMessage(errorMessage)
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ProgramRuleConfigurationProgram(
    programUid: String,
    programName: String,
    metadataIconData: MetadataIconData,
    isSelected: Boolean,
    onProgramClick: (programUid: String) -> Unit,
) {
    val scaleStatus = animateFloatAsState(if (isSelected) -1f else 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp,
            )
            .clickable {
                onProgramClick(programUid)
            },
    ) {
        MetadataAvatar(
            icon = { MetadataIcon(imageCardData = metadataIconData.imageCardData) },
            iconTint = metadataIconData.color,
            size = AvatarSize.Normal,
        )

        Text(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(horizontal = 11.5.dp)
                .align(Alignment.CenterVertically),
            text = programName,
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
        Icon(
            modifier = Modifier.scale(scaleStatus.value),
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = "",
            tint = colorResource(id = R.color.textPrimary),
        )
    }
}

@Composable
fun ProgramRuleConfigurationTitle(ruleValidation: RuleValidation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
            },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_warning),
            contentDescription = "",
            tint = colorResource(id = R.color.warning_color),
        )
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            text = ruleValidation.title(),
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
    }
}

@Composable
fun ProgramRuleConfigurationMessage(errorMessage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),

    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_circle_indicator),
            contentDescription = "",
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
                fontFamily = FontFamily(Font(R.font.rubik_light)),
            ),
        )
    }
}

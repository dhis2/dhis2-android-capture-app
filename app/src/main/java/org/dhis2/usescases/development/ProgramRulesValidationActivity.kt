package org.dhis2.usescases.development

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import com.google.android.material.composethemeadapter.MdcTheme
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.data.forms.dataentry.FormView
import org.dhis2.databinding.ActivityProgramRulesValidationBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.program.Program

class ProgramRulesValidationActivity : ActivityGlobalAbstract() {

    private val binding: ActivityProgramRulesValidationBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_program_rules_validation)
    }

    @Inject
    lateinit var ruleValidationModelFactory: RulesValidationsModelFactory

    val rulesValidationsModel: RulesValidationsModel by viewModels { ruleValidationModelFactory }

    @Inject
    lateinit var formView: FormView

    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        app().serverComponent()?.plus(ProgramRulesValidationsModule(this))?.inject(this)
        super.onCreate(savedInstanceState)

        binding.progress.show()
        binding.apply {
            backButton.setOnClickListener { onBackPressed() }
            runRuleValidation.setOnClickListener { rulesValidationsModel.runCurrentValidation() }
            composeView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MdcTheme {
                        ValidationList(rulesValidationsModel)
                    }
                }
            }
            composeProgramsView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MdcTheme {
                        ProgramChipGroup(rulesValidationsModel)
                    }
                }
            }

            showAllButton.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MdcTheme {
                        ShowValidationButton(rulesValidationsModel)
                    }
                }
            }
        }
        rulesValidationsModel.ruleValidations.observe(
            this,
            {
                binding.progress.hide()
            }
        )

        rulesValidationsModel.programVariables.observe(
            this,
            {
                formView.processItems(it)
            }
        )

        rulesValidationsModel.expressionValidationResult.observe(
            this,
            {
                if (it) {
                    Toast.makeText(
                        this,
                        "Expression is correct",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Expression is wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.formViewContainer, formView).commit()
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun ValidationList(rulesValidationsModel: RulesValidationsModel) {
    val visible by rulesValidationsModel.allValidationsIsOpen.observeAsState(false)
    val ruleValidations by rulesValidationsModel.ruleValidations.observeAsState(emptyList())
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        LazyColumn(Modifier.background(color = Color.White)) {
            ruleValidations.groupBy { it.program }.forEach { (program, items) ->
                stickyHeader {
                    RuleValidationSectionRow(program)
                }

                items(items) { ruleValidation ->
                    RuleValidationRow(ruleValidation) { programErrorClicked ->
                        rulesValidationsModel.allValidationsIsOpen.value = !visible
                        rulesValidationsModel.setSelectedProgram(programErrorClicked)
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ShowValidationButton(rulesValidationsModel: RulesValidationsModel) {
    val visible by rulesValidationsModel.allValidationsIsOpen.observeAsState(false)
    Button(
        onClick = {
            rulesValidationsModel.allValidationsIsOpen.value = !visible
        }
    ) {
        Text(text = if (visible) "Hide all validations" else "Show all validations")
    }
}

@Composable
fun RuleValidationRow(
    ruleValidation: RuleValidation,
    onErrorClick: (String) -> Unit = {}
) {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            fontSize = 14.sp,
            text = ruleValidation.title()!!
        )
        Divider()
        ruleValidation.errors().forEach { error ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onErrorClick(ruleValidation.program.uid()) }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                fontSize = 12.sp,
                text = error
            )
        }
    }
}

@Composable
fun RuleValidationSectionRow(program: Program) {
    Column(
        Modifier
            .background(color = MaterialTheme.colors.primary)
            .fillMaxWidth()
            .padding(vertical = Dp(8f), horizontal = Dp(16f))
    ) {
        Text(program.displayName()!!, color = MaterialTheme.colors.onPrimary)
    }
}

@Composable
fun ProgramChipGroup(
    rulesValidationsModel: RulesValidationsModel
) {
    val programsWithRules by rulesValidationsModel.programWithRules.observeAsState(emptyList())
    val selectedProgram by rulesValidationsModel.selectedProgramUid.observeAsState(null)
    Column(
        modifier = Modifier
            .padding(Dp(8f))
            .wrapContentHeight()
    ) {
        LazyRow {
            items(programsWithRules) { program ->
                Chip(
                    name = program.displayName()!!,
                    isSelected = selectedProgram == program.uid(),
                    onSelectionChanged = {
                        rulesValidationsModel.setSelectedProgram(program.uid())
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Chip(
    name: String = "Chip",
    isSelected: Boolean = false,
    onSelectionChanged: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.padding(Dp(4f)),
        elevation = Dp(8f),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Color.White else MaterialTheme.colors.primary
    ) {
        Row(
            modifier = Modifier
                .toggleable(
                    value = isSelected,
                    onValueChange = {
                        onSelectionChanged(name)
                    }
                )
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body2,
                color = if (isSelected) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onPrimary
                },
                modifier = Modifier.padding(Dp(8f))
            )
        }
    }
}

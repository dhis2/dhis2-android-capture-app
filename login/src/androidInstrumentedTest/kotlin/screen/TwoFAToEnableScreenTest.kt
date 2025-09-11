package screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import org.dhis2.mobile.login.authentication.ui.screen.TURN_ON_BUTTON_TEST_TAG
import org.dhis2.mobile.login.authentication.ui.screen.TwoFAToEnableScreen
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.junit.Rule
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TwoFAToEnableScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val secretCode = "secret_code"

    @Test
    fun should_init_with_disabled_button() {
        with(composeTestRule) {
            setContent {
                DHIS2Theme {
                    TwoFAToEnableScreen(
                        enableUiState =
                            TwoFAUiState.Enable(
                                secretCode = secretCode,
                                isEnabling = false,
                                enableErrorMessage = null,
                            ),
                        onAuthenticatorButtonClicked = {},
                        onCopyCodeButtonClicked = {},
                        onEnableButtonClicked = {},
                    )
                }
            }

            onNodeWithTag(TURN_ON_BUTTON_TEST_TAG).assertIsNotEnabled()
        }
    }

    @Test
    fun should_set_error_if_code_is_wrong() =
        runComposeUiTest {
            with(composeTestRule) {
                setContent {
                    DHIS2Theme {
                        TwoFAToEnableScreen(
                            enableUiState =
                                TwoFAUiState.Enable(
                                    secretCode = secretCode,
                                    isEnabling = false,
                                    enableErrorMessage = "Error code",
                                ),
                            onAuthenticatorButtonClicked = {},
                            onCopyCodeButtonClicked = {},
                            onEnableButtonClicked = {},
                        )
                    }
                }

                onNode(
                    hasTestTag("INPUT_TEXT_SUPPORTING_TEXT") and
                        hasText("two_fa_failed_to_turn_on"),
                ).assertIsDisplayed()
            }
        }
}

package org.dhis2.form.model.section

import androidx.databinding.ObservableField
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.model.SectionUiModelImpl.Companion.CLOSING_SECTION_UID
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionUiModelTest {

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDescriptionIsNull() {
        val sectionUiModel = givenSectionUiModelWithNullDescription()
        assertTrue(sectionUiModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDescriptionIsEmpty() {
        val sectionUiModel = givenSectionUiModelWithEmptyDescription()
        assertTrue(sectionUiModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDHasDescription() {
        val sectionUiModel = givenSectionUiModelWithDescription()
        assertTrue(sectionUiModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldNotShowDescriptionIconWhenTitleIsNotEllipsizedAndDescriptionIsNull() {
        val sectionUiModel = givenSectionUiModelWithNullDescription()
        assertFalse(sectionUiModel.hasToShowDescriptionIcon(false))
    }

    @Test
    fun shouldNotShowDescriptionIconWhenTitleIsNotEllipsizedAndDescriptionIsEmpty() {
        val sectionUiModel = givenSectionUiModelWithEmptyDescription()
        assertFalse(sectionUiModel.hasToShowDescriptionIcon(false))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsNotEllipsizedAndHasDescription() {
        val sectionUiModel = givenSectionUiModelWithDescription()
        assertTrue(sectionUiModel.hasToShowDescriptionIcon(false))
    }

    @Test
    fun shouldShowNextButton() {
        val sectionUiModel = givenSectionUiModelWithDescription().also {
            it.setShowBottomShadow(true)
        }
        assertTrue(sectionUiModel.showNextButton())
    }

    @Test
    fun shouldNotShowNextButton() {
        val sectionUiModel = givenSectionUiModelWithDescription().also {
            it.setShowBottomShadow(false)
        }
        assertFalse(sectionUiModel.showNextButton())
    }

    @Test
    fun shouldNotShowNextButtonForClosingSection() {
        val sectionUiModel = givenClosingSectionUiModel().also {
            it.setShowBottomShadow(true)
        }
        assertFalse(sectionUiModel.showNextButton())
    }

    private fun givenClosingSectionUiModel() = SectionUiModelImpl(
        CLOSING_SECTION_UID,
        1,
        null,
        false,
        null,
        false,
        null,
        false,
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        1,
        1,
        0,
        0,
        "",
        ObservableField(),
    )

    private fun givenSectionUiModelWithNullDescription() = SectionUiModelImpl(
        "",
        1,
        null,
        false,
        null,
        false,
        null,
        false,
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        1,
        1,
        0,
        0,
        "",
        ObservableField(),
    )

    private fun givenSectionUiModelWithEmptyDescription() = SectionUiModelImpl(
        "",
        1,
        null,
        false,
        null,
        false,
        null,
        false,
        "",
        null,
        null,
        null,
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        1,
        1,
        0,
        0,
        "",
        ObservableField(),
    )

    private fun givenSectionUiModelWithDescription() = SectionUiModelImpl(
        "",
        1,
        null,
        false,
        null,
        false,
        null,
        false,
        "",
        null,
        null,
        null,
        "This is a description",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        1,
        1,
        0,
        0,
        "",
        ObservableField(),
    )
}

package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ParameterInputModelMapperTest {
    private val resourceManager: ResourceManager = mock()

    @Before
    fun setUp() {
        whenever(resourceManager.getString(R.string.yes)) doReturn "Yes"
        whenever(resourceManager.getString(R.string.no)) doReturn "No"
    }

    // Test getInputTypeForOptionSetByRenderingType
    @Test
    fun `should return RADIO_BUTTON for horizontal radio buttons rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.HORIZONTAL_RADIOBUTTONS)
        assertEquals(TrackerInputType.RADIO_BUTTON, result)
    }

    @Test
    fun `should return RADIO_BUTTON for vertical radio buttons rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.VERTICAL_RADIOBUTTONS)
        assertEquals(TrackerInputType.RADIO_BUTTON, result)
    }

    @Test
    fun `should return CHECKBOX for horizontal checkboxes rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.HORIZONTAL_CHECKBOXES)
        assertEquals(TrackerInputType.CHECKBOX, result)
    }

    @Test
    fun `should return CHECKBOX for vertical checkboxes rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.VERTICAL_CHECKBOXES)
        assertEquals(TrackerInputType.CHECKBOX, result)
    }

    @Test
    fun `should return MATRIX for matrix rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.MATRIX)
        assertEquals(TrackerInputType.MATRIX, result)
    }

    @Test
    fun `should return SEQUENTIAL for sequential rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.SEQUENCIAL)
        assertEquals(TrackerInputType.SEQUENTIAL, result)
    }

    @Test
    fun `should return DROPDOWN for null rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(null)
        assertEquals(TrackerInputType.DROPDOWN, result)
    }

    @Test
    fun `should return DROPDOWN for default rendering type`() {
        val result = getInputTypeForOptionSetByRenderingType(UiRenderType.DEFAULT)
        assertEquals(TrackerInputType.DROPDOWN, result)
    }

    // Test getInputTypeByValueType for TEXT
    @Test
    fun `should return TEXT for TEXT value type with default rendering`() {
        val result = getInputTypeByValueType(ValueType.TEXT, null)
        assertEquals(TrackerInputType.TEXT, result)
    }

    @Test
    fun `should return QR_CODE for TEXT value type with QR_CODE rendering`() {
        val result = getInputTypeByValueType(ValueType.TEXT, UiRenderType.QR_CODE)
        assertEquals(TrackerInputType.QR_CODE, result)
    }

    @Test
    fun `should return QR_CODE for TEXT value type with GS1_DATAMATRIX rendering`() {
        val result = getInputTypeByValueType(ValueType.TEXT, UiRenderType.GS1_DATAMATRIX)
        assertEquals(TrackerInputType.QR_CODE, result)
    }

    @Test
    fun `should return BAR_CODE for TEXT value type with BAR_CODE rendering`() {
        val result = getInputTypeByValueType(ValueType.TEXT, UiRenderType.BAR_CODE)
        assertEquals(TrackerInputType.BAR_CODE, result)
    }

    // Test getInputTypeByValueType for numeric types
    @Test
    fun `should return INTEGER_POSITIVE for INTEGER_POSITIVE value type`() {
        val result = getInputTypeByValueType(ValueType.INTEGER_POSITIVE, null)
        assertEquals(TrackerInputType.INTEGER_POSITIVE, result)
    }

    @Test
    fun `should return INTEGER_ZERO_OR_POSITIVE for INTEGER_ZERO_OR_POSITIVE value type`() {
        val result = getInputTypeByValueType(ValueType.INTEGER_ZERO_OR_POSITIVE, null)
        assertEquals(TrackerInputType.INTEGER_ZERO_OR_POSITIVE, result)
    }

    @Test
    fun `should return PERCENTAGE for PERCENTAGE value type`() {
        val result = getInputTypeByValueType(ValueType.PERCENTAGE, null)
        assertEquals(TrackerInputType.PERCENTAGE, result)
    }

    @Test
    fun `should return NUMBER for NUMBER value type`() {
        val result = getInputTypeByValueType(ValueType.NUMBER, null)
        assertEquals(TrackerInputType.NUMBER, result)
    }

    @Test
    fun `should return INTEGER_NEGATIVE for INTEGER_NEGATIVE value type`() {
        val result = getInputTypeByValueType(ValueType.INTEGER_NEGATIVE, null)
        assertEquals(TrackerInputType.INTEGER_NEGATIVE, result)
    }

    @Test
    fun `should return INTEGER for INTEGER value type`() {
        val result = getInputTypeByValueType(ValueType.INTEGER, null)
        assertEquals(TrackerInputType.INTEGER, result)
    }

    // Test getInputTypeByValueType for text types
    @Test
    fun `should return LONG_TEXT for LONG_TEXT value type`() {
        val result = getInputTypeByValueType(ValueType.LONG_TEXT, null)
        assertEquals(TrackerInputType.LONG_TEXT, result)
    }

    @Test
    fun `should return EMAIL for EMAIL value type`() {
        val result = getInputTypeByValueType(ValueType.EMAIL, null)
        assertEquals(TrackerInputType.EMAIL, result)
    }

    @Test
    fun `should return PHONE_NUMBER for PHONE_NUMBER value type`() {
        val result = getInputTypeByValueType(ValueType.PHONE_NUMBER, null)
        assertEquals(TrackerInputType.PHONE_NUMBER, result)
    }

    // Test getInputTypeByValueType for BOOLEAN
    @Test
    fun `should return RADIO_BUTTON for BOOLEAN value type with default rendering`() {
        val result = getInputTypeByValueType(ValueType.BOOLEAN, null)
        assertEquals(TrackerInputType.RADIO_BUTTON, result)
    }

    @Test
    fun `should return CHECKBOX for BOOLEAN value type with horizontal checkboxes rendering`() {
        val result = getInputTypeByValueType(ValueType.BOOLEAN, UiRenderType.HORIZONTAL_CHECKBOXES)
        assertEquals(TrackerInputType.CHECKBOX, result)
    }

    @Test
    fun `should return CHECKBOX for BOOLEAN value type with vertical checkboxes rendering`() {
        val result = getInputTypeByValueType(ValueType.BOOLEAN, UiRenderType.VERTICAL_CHECKBOXES)
        assertEquals(TrackerInputType.CHECKBOX, result)
    }

    // Test getInputTypeByValueType for TRUE_ONLY
    @Test
    fun `should return YES_ONLY_CHECKBOX for TRUE_ONLY value type with default rendering`() {
        val result = getInputTypeByValueType(ValueType.TRUE_ONLY, null)
        assertEquals(TrackerInputType.YES_ONLY_CHECKBOX, result)
    }

    @Test
    fun `should return YES_ONLY_SWITCH for TRUE_ONLY value type with toggle rendering`() {
        val result = getInputTypeByValueType(ValueType.TRUE_ONLY, UiRenderType.TOGGLE)
        assertEquals(TrackerInputType.YES_ONLY_SWITCH, result)
    }

    // Test getInputTypeByValueType for date/time types
    @Test
    fun `should return DATE for DATE value type`() {
        val result = getInputTypeByValueType(ValueType.DATE, null)
        assertEquals(TrackerInputType.DATE, result)
    }

    @Test
    fun `should return DATE_TIME for DATETIME value type`() {
        val result = getInputTypeByValueType(ValueType.DATETIME, null)
        assertEquals(TrackerInputType.DATE_TIME, result)
    }

    @Test
    fun `should return TIME for TIME value type`() {
        val result = getInputTypeByValueType(ValueType.TIME, null)
        assertEquals(TrackerInputType.TIME, result)
    }

    @Test
    fun `should return AGE for AGE value type`() {
        val result = getInputTypeByValueType(ValueType.AGE, null)
        assertEquals(TrackerInputType.AGE, result)
    }

    // Test getInputTypeByValueType for other supported types
    @Test
    fun `should return ORGANISATION_UNIT for ORGANISATION_UNIT value type`() {
        val result = getInputTypeByValueType(ValueType.ORGANISATION_UNIT, null)
        assertEquals(TrackerInputType.ORGANISATION_UNIT, result)
    }

    @Test
    fun `should return MULTI_SELECTION for MULTI_TEXT value type`() {
        val result = getInputTypeByValueType(ValueType.MULTI_TEXT, null)
        assertEquals(TrackerInputType.MULTI_SELECTION, result)
    }

    // Test getInputTypeByValueType for NOT_SUPPORTED types
    @Test
    fun `should return NOT_SUPPORTED for USERNAME value type`() {
        val result = getInputTypeByValueType(ValueType.USERNAME, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for LETTER value type`() {
        val result = getInputTypeByValueType(ValueType.LETTER, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for UNIT_INTERVAL value type`() {
        val result = getInputTypeByValueType(ValueType.UNIT_INTERVAL, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for TRACKER_ASSOCIATE value type`() {
        val result = getInputTypeByValueType(ValueType.TRACKER_ASSOCIATE, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for REFERENCE value type`() {
        val result = getInputTypeByValueType(ValueType.REFERENCE, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for COORDINATE value type`() {
        val result = getInputTypeByValueType(ValueType.COORDINATE, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for IMAGE value type`() {
        val result = getInputTypeByValueType(ValueType.IMAGE, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for FILE_RESOURCE value type`() {
        val result = getInputTypeByValueType(ValueType.FILE_RESOURCE, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for GEOJSON value type`() {
        val result = getInputTypeByValueType(ValueType.GEOJSON, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for URL value type`() {
        val result = getInputTypeByValueType(ValueType.URL, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    @Test
    fun `should return NOT_SUPPORTED for null value type`() {
        val result = getInputTypeByValueType(null, null)
        assertEquals(TrackerInputType.NOT_SUPPORTED, result)
    }

    // Test getOrientation
    @Test
    fun `should return HORIZONTAL for horizontal checkboxes rendering type`() {
        val result = UiRenderType.HORIZONTAL_CHECKBOXES.getOrientation()
        assertEquals(Orientation.HORIZONTAL, result)
    }

    @Test
    fun `should return HORIZONTAL for horizontal radio buttons rendering type`() {
        val result = UiRenderType.HORIZONTAL_RADIOBUTTONS.getOrientation()
        assertEquals(Orientation.HORIZONTAL, result)
    }

    @Test
    fun `should return VERTICAL for vertical checkboxes rendering type`() {
        val result = UiRenderType.VERTICAL_CHECKBOXES.getOrientation()
        assertEquals(Orientation.VERTICAL, result)
    }

    @Test
    fun `should return VERTICAL for null rendering type`() {
        val result = (null as UiRenderType?).getOrientation()
        assertEquals(Orientation.VERTICAL, result)
    }

    @Test
    fun `should return VERTICAL for default rendering type`() {
        val result = UiRenderType.DEFAULT.getOrientation()
        assertEquals(Orientation.VERTICAL, result)
    }

    // Test getBooleanOptionConfiguration
    @Test
    fun `should return correct boolean option configuration`() {
        val result = getBooleanOptionConfiguration(resourceManager)

        assertEquals(2, result.options.size)
        assertEquals("true", result.options[0].code)
        assertEquals("Yes", result.options[0].displayName)
        assertEquals("false", result.options[1].code)
        assertEquals("No", result.options[1].displayName)
    }
}

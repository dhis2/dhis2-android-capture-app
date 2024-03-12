package org.dhis2.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R

val defaultFontFamily = FontFamily(
    Font(R.font.rubik_regular),
    Font(R.font.rubik_bold, FontWeight.Bold),
    Font(R.font.rubik_light, FontWeight.Light),
)

val descriptionTextStyle = TextStyle(
    color = Color(0xFF667685),
    fontSize = 10.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = FontFamily(Font(R.font.roboto_regular)),
    lineHeight = 16.sp,
    letterSpacing = (0.4).sp,
    textAlign = TextAlign.End,
)

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val DancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script, FontWeight.Normal),
    Font(R.font.dancing_script, FontWeight.SemiBold),
    Font(R.font.dancing_script, FontWeight.Bold)
)

val RobotoMonoFontFamily = FontFamily(
    Font(R.font.roboto_mono, FontWeight.Normal),
    Font(R.font.roboto_mono, FontWeight.Medium),
    Font(R.font.roboto_mono, FontWeight.Bold)
)

enum class AppFontChoice(
    val key: String,
    val label: String
) {
    PLUS_JAKARTA("PLUS_JAKARTA", "Modern"),
    PLAYFAIR("PLAYFAIR", "Klasik Serif"),
    DANCING_SCRIPT("DANCING_SCRIPT", "Latin Indah"),
    ROBOTO_MONO("ROBOTO_MONO", "Mono Rapi"),
    SYSTEM("SYSTEM", "Sistem");

    val fontFamily: FontFamily
        get() = when (this) {
            PLUS_JAKARTA -> PlusJakartaSansFontFamily
            PLAYFAIR -> PlayfairDisplayFontFamily
            DANCING_SCRIPT -> DancingScriptFontFamily
            ROBOTO_MONO -> RobotoMonoFontFamily
            SYSTEM -> FontFamily.Default
        }

    companion object {
        fun fromKey(key: String): AppFontChoice =
            entries.firstOrNull { it.key == key } ?: PLUS_JAKARTA
    }
}

enum class AppFontScale(
    val key: String,
    val label: String,
    val multiplier: Float
) {
    SMALL("SMALL", "Kecil", 0.88f),
    NORMAL("NORMAL", "Sedang", 1.0f),
    LARGE("LARGE", "Besar", 1.15f);

    companion object {
        fun fromKey(key: String): AppFontScale =
            entries.firstOrNull { it.key == key } ?: NORMAL
    }
}

enum class AppButtonSize(
    val key: String,
    val label: String,
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val iconSize: Dp,
    val fabSize: Dp
) {
    COMPACT(
        key = "COMPACT",
        label = "Kecil",
        height = 34.dp,
        horizontalPadding = 11.dp,
        verticalPadding = 5.dp,
        iconSize = 15.dp,
        fabSize = 40.dp
    ),
    MEDIUM(
        key = "MEDIUM",
        label = "Sedang",
        height = 40.dp,
        horizontalPadding = 15.dp,
        verticalPadding = 8.dp,
        iconSize = 18.dp,
        fabSize = 48.dp
    ),
    LARGE(
        key = "LARGE",
        label = "Besar",
        height = 48.dp,
        horizontalPadding = 20.dp,
        verticalPadding = 12.dp,
        iconSize = 20.dp,
        fabSize = 56.dp
    );

    companion object {
        fun fromKey(key: String): AppButtonSize =
            entries.firstOrNull { it.key == key } ?: COMPACT
    }
}

val BrandTitleTextStyle = TextStyle(
    fontFamily = DancingScriptFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.2.sp
)

fun brandTitleStyleForScale(fontScale: AppFontScale): TextStyle =
    BrandTitleTextStyle.copy(
        fontSize = (24f * fontScale.multiplier).sp,
        lineHeight = (28f * fontScale.multiplier).sp
    )

fun buildAppTypography(
    fontChoice: AppFontChoice = AppFontChoice.PLUS_JAKARTA,
    fontScale: AppFontScale = AppFontScale.NORMAL
): Typography {
    val scale = fontScale.multiplier
    val bodyFamily = fontChoice.fontFamily
    val displayFamily = when (fontChoice) {
        AppFontChoice.PLUS_JAKARTA -> PlayfairDisplayFontFamily
        else -> fontChoice.fontFamily
    }

    return Typography(
        displaySmall = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28f * scale).sp,
            lineHeight = (34f * scale).sp,
            letterSpacing = (-0.4).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24f * scale).sp,
            lineHeight = (30f * scale).sp,
            letterSpacing = (-0.25).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20f * scale).sp,
            lineHeight = (26f * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18f * scale).sp,
            lineHeight = (24f * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15f * scale).sp,
            lineHeight = (21f * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15f * scale).sp,
            lineHeight = (22f * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5f * scale).sp,
            lineHeight = (19f * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (11.5f * scale).sp,
            lineHeight = (15.5f * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * scale).sp,
            lineHeight = (15f * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (10.5f * scale).sp,
            lineHeight = (13.5f * scale).sp
        )
    )
}

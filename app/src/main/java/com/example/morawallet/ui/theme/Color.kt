package com.example.morawallet.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * MoraWallet palette: blue-to-cyan finance base with varied action/category accents.
 * Keep global surfaces calm; let category icons, charts, buttons, and cards carry color.
 */

private val Blue600 = Color(0xFF1F7CFF)
private val Blue500 = Color(0xFF2696F5)
private val Sky400 = Color(0xFF4DB7EF)
private val Cyan100 = Color(0xFFE6FBFA)
private val SurfaceBlue = Color(0xFFF1F8FF)
private val Ink900 = Color(0xFF102033)
private val Ink600 = Color(0xFF536579)

val LightPrimary = Blue600
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFD7ECFF)
val LightOnPrimaryContainer = Color(0xFF04315F)
val LightSecondary = Blue500
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFD9F2FF)
val LightOnSecondaryContainer = Color(0xFF07384F)
val LightTertiary = Sky400
val LightOnTertiary = Color(0xFF05263A)
val LightTertiaryContainer = Cyan100
val LightOnTertiaryContainer = Color(0xFF063B46)
val LightBackground = SurfaceBlue
val LightOnBackground = Ink900
val LightSurface = Color.White
val LightOnSurface = Ink900
val LightSurfaceVariant = Color(0xFFE7F3FC)
val LightOnSurfaceVariant = Ink600
val LightOutline = Color(0xFFB7D5EA)
val LightOutlineVariant = Color(0xFFD7EAF6)
val LightError = Color(0xFFE5485E)
val LightOnError = Color.White

val DarkPrimary = Color(0xFF8FCAFF)
val DarkOnPrimary = Color(0xFF003258)
val DarkPrimaryContainer = Color(0xFF145D9B)
val DarkOnPrimaryContainer = Color(0xFFD8EDFF)
val DarkSecondary = Color(0xFF79D9FF)
val DarkOnSecondary = Color(0xFF003447)
val DarkSecondaryContainer = Color(0xFF0B637E)
val DarkOnSecondaryContainer = Color(0xFFD5F5FF)
val DarkTertiary = Color(0xFF9BE6E0)
val DarkOnTertiary = Color(0xFF003734)
val DarkTertiaryContainer = Color(0xFF0B6C68)
val DarkOnTertiaryContainer = Color(0xFFE6FBFA)
val DarkBackground = Color(0xFF071827)
val DarkOnBackground = Color(0xFFE7F3FC)
val DarkSurface = Color(0xFF10263A)
val DarkOnSurface = Color(0xFFE7F3FC)
val DarkSurfaceVariant = Color(0xFF17344D)
val DarkOnSurfaceVariant = Color(0xFFB9D4E8)
val DarkOutline = Color(0xFF5D829D)
val DarkOutlineVariant = Color(0xFF31536D)
val DarkError = Color(0xFFFF8596)
val DarkOnError = Color(0xFF45000B)

val IncomeGreen = Color(0xFF13A079)
val IncomeGreenDark = Color(0xFF5FE2C2)
val ExpenseRed = Color(0xFFE5485E)
val ExpenseRedDark = Color(0xFFFF8596)
val TransferBlue = Color(0xFF1F7CFF)
val TransferBlueDark = Color(0xFF8FCAFF)

val BorderSubtleLight = Color(0xFFD7EAF6)
val BorderStrongLight = Color(0xFFB7D5EA)
val BorderSubtleDark = Color(0xFF31536D)
val BorderStrongDark = Color(0xFF5D829D)

val SurfaceRaisedLight = Color.White
val SurfaceRaisedDark = Color(0xFF10263A)

val ChartPaletteLight: List<Color> = listOf(
    Color(0xFF1F7CFF),
    Color(0xFF24A7F2),
    Color(0xFF61C5E8),
    Color(0xFF13A079),
    Color(0xFFFFB020),
    Color(0xFFE5485E),
    Color(0xFF8A63D2),
    Color(0xFF00A7A7),
)

val ChartPaletteDark: List<Color> = listOf(
    Color(0xFF8FCAFF),
    Color(0xFF79D9FF),
    Color(0xFF9BE6E0),
    Color(0xFF5FE2C2),
    Color(0xFFFFC861),
    Color(0xFFFF8596),
    Color(0xFFC6A8FF),
    Color(0xFF5DE0E0),
)

fun paletteColor(index: Int): Color = CategoryColors[index.mod(CategoryColors.size)]

val CategoryColors: List<Color> = listOf(
    Color(0xFF1F7CFF),
    Color(0xFF24A7F2),
    Color(0xFF00A7A7),
    Color(0xFF13A079),
    Color(0xFFFFB020),
    Color(0xFFE5485E),
    Color(0xFF8A63D2),
    Color(0xFFEF7C45),
    Color(0xFF5DADEC),
    Color(0xFF45C4B0),
    Color(0xFF6074D8),
    Color(0xFF7B90A6),
)

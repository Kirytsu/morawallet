package com.example.morawallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors that Material's [androidx.compose.material3.ColorScheme] does not model.
 *
 * App code reads these by intent — `MoraTheme.colors.income`, `...borderSubtle` — instead of
 * reaching for raw hues or guessing which Material slot to abuse. Values are provided per
 * light/dark in [MoraWalletTheme].
 */
data class MoraSemanticColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val positive: Color,
    val negative: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val surfaceRaised: Color,
    /** Harmonized palette for chart series / slices. */
    val chart: List<Color>,
)

internal val LightMoraColors = MoraSemanticColors(
    income = IncomeGreen,
    expense = ExpenseRed,
    transfer = TransferBlue,
    positive = IncomeGreen,
    negative = ExpenseRed,
    borderSubtle = BorderSubtleLight,
    borderStrong = BorderStrongLight,
    surfaceRaised = SurfaceRaisedLight,
    chart = ChartPaletteLight,
)

internal val DarkMoraColors = MoraSemanticColors(
    income = IncomeGreenDark,
    expense = ExpenseRedDark,
    transfer = TransferBlueDark,
    positive = IncomeGreenDark,
    negative = ExpenseRedDark,
    borderSubtle = BorderSubtleDark,
    borderStrong = BorderStrongDark,
    surfaceRaised = SurfaceRaisedDark,
    chart = ChartPaletteDark,
)

val LocalMoraColors = staticCompositionLocalOf { LightMoraColors }

/** Entry point for MoraWallet's semantic design tokens: `MoraTheme.colors.income`. */
object MoraTheme {
    val colors: MoraSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMoraColors.current
}

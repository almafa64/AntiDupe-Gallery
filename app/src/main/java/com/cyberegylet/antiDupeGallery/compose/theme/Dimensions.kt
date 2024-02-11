package com.cyberegylet.antiDupeGallery.compose.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.cyberegylet.antiDupeGallery.compose.theme.model.Dimensions

val LocalDimensions: ProvidableCompositionLocal<Dimensions> = staticCompositionLocalOf {
	Dimensions(
		padding = Dimensions.Paddings(
			extraSmall = 2.dp,
			small = 4.dp,
			medium = 8.dp,
			large = 12.dp,
			extraLarge = 16.dp,
		),
		icon = Dimensions.IconSizes(
			extraSmall = 24.dp,
			small = 32.dp,
			medium = 48.dp,
			large = 56.dp,
			extraLarge = 64.dp,
		)
	)
}
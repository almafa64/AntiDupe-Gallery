package com.cyberegylet.antiDupeGallery.compose

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.compose.theme.AntiDupeGallerySurface
import com.cyberegylet.antiDupeGallery.compose.theme.DataTheme

class AboutActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContent {
			val context: Context = LocalContext.current
			val resources: Resources = context.resources
			AntiDupeGallerySurface {
				ColumnScaffold(title = stringResource(R.string.about_header_text), goBack = ::finish) {
					Group(title = {
						Header(resources.getString(R.string.about_support))
					}) {
						Row(resources.getString(R.string.email), R.drawable.ic_email)
						HorizontalDivider()
					}
					Group(title = {
						Header(resources.getString(R.string.about_other))
					}) {
						Row(resources.getString(R.string.version_text), R.drawable.ic_info)
						HorizontalDivider()
					}
				}
			}
		}

		ViewCompat.setOnApplyWindowInsetsListener(window.decorView.findViewById(android.R.id.content)) { v, windowInsets ->
			val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

			v.updateLayoutParams<MarginLayoutParams> {
				leftMargin = insets.left
				bottomMargin = insets.bottom
				topMargin = insets.top
				rightMargin = insets.right
			}

			WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.systemBars(), insets).build()
		}
	}

	@Composable
	fun Header(txt: String)
	{
		Box(modifier = Modifier.padding(top = DataTheme.dimens.padding.extraLarge))
		{
			Text(
				text = txt.uppercase(),
				modifier = Modifier
					.padding(start = 60.dp)
					.padding(horizontal = DataTheme.dimens.padding.small)
			)
		}
	}

	@Composable
	fun Row(txt: String, @DrawableRes icon: Int? = null)
	{
		ListItem(
			headlineContent = { Text(text = txt) },
			leadingContent = {
				val imageSize = Modifier
					.size(DataTheme.dimens.icon.medium)
					.padding(DataTheme.dimens.padding.medium)
				when
				{
					icon != null -> Icon(
						painter = painterResource(icon),
						contentDescription = null,
						modifier = imageSize
					)

					else -> Box(modifier = imageSize)
				}
			}
		)
	}

	@Composable
	fun Group(
		modifier: Modifier = Modifier,
		title: @Composable (() -> Unit)? = null,
		content: @Composable ColumnScope.() -> Unit,
	)
	{
		Column(modifier = modifier.fillMaxWidth()) {
			if (title != null)
			{
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = DataTheme.dimens.padding.extraLarge),
					contentAlignment = Alignment.CenterStart
				)
				{
					val primary = DataTheme.colorScheme.primary
					val style = DataTheme.typography.headlineMedium.copy(color = primary)
					ProvideTextStyle(value = style) { title() }
				}
			}
			content()
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun ColumnScaffold(title: String, goBack: () -> Unit, content: @Composable (ColumnScope.(PaddingValues) -> Unit))
	{
		val scrollState = rememberScrollState()
		val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
		Scaffold(modifier = Modifier
			.fillMaxSize()
			.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
			TopAppBar(
				title = {
					Text(
						text = title,
						modifier = Modifier
							.padding(start = DataTheme.dimens.padding.medium)
							.fillMaxWidth(),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				},
				navigationIcon = {
					Box(modifier = Modifier
						//.topAppB
						.padding(start = DataTheme.dimens.padding.medium)
						.clip(RoundedCornerShape(50))
						.clickable(
							remember { MutableInteractionSource() },
							rememberRipple(color = DataTheme.colorScheme.onSurface, bounded = true)
						) { goBack() })
					{
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(R.string.back_arrow_text),
							modifier = Modifier.padding(DataTheme.dimens.padding.small)
						)
					}
				}
			)
		})
		{ paddingValues ->
			val layoutDir = LocalLayoutDirection.current
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(DataTheme.colorScheme.surface)
					.padding(
						top = paddingValues.calculateTopPadding(),
						start = paddingValues.calculateStartPadding(layoutDir),
						end = paddingValues.calculateEndPadding(layoutDir),
					)
			) {
				Column(
					modifier = Modifier
						.matchParentSize()
						.verticalScroll(scrollState),
					horizontalAlignment = Alignment.Start,
					verticalArrangement = Arrangement.Top
				) {
					content(paddingValues)
					Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
				}
			}
		}
	}

	@Preview(name = "Light", showBackground = true, showSystemUi = true)
	@Preview(name = "Dark", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
	@Preview(name = "hu", showBackground = true, showSystemUi = true, locale = "hu")
	@Preview(
		name = "landscape",
		showBackground = true,
		showSystemUi = true,
		device = "spec:parent=pixel_5,orientation=landscape",
	)
	@Composable
	fun Preview()
	{
		val context: Context = LocalContext.current
		val resources: Resources = context.resources
		AntiDupeGallerySurface(dynamicColor = false) {
			ColumnScaffold(title = stringResource(R.string.about_header_text), goBack = {}) {
				Group(title = {
					Header(resources.getString(R.string.about_support))
				}) {
					Row(resources.getString(R.string.email), R.drawable.ic_email)
					HorizontalDivider()
				}
				Group(title = {
					Header(resources.getString(R.string.about_other))
				}) {
					Row(resources.getString(R.string.version_text), R.drawable.ic_info)
					HorizontalDivider()
				}
			}
		}
	}
}

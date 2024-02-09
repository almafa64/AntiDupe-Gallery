package com.cyberegylet.antiDupeGallery.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.activities.ui.theme.AntiDupeGalleryTheme

class AboutActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		/*val generator = SimpleActivityGenerator(this, true, R.string.about_header_text)
		generator.newHeader(R.string.about_support)
		generator.addRow(R.string.email, R.drawable.ic_email)
		generator.newHeader(R.string.about_other)
		generator.addRow(R.string.version_text, R.drawable.ic_info)
		findViewById<View>(R.id.back_button).setOnClickListener { v: View? -> goBack(this@AboutActivity) }*/

		setContent {
			AntiDupeGalleryTheme {
				Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
					ShowText(txt = "test")
				}
			}
		}
	}

	@Composable
	fun ShowText(txt: String)
	{
		Text(text = txt)
	}

	@Preview(
		name = "Light",
		showBackground = true,
		showSystemUi = true,
	)
	@Preview(
		name = "Dark",
		showBackground = true,
		showSystemUi = true,
		uiMode = Configuration.UI_MODE_NIGHT_YES
	)
	@Composable
	fun Preview()
	{
		AntiDupeGalleryTheme {
			Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
				ShowText(txt = "preview")
			}
		}
	}
}

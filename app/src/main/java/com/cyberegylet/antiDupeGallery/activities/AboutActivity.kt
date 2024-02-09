package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.helpers.SimpleActivityGenerator;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;

public class AboutActivity extends Activity
{
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SimpleActivityGenerator generator = new SimpleActivityGenerator(this, true, R.string.about_header_text);
		generator.newHeader(R.string.about_support);
		generator.addRow(R.string.email, R.drawable.ic_email);
		generator.newHeader(R.string.about_other);
		generator.addRow(R.string.version_text, R.drawable.ic_info);

		findViewById(R.id.back_button).setOnClickListener(v -> ActivityManager.goBack(AboutActivity.this));
	}
}

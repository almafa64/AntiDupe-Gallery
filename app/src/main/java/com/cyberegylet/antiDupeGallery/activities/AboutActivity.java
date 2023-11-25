package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.SimpleActivityGenerator;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

public class AboutActivity extends Activity
{
	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SimpleActivityGenerator generator = new SimpleActivityGenerator(this, R.string.about_header_text);
		generator.newHeader(R.string.about_support);
		generator.addRow(R.drawable.ic_email, R.string.company_email);
		generator.newHeader(R.string.about_other);
		generator.addRow(R.drawable.ic_info, R.string.version_text);

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());
	}
}

package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.ConfigManager;
import com.cyberegylet.antiDupeGallery.backend.SimpleActivityGenerator;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

public class SettingsActivity extends Activity
{
	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SimpleActivityGenerator generator = new SimpleActivityGenerator(this, false, R.string.settings_header);

		SwitchCompat toggleButton = new SwitchCompat(this);
		toggleButton.setChecked(ConfigManager.getBooleanConfig(ConfigManager.Config.ANIMATE_GIF));
		toggleButton.setOnCheckedChangeListener((v, checked) -> ConfigManager.setBooleanConfig(ConfigManager.Config.ANIMATE_GIF, checked));
		generator.newHeader(R.string.settings_thumbnail_heading);
		generator.addRow(null, R.string.settings_animate_gif, toggleButton);

		findViewById(R.id.back_button).setOnClickListener(v -> {
			activityManager.goBack();
			ConfigManager.saveConfigs();
		});
	}
}

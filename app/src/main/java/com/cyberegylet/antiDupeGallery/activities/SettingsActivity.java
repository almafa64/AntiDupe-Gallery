package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

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

		generator.newHeader(R.string.settings_general_heading);
		generator.addRow(R.string.settings_pin, v -> {
			// ToDo open popup
			View popup = getLayoutInflater().inflate(R.layout.dialog_enter_pin, null);
			PopupWindow window = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			window.showAtLocation(v, Gravity.CENTER, 0, 0);
		});
		generator.addConfigCheckRow(R.string.settings_show_hidden, ConfigManager.Config.SHOW_HIDDEN);

		generator.newHeader(R.string.settings_thumbnail_heading);
		generator.addConfigCheckRow(R.string.settings_animate_gif, ConfigManager.Config.ANIMATE_GIF);

		findViewById(R.id.back_button).setOnClickListener(v -> {
			activityManager.goBack();
			ConfigManager.saveConfigs();
		});
	}
}

package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

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

		CheckBox checkBox = new CheckBox(this);
		checkBox.setChecked(ConfigManager.getConfig(ConfigManager.Config.PIN_LOCk).length() != 0);
		checkBox.setOnCheckedChangeListener((v, check) -> {
			if(!check)
			{
				ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, "");
				return;
			}
			// ToDo fix dialogue
			ViewGroup popup = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_enter_pin, null);
			TextView text = (TextView) popup.getChildAt(0);
			text.setText(R.string.pin_enter_pin);
			PopupWindow window = new PopupWindow(popup, 400, 400, true); // ToDo auto resize
			window.showAtLocation(v, Gravity.CENTER, 0, 0);
			popup.setOnTouchListener((v2, e) -> {
				window.dismiss();
				v2.performClick();
				return true;
			});
			EditText editText = (EditText) popup.getChildAt(1);
			final String[] tmpPin = new String[] {""};
			editText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

				@Override
				public void afterTextChanged(Editable s) { }

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					if (s.length() == 4)
					{
						if(text.getText() == getResources().getText(R.string.pin_enter_pin))
						{
							text.setText(R.string.pin_enter_pin_again);
							tmpPin[0] = s.toString();
							editText.getText().clear();
						}
						else if(s.toString().equals(tmpPin[0]))
						{
							ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, s.toString());
							window.dismiss();
						}
						else
						{
							text.setText(R.string.pin_enter_pin);
							editText.getText().clear();
						}
					}
				}
			});
		});
		generator.addRow(R.string.settings_pin, checkBox);
		generator.addConfigCheckRow(R.string.settings_show_hidden, ConfigManager.Config.SHOW_HIDDEN);
		generator.addConfigCheckRow(R.string.settings_use_bin, ConfigManager.Config.USE_BIN);

		generator.newHeader(R.string.settings_thumbnail_heading);
		generator.addConfigCheckRow(R.string.settings_animate_gif, ConfigManager.Config.ANIMATE_GIF);

		generator.newHeader(R.string.settings_danger_header);
		generator.addRow(R.string.settings_reset, v -> ConfigManager.resetConfigs());

		findViewById(R.id.back_button).setOnClickListener(v -> {
			activityManager.goBack();
			ConfigManager.saveConfigs();
		});
	}
}

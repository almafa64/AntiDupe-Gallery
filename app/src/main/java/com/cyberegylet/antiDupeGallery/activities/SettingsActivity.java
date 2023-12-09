package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.ConfigManager;
import com.cyberegylet.antiDupeGallery.backend.SimpleActivityGenerator;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

public class SettingsActivity extends Activity
{
	private static final String TAG = "SettingsActivity";

	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SimpleActivityGenerator generator = new SimpleActivityGenerator(this, false, R.string.settings_header);

		generator.newHeader(R.string.settings_general_heading);

		AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
		checkBox.setChecked(ConfigManager.getConfig(ConfigManager.Config.PIN_LOCK).length() != 0);
		checkBox.setOnClickListener(v -> {
			if (!checkBox.isChecked())
			{
				ConfigManager.setConfig(ConfigManager.Config.PIN_LOCK, "");
				return;
			}
			final boolean[] isGood = { false };
			final String[] tmpPin = new String[]{ "" };
			ViewGroup popup = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_enter_pin, (ViewGroup) v.getRootView(), false);
			TextView text = popup.findViewById(R.id.header_title);
			text.setText(R.string.pin_enter_pin);
			PopupWindow window = new PopupWindow(popup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
			ActivityManager.applyDim(root, 0.5f);
			window.showAtLocation(v, Gravity.CENTER, 0, 0);
			window.setOnDismissListener(() -> {
				if (!isGood[0]) checkBox.setChecked(false);
				ActivityManager.clearDim(root);
			});
			EditText editText = popup.findViewById(R.id.pin_input);
			editText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

				@Override
				public void afterTextChanged(Editable s)
				{
					if (s.length() == 4) editText.getText().clear();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					if (s.length() == 4)
					{
						if (text.getText() == getResources().getText(R.string.pin_enter_pin))
						{
							text.setText(R.string.pin_enter_pin_again);
							tmpPin[0] = s.toString();
						}
						else if (s.toString().equals(tmpPin[0]))
						{
							ConfigManager.setConfig(ConfigManager.Config.PIN_LOCK, s.toString());
							isGood[0] = true;
							window.dismiss();
						}
						else
						{
							text.setText(R.string.pin_enter_pin);
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

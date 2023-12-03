package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
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
	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SimpleActivityGenerator generator = new SimpleActivityGenerator(this, false, R.string.settings_header);

		generator.newHeader(R.string.settings_general_heading);

		AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
		checkBox.setChecked(ConfigManager.getConfig(ConfigManager.Config.PIN_LOCk).length() != 0);
		checkBox.setOnClickListener(v -> {
			if (!checkBox.isChecked())
			{
				ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, "");
				return;
			}
			final boolean[] isGood = { false };
			final String[] tmpPin = new String[]{ "" };
			ViewGroup popup = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_enter_pin, (ViewGroup) v.getRootView(), false);
			TextView text = (TextView) popup.getChildAt(0);
			text.setText(R.string.pin_enter_pin);
			popup.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			DisplayMetrics metrics = this.getResources().getDisplayMetrics();
			float ratio = ((float)metrics.heightPixels / (float)metrics.widthPixels);
			PopupWindow window = new PopupWindow(popup, (int)ratio * popup.getMeasuredHeight() / 2, popup.getMeasuredHeight(), true);
			window.showAtLocation(v, Gravity.CENTER, 0, 0);
			window.setOnDismissListener(() -> {
				if(!isGood[0]) checkBox.setChecked(false);
			});
			EditText editText = (EditText) popup.getChildAt(1);
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
						if (text.getText() == getResources().getText(R.string.pin_enter_pin))
						{
							text.setText(R.string.pin_enter_pin_again);
							tmpPin[0] = s.toString();
							editText.getText().clear();
						}
						else if (s.toString().equals(tmpPin[0]))
						{
							ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, s.toString());
							isGood[0] = true;
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
		/*checkBox.setOnCheckedChangeListener((v, check) -> {
			if (!check)
			{
				ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, "");
				return;
			}
			ViewGroup popup = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_enter_pin, (ViewGroup) v.getRootView(), false);
			TextView text = (TextView) popup.getChildAt(0);
			text.setText(R.string.pin_enter_pin);
			popup.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			DisplayMetrics metrics = this.getResources().getDisplayMetrics();
			float ratio = ((float)metrics.heightPixels / (float)metrics.widthPixels);
			PopupWindow window = new PopupWindow(popup, (int)ratio * popup.getMeasuredHeight(), popup.getMeasuredHeight(), true);
			window.showAtLocation(v, Gravity.CENTER, 0, 0);
			window.setOnDismissListener(() -> checkBox.setChecked(false));
			EditText editText = (EditText) popup.getChildAt(1);
			final String[] tmpPin = new String[]{ "" };
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
						if (text.getText() == getResources().getText(R.string.pin_enter_pin))
						{
							text.setText(R.string.pin_enter_pin_again);
							tmpPin[0] = s.toString();
							editText.getText().clear();
						}
						else if (s.toString().equals(tmpPin[0]))
						{
							ConfigManager.setConfig(ConfigManager.Config.PIN_LOCk, s.toString());
							window.dismiss();
							checkBox.setChecked(true);
						}
						else
						{
							text.setText(R.string.pin_enter_pin);
							editText.getText().clear();
						}
					}
				}
			});
		});*/
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

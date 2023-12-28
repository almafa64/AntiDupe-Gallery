package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.Config;
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
		checkBox.setChecked(Config.getStringProperty(Config.Property.PIN_LOCK).length() != 0);
		checkBox.setOnClickListener(v -> {
			if (!checkBox.isChecked())
			{
				Config.setStringProperty(Config.Property.PIN_LOCK, "");
				return;
			}
			final boolean[] isGood = { false };
			final String[] tmpPin = new String[]{ "" };
			PopupWindow window = activityManager.MakePopupWindow(R.layout.dialog_enter_pin, () -> {
				if (!isGood[0]) checkBox.setChecked(false);
			});
			ViewGroup popup = (ViewGroup) window.getContentView();
			TextView text = popup.findViewById(R.id.header_title);
			text.setText(R.string.pin_enter_pin);
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
							Config.setStringProperty(Config.Property.PIN_LOCK, s.toString());
							isGood[0] = true;
							window.dismiss();
						}
						else
						{
							text.setText(R.string.pin_enter_pin);
							Toast.makeText(activityManager.activity, R.string.pin_didnt_match, Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		});
		generator.addRow(R.string.settings_pin, checkBox);
		generator.addConfigCheckRow(R.string.settings_show_hidden, Config.Property.SHOW_HIDDEN);
		generator.addConfigCheckRow(R.string.settings_use_bin, Config.Property.USE_BIN);

		generator.newHeader(R.string.settings_thumbnail_heading);
		generator.addConfigCheckRow(R.string.settings_animate_gif, Config.Property.ANIMATE_GIF);

		generator.newHeader(R.string.settings_danger_header);
		generator.addRow(R.string.settings_reset, v -> Config.restoreDefaults());

		findViewById(R.id.back_button).setOnClickListener(v -> {
			activityManager.goBack();
			Config.save();
		});
	}

	@Override
	protected void onStop()
	{
		Config.save();
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Config.save();
		super.onDestroy();
	}
}

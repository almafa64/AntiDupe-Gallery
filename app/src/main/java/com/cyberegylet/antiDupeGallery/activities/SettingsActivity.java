package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.helpers.SimpleActivityGenerator;

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
			PopupWindow window = activityManager.makePopupWindow(R.layout.dialog_enter_pin, () -> {
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
							Toast.makeText(activityManager.activity, R.string.pin_didnt_match, Toast.LENGTH_SHORT)
									.show();
						}
					}
				}
			});
		});
		generator.addRow(R.string.settings_pin, checkBox);

		generator.addConfigCheckRow(R.string.settings_show_hidden, Config.Property.SHOW_HIDDEN);
		generator.addConfigCheckRow(R.string.settings_use_bin, Config.Property.USE_BIN);

		generator.addRow(R.string.settings_sort_button, (v) -> {
			ViewGroup popup = (ViewGroup) activityManager.makePopupWindow(R.layout.dialog_sorting).getContentView();
			Spinner folderSpinner = popup.findViewById(R.id.sorting_folder_type);
			Spinner imageSpinner = popup.findViewById(R.id.sorting_image_type);
			CheckBox folderAsc = popup.findViewById(R.id.sorting_folder_asc);
			CheckBox imageAsc = popup.findViewById(R.id.sorting_image_asc);

			String folderSort = Config.getStringProperty(Config.Property.FOLDER_SORT);
			String imageSort = Config.getStringProperty(Config.Property.IMAGE_SORT);

			folderAsc.setChecked(ConfigSort.isAscending(folderSort));
			imageAsc.setChecked(ConfigSort.isAscending(imageSort));
			folderSpinner.setSelection(Character.getNumericValue(folderSort.charAt(1)));
			imageSpinner.setSelection(Character.getNumericValue(imageSort.charAt(1)));

			folderAsc.setOnCheckedChangeListener((v2, checked) -> Config.setStringProperty(
					Config.Property.FOLDER_SORT,
					(checked ? "1" : "0") + Config.getStringProperty(Config.Property.FOLDER_SORT).charAt(1)
			));
			imageAsc.setOnCheckedChangeListener((v2, checked) -> Config.setStringProperty(
					Config.Property.IMAGE_SORT,
					(checked ? "1" : "0") + Config.getStringProperty(Config.Property.IMAGE_SORT).charAt(1)
			));

			folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					Config.setStringProperty(
							Config.Property.FOLDER_SORT,
							Config.getStringProperty(Config.Property.FOLDER_SORT).charAt(0) + String.valueOf(position)
					);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) { }
			});

			imageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					Config.setStringProperty(
							Config.Property.IMAGE_SORT,
							Config.getStringProperty(Config.Property.IMAGE_SORT).charAt(0) + String.valueOf(position)
					);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) { }
			});
		});

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

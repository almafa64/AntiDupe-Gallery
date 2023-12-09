package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.ConfigManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;

public class PinActivity extends Activity
{
	private static final String TAG = "PinActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pin_activity);

		EditText editText = findViewById(R.id.pin_input);
		editText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void afterTextChanged(Editable s) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() == 4 && ConfigManager.getConfig(ConfigManager.Config.PIN_LOCK).equals(s.toString()))
				{
					ActivityManager.switchActivity(PinActivity.this, MainActivity.class, new ActivityParameter("login", true));
				}
			}
		});
	}

	@Override
	public void onBackPressed() { }
}

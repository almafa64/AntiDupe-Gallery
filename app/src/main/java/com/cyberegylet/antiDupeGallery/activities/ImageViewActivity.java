package com.cyberegylet.antiDupeGallery.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.Serializable;

public class ImageViewActivity extends Activity implements Serializable
{
	private static final String TAG = "ImageViewActivity";
	private static final float MAX_SCALE = Float.MAX_VALUE;
	private final ActivityManager activityManager = new ActivityManager(this);

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		String imagePath;
		Intent intent = getIntent();
		//ToDo make safe
		if (Intent.ACTION_VIEW.equals(intent.getAction())) imagePath = intent.getData().getPath();
		else imagePath = (String) activityManager.getParam("imagePath");

		setContentView(R.layout.image_view);


		TextView textView = findViewById(R.id.activity_header);
		textView.setText(new File(imagePath).getName());

		SubsamplingScaleImageView imageView = findViewById(R.id.imageView);
		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		imageView.setMaxScale(MAX_SCALE);
		imageView.setImage(ImageSource.uri(imagePath));
	}
}

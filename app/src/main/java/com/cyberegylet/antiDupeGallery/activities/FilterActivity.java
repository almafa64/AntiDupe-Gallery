package com.cyberegylet.antiDupeGallery.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.FilterAdapter;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.helpers.PermissionManager;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum;
import com.cyberegylet.antiDupeGallery.services.FilterService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;

public class FilterActivity extends AppCompatActivity
{
	private static final String TAG = "FilterActivity";

	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_activity);

		Config.init(getApplicationContext());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			new PermissionManager(this).requestPermissions(p -> {
				if (p != null && p.length > 0)
				{
					Toast.makeText(this, R.string.permission_notification_denied, Toast.LENGTH_SHORT).show();
					activityManager.goBack();
				}
				return Unit.INSTANCE;
			}, Manifest.permission.POST_NOTIFICATIONS);
		}

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		String[] paths = activityManager.getStringArray("paths");

		RecyclerView recycler = findViewById(R.id.recycler);
		recycler.setLayoutManager(new GridLayoutManager(
				this,
				Config.getIntProperty(Config.Property.ALBUM_COLUMN_NUMBER)
		));

		if (FilterService.isRunning())
		{
			RecyclerView oldRecycler = Objects.requireNonNull(FilterService.recyclerViewMutable.getValue());
			recycler.setAdapter(oldRecycler.getAdapter());
			FilterService.recyclerViewMutable.setValue(recycler);

			if (activityManager.getParam(FilterService.FILTER_DONE_PARAM) != null)
			{
				FilterService filterService = Objects.requireNonNull(FilterService.getFilterService());
				filterService.stopNotification();
				filterService.stop();
			}

			return;
		}

		FilterService.recyclerViewMutable.setValue(recycler);
		setStateToService(true);

		List<FilteredAlbum> albums = new ArrayList<>();
		FilterAdapter adapter = new FilterAdapter(albums, new FileManager(this));
		recycler.setAdapter(adapter);

		Intent i = new Intent(getApplicationContext(), FilterService.class);
		i.setAction(FilterService.ACTION_START_FILTERING);
		i.putExtra(FilterService.PATHS_PARAM, paths);
		startService(i);
	}

	private void setStateToService(Boolean isOpen)
	{
		FilterService.isFilterActivityOpen = isOpen;
	}

	@Override
	protected void onDestroy()
	{
		setStateToService(false);
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		setStateToService(false);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		setStateToService(true);
	}
}
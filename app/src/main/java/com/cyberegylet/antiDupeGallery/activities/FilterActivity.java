package com.cyberegylet.antiDupeGallery.activities;

import android.Manifest;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.FilterAdapter;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.helpers.PermissionManager;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum;
import com.cyberegylet.antiDupeGallery.services.FilterService;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class FilterActivity extends AppCompatActivity
{
	private static final String TAG = "FilterActivity";

	private final ActivityManager activityManager = new ActivityManager(this);
	private SQLiteDatabase database;

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

		/*Object pathsObj = activityManager.getParam("paths");
		List<String> paths = null;
		if (pathsObj != null) paths = Arrays.asList((String[]) pathsObj);*/

		String[] paths = activityManager.getStringArray("paths");

		//List<String> paths = Arrays.asList((String[]) pathsObj);

		RecyclerView recycler = findViewById(R.id.recycler);
		FilterService.mutableLiveData.setValue(recycler);

		if (paths == null) return;
		
		List<FilteredAlbum> albums = new ArrayList<>();
		FilterAdapter adapter = new FilterAdapter(albums, new FileManager(this));
		recycler.setLayoutManager(new GridLayoutManager(
				this,
				Config.getIntProperty(Config.Property.ALBUM_COLUMN_NUMBER)
		));
		recycler.setAdapter(adapter);

		database = Cache.getCache();

		Intent i = new Intent(getApplicationContext(), FilterService.class);
		i.setAction(FilterService.ACTION_START_FILTERING);
		i.putExtra(FilterService.PATHS_PARAM, paths);
		startService(i);
	}
}

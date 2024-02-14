package com.cyberegylet.antiDupeGallery.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;

import kotlin.Unit;

public abstract class ImageListBaseActivity extends AppCompatActivity
{
	protected static final int MOVE_SELECTED = 1;
	protected static final int COPY_SELECTED = 2;
	protected static final int DELETE_SELECTED = 3;

	public final String TAG;
	public static SQLiteDatabase database;
	protected FileManager fileManager;
	protected RecyclerView recycler;
	protected final ActivityManager activityManager = new ActivityManager(this);
	protected SearchView search;

	protected final ActivityResultLauncher<Intent> moveLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			MOVE_SELECTED,
			o.getResultCode(),
			o.getData()
	));

	protected final ActivityResultLauncher<Intent> copyLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			COPY_SELECTED,
			o.getResultCode(),
			o.getData()
	));

	protected ImageListBaseActivity(String tag) { TAG = tag; }

	protected abstract boolean myOnCreate(@Nullable Bundle savedInstanceState);

	protected void contentSet()
	{
		search = findViewById(R.id.search_bar);
		recycler = findViewById(R.id.recycler);
		database = Cache.getCache();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Cache.init(this);
		if (!myOnCreate(savedInstanceState)) return;
		if (recycler == null) throw new RuntimeException("contentSet() wasn't called");

		int span = Config.getIntProperty((this instanceof AlbumActivity) ? Config.Property.ALBUM_COLUMN_NUMBER : Config.Property.IMAGE_COLUMN_NUMBER);
		recycler.setLayoutManager(new GridLayoutManager(this, span));

		search.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query) { return false; }

			@Override
			public boolean onQueryTextChange(String text)
			{
				filterRecycle(text);
				return true;
			}
		});

		fileManager = new FileManager(this);
		fileManager.requestStoragePermissions(permissions -> {
			if (permissions == null || permissions.length == 0) storageAccessGranted();
			else
			{
				Toast.makeText(this, getString(R.string.permission_storage_denied), Toast.LENGTH_SHORT).show();
				finishAndRemoveTask();
			}
			return Unit.INSTANCE;
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

	@Override
	protected void onResume()
	{
		super.onResume();
		if (recycler == null || recycler.getAdapter() == null) return;
		filterRecycle(search.getQuery().toString());
	}

	protected abstract void myOnActivityResult(int requestCode, int resultCode, Intent data);

	protected abstract void storageAccessGranted();

	// dependency for rust lib
	protected String getDbPath(String name) { return getDatabasePath(name).getAbsolutePath(); }

	protected abstract void filterRecycle(String text);
}
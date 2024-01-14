package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

import java.util.Arrays;

public abstract class ImageListBaseActivity extends Activity
{
	public final String TAG;

	private static final String DATABASE_NAME = "data.db";
	public static SQLiteDatabase database;

	protected FileManager fileManager;
	protected RecyclerView recycler;
	protected final ActivityManager activityManager = new ActivityManager(this);
	protected SearchView search;

	protected ImageListBaseActivity(String tag)
	{
		TAG = tag;
	}

	protected abstract void myOnCreate(@Nullable Bundle savedInstanceState);

	protected void contentSet()
	{
		search = findViewById(R.id.search_bar);
		recycler = findViewById(R.id.items);
		database = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(DATABASE_NAME), null);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		myOnCreate(savedInstanceState);
		if (recycler == null) throw new RuntimeException("contentSet() wasn't called");

		int span = Config.getIntProperty((this instanceof FoldersActivity) ? Config.Property.FOLDER_COLUMN_NUMBER : Config.Property.IMAGE_COLUMN_NUMBER);
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
		if (fileManager.hasFileAccess()) fileThings();
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

	@Override
	protected abstract void onActivityResult(int requestCode, int resultCode, Intent data);

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == FileManager.STORAGE_REQUEST_CODE && Arrays.stream(grantResults)
				.allMatch(v -> v == PackageManager.PERMISSION_GRANTED))
		{
			fileThings();
		}
		else
		{
			Toast.makeText(this, getString(R.string.no_storage_permission), Toast.LENGTH_SHORT).show();
			finishAndRemoveTask();
		}
	}

	protected abstract void fileThings();

	protected String getDbPath(String name) { return getDatabasePath(name).getAbsolutePath(); }

	protected abstract void filterRecycle(String text);
}
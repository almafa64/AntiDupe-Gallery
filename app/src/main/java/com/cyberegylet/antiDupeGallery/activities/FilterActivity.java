package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.FilterAdapter;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends Activity
{
	private static final String TAG = "FilterActivity";

	private final ActivityManager activityManager = new ActivityManager(this);
	private SQLiteDatabase database;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_activity);

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		List<String> paths = Arrays.asList((String[]) activityManager.getParam("paths"));

		RecyclerView recycler = findViewById(R.id.recycler);
		List<FilteredAlbum> albums = new ArrayList<>();
		FilterAdapter adapter = new FilterAdapter(albums, new FileManager(this));
		recycler.setLayoutManager(new GridLayoutManager(this,
				Config.getIntProperty(Config.Property.ALBUM_COLUMN_NUMBER)
		));
		recycler.setAdapter(adapter);

		database = Cache.getCache();

		new MyAsyncTask()
		{
			@Override
			public void doInBackground()
			{
				try (Cursor cursor = database.rawQuery(
						"select path, HEX(digest), COUNT(digest) from " + Cache.tableDigests + " group by  digest having count(digest) > 1 order by COUNT(digest) desc",
						null
				))
				{
					int pathCol = cursor.getColumnIndex("path");
					int digestCol = cursor.getColumnIndex("HEX(digest)");
					int countCol = cursor.getColumnIndex("COUNT(digest)");

					boolean hasPaths = paths.size() > 0;

					if (!cursor.moveToFirst()) return;
					int count = 0;
					do
					{
						String path = cursor.getString(pathCol);
						File f = new File(path);
						if (!f.canRead() || (hasPaths && !paths.contains(f.getParent()))) continue;

						count++;
						albums.add(new FilteredAlbum(f,
								String.valueOf(count),
								cursor.getInt(countCol),
								cursor.getString(digestCol)
						));
						runOnUiThread(adapter::notifyDataSetChanged);

					} while (cursor.moveToNext());
				}
			}

			@Override
			public void onPostExecute()
			{
				runOnUiThread(() -> Toast.makeText(FilterActivity.this, R.string.filter_complete, Toast.LENGTH_SHORT)
						.show());
			}

			@Override
			public void onPreExecute() { }
		}.execute();
	}
}

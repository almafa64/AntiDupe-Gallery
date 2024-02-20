package com.cyberegylet.antiDupeGallery.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.FilterAdapter;
import com.cyberegylet.antiDupeGallery.backend.Backend;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.helpers.Utils;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		List<String> paths = Arrays.asList((String[]) Objects.requireNonNull(activityManager.getParam("paths")));

		RecyclerView recycler = findViewById(R.id.recycler);
		List<FilteredAlbum> albums = new ArrayList<>();
		FilterAdapter adapter = new FilterAdapter(albums, new FileManager(this));
		recycler.setLayoutManager(new GridLayoutManager(
				this,
				Config.getIntProperty(Config.Property.ALBUM_COLUMN_NUMBER)
		));
		recycler.setAdapter(adapter);

		database = Cache.getCache();

		MyAsyncTask show = new MyAsyncTask()
		{
			int count = 0;

			@Override
			public void doInBackground()
			{
				try (Cursor cursor = database.query(
						Cache.Tables.DIGESTS,
						new String[]{ Cache.Digests.PATH, "HEX(" + Cache.Digests.DIGEST + ")",
								"COUNT(" + Cache.Digests.DIGEST + ")" },
						null,
						null,
						Cache.Digests.DIGEST,
						"COUNT(" + Cache.Digests.DIGEST + ") > 1",
						"COUNT(" + Cache.Digests.DIGEST + ") desc"
				))
				{
					int pathCol = cursor.getColumnIndexOrThrow(Cache.Digests.PATH);
					int digestCol = cursor.getColumnIndexOrThrow("HEX(" + Cache.Digests.DIGEST + ")");
					int countCol = cursor.getColumnIndexOrThrow("COUNT(" + Cache.Digests.DIGEST + ")");

					boolean hasPaths = paths.size() > 0;

					if (!cursor.moveToFirst()) return;
					do
					{
						String path = cursor.getString(pathCol);
						File f = new File(path);
						if (!f.canRead() || (hasPaths && !paths.contains(f.getParent()))) continue;

						String hex = cursor.getString(digestCol);

						FilteredAlbum a = albums.stream().filter(album -> album.getDigestHex().equals(hex)).findAny()
								.orElse(null);

						if (a != null)
						{
							a.setData(null, null, cursor.getInt(countCol), null);
						}
						else
						{
							count++;
							albums.add(new FilteredAlbum(f, "group " + count, cursor.getInt(countCol), hex));
							albums.sort((b, c) -> Math.toIntExact(c.getCount() - b.getCount()));
						}
						runOnUiThread(adapter::notifyDataSetChanged);

					} while (cursor.moveToNext());
				}
			}

			@Override
			public void onPostExecute() { }

			@Override
			public void onPreExecute() { }
		};

		new MyAsyncTask()
		{
			long maxFiles;

			@Override
			public void onPreExecute()
			{
				try (Cursor cursor = database.query(
						Cache.Tables.MEDIA,
						new String[]{ Cache.Media.PATH, Cache.Media.ID },
						null,
						null,
						null,
						null,
						null
				))
				{
					if (!cursor.moveToFirst())
					{
						stop();
						return;
					}

					int pathCol = cursor.getColumnIndexOrThrow(Cache.Media.PATH);
					int idCol = cursor.getColumnIndexOrThrow(Cache.Media.ID);
					maxFiles = cursor.getCount();

					int count = 0;

					do
					{
						Backend.queueFile(cursor.getLong(idCol), cursor.getString(pathCol));
						count++;
					} while (cursor.moveToNext());
				}
			}

			@Override
			public void doInBackground()
			{
				long old = Backend.getQueuedFileProgress();
				while (true)
				{
					long files = Backend.getQueuedFileProgress();
					if (files == 0) break;
					if (old - files >= 1)
					{
						old = files;
						double percentage = 100 - (double) files / maxFiles * 100;
						runOnUiThread(() -> Toast.makeText(
								FilterActivity.this,
								Utils.doubleString(percentage, 2) + "%",
								Toast.LENGTH_SHORT
						).show());
						if (!show.running()) show.execute();
					}
				}
			}

			@Override
			public void onPostExecute()
			{
				runOnUiThread(() -> Toast.makeText(FilterActivity.this, R.string.filter_complete, Toast.LENGTH_SHORT)
						.show());
				try
				{
					Objects.requireNonNull(show.getThread()).join();
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
				show.execute();
			}
		}.execute();
	}
}

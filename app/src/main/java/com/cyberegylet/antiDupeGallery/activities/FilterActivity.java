package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.ImagesAdapter;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

		if (paths.size() == 0) return;

		database = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(ImageListBaseActivity.DATABASE_NAME), null);
		LinearLayout layout = findViewById(R.id.filter_content);

		new MyAsyncTask()
		{

			@Override
			public void doInBackground()
			{
				StringBuilder likes = new StringBuilder();
				for (int i = paths.size() - 1; i >= 0; i--)
				{
					likes.append("path like ?");
					if (i != 0) likes.append(" or ");
				}

				Cursor cursor = database.query(ImageListBaseActivity.tableDigests,
						new String[]{ "path", "digest" },
						likes.toString(),
						paths.stream().map(e -> e + "/%").toArray(String[]::new),
						null,
						null,
						"digest"
				);


				List<File> gotPaths = new ArrayList<>();
				byte[] lastDigest = new byte[0];
				int count = 0;

				if (cursor.moveToFirst())
				{
					int pathCol = cursor.getColumnIndex("path");
					int digestCol = cursor.getColumnIndex("digest");

					do
					{
						String path = cursor.getString(pathCol);
						File f = new File(path);
						if (!f.canRead() || !paths.contains(f.getParent())) continue;
						byte[] digest = cursor.getBlob(digestCol);
						if (!Arrays.equals(lastDigest, digest))
						{
							if (gotPaths.size() >= 2)
							{
								List<ImageFile> files = gotPaths.stream().map(ImageFile::new)
										.collect(Collectors.toList());
								count++;
								int finalCount = count;
								runOnUiThread(() -> {
									TextView textField = new TextView(FilterActivity.this);
									textField.setText(String.valueOf(finalCount));
									layout.addView(textField);

									RecyclerView recycler = new RecyclerView(FilterActivity.this);
									recycler.setLayoutManager(new GridLayoutManager(
											FilterActivity.this,
											Config.getIntProperty(Config.Property.IMAGE_COLUMN_NUMBER)
									));
									recycler.setAdapter(new ImagesAdapter(files, new FileManager(FilterActivity.this)));
									layout.addView(recycler);
								});
							}
							lastDigest = digest;
							gotPaths.clear();
						}
						gotPaths.add(f);
					} while (cursor.moveToNext());
				}
				cursor.close();
			}

			@Override
			public void onPostExecute() { }

			@Override
			public void onPreExecute() { }
		}.execute();
	}
}

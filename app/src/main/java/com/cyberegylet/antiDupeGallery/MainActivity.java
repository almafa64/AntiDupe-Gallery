package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.adapters.FolderAdapter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.backend.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView recyclerView;
	ArrayList<ImageFile> images = new ArrayList<>();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		recyclerView = findViewById(R.id.recycle);
		findViewById(R.id.downBut).setOnClickListener(v -> recyclerView.scrollToPosition(images.size() - 1));
		findViewById(R.id.upBut).setOnClickListener(v -> recyclerView.scrollToPosition(0));

		findViewById(R.id.more_button).setOnClickListener(v -> {
			Toast.makeText(this, "too bad", Toast.LENGTH_SHORT).show();
			/*PopupMenu popup = new PopupMenu(this, v);
			popup.getMenuInflater().inflate(R.menu., popup.getMenu()).show();*/
			// TODO make popup
		});

		fileManager = new FileManager(this);
		if (fileManager.hasReadAccess())
		{
			fileThings();
		}
	}

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
			Log.i("Main", "didn't get storage permissions, quitting");
			finishAndRemoveTask();
		}
	}

	private void fileThings()
	{
		HashMap<String, Object[]> folderNames = new HashMap<>();

		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				int id = getID();
				//if (path.contains("/.")) return; // check if file is in empty directory
				int lastThing = path.lastIndexOf('/');

				if (lastThing == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				int secondLastThing = path.lastIndexOf('/', lastThing - 1);

				String folderAbs = path.substring(0, lastThing);
				Object[] tmp = folderNames.get(folderAbs);
				if (tmp != null)
				{
					tmp[1] = (Integer) tmp[1] + 1;
					return;
				}

				folderNames.put(folderAbs,
						new Object[]{ fileManager.stringToUri(path), 1, id, path.substring(secondLastThing + 1, lastThing) }
				);
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoLoop(sort, wrapper, MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA);

		Comparator<Object[]> comparator = Comparator.comparing((Object[] a) -> ((String) a[3]));
		folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).forEach(m -> {
			Object[] v = m.getValue();
			images.add(new ImageFile((Uri) v[0], (Integer) v[2], (Integer) v[1], (String) v[3]));
		});

		recyclerView.setAdapter(new FolderAdapter(
				images,
				fileManager,
				item -> ActivityManager.switchActivity(this,
						FolderViewActivity.class,
						new ActivityManager.Parameter(
								"currentFolder",
								Objects.requireNonNull(new File(Objects.requireNonNull(item.uri.getPath())).getParentFile())
										.getAbsolutePath()
						)
				)
		));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
	}
}

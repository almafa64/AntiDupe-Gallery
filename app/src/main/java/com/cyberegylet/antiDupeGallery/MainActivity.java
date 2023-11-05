package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.adapters.FolderAdapter;
import com.cyberegylet.antiDupeGallery.models.ImageFolder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView folders;
	ArrayList<ImageFile> images = new ArrayList<>();

	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		folders = findViewById(R.id.items);
		findViewById(R.id.downBut).setOnClickListener(v -> folders.scrollToPosition(images.size() - 1));
		findViewById(R.id.upBut).setOnClickListener(v -> folders.scrollToPosition(0));

		findViewById(R.id.more_button).setOnClickListener(v -> {
			PopupMenu popup = new PopupMenu(this, v);
			popup.setOnMenuItemClickListener(item -> {
				int id = item.getItemId();
				if (id == R.id.settings)
				{
					activityManager.switchActivity(SettingsActivity.class);
				}
				else if (id == R.id.about)
				{
					activityManager.switchActivity(AboutActivity.class);
				}
				else return false;
				return true;
			});
			popup.inflate(R.menu.main_popup_menu);
			popup.show();
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
		HashMap<String, ImageFolder> folderNames = new HashMap<>();

		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				int id = getID();
				//if (path.contains("/.")) return; // check if file is in empty directory
				int lastSeparator = path.lastIndexOf('/');

				if (lastSeparator == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				int secondLastSeparator = path.lastIndexOf('/', lastSeparator - 1);

				String folderAbs = path.substring(0, lastSeparator);
				ImageFolder folder = folderNames.get(folderAbs);
				if (folder != null)
				{
					folder.incrementFileCount();
					return;
				}

				String basename = path.substring(secondLastSeparator + 1, lastSeparator);
				folderNames.put(folderAbs, new ImageFolder(fileManager.stringToUri(path), 1, id, basename));
			}
		};

		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoLoop(sort, wrapper, MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA);

		Comparator<ImageFolder> comparator = Comparator.comparing(ImageFolder::getBasename);
		folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).forEach(entry -> images.add(entry.getValue()));

		folders.setAdapter(new FolderAdapter(images, fileManager));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
	}
}

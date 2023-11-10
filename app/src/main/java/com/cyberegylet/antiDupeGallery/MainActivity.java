package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.adapters.FolderAdapter;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView recycler;

	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		recycler = findViewById(R.id.items);

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
			Log.e("Main", "didn't get storage permissions, quitting");
			finishAndRemoveTask();
		}
	}

	private void fileThings()
	{
		HashMap<String, Folder> folderNames = new HashMap<>();

		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				//if (path.contains("/.")) return; // check if file is in hidden directory
				int lastSeparator = path.lastIndexOf('/');

				if (lastSeparator == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				String folderAbs = path.substring(0, lastSeparator);

				Folder folder = folderNames.get(folderAbs);
				ImageFile image = new ImageFile(FileManager.stringToUri(path));
				if (folder != null)
				{
					folder.images.add(image);
					return;
				}

				if (!new File(path).canRead()) return;

				folder = new Folder(FileManager.stringToUri(folderAbs));
				folder.images.add(image);
				folderNames.put(folderAbs, folder);
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoLoop(sort, wrapper, MediaStore.MediaColumns.DATA);

		Comparator<Folder> comparator = Comparator.comparing(Folder::getName);
		List<Folder> folders = folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).map(Map.Entry::getValue)
				.collect(Collectors.toList());
		List<Folder> foldersCopy = new ArrayList<>(folders);

		recycler.setAdapter(new FolderAdapter(folders, fileManager));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
		SearchView search = findViewById(R.id.search_bar);

		search.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query) { return false; }

			@Override
			public boolean onQueryTextChange(String text)
			{
				((FolderAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					foldersCopy.forEach(folder -> {
						List<ImageFile> images = new ArrayList<>();
						folder.images.forEach(image -> {
							if (!image.getBasename().contains(text)) return;
							images.add(image);
						});
						if (images.size() == 0) return;
						Folder f = new Folder(folder);
						dirs.add(f);
						f.images.addAll(images);
					});
				});
				return true;
			}
		});
	}
}

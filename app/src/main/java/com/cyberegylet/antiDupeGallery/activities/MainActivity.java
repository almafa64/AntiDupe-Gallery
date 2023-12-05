package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.FolderAdapter;
import com.cyberegylet.antiDupeGallery.backend.ConfigManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

		ConfigManager.init(this);
		if (ConfigManager.getConfig(ConfigManager.Config.PIN_LOCk).length() != 0 && ActivityManager.getParam(this, "login") == null)
		{
			ActivityManager.switchActivity(this, PinActivity.class);
			return;
		}

		setContentView(R.layout.main_activity);

		recycler = findViewById(R.id.items);
		int span = ConfigManager.getIntConfig(ConfigManager.Config.FOLDER_COLUMN_NUMBER);
		recycler.setLayoutManager(new GridLayoutManager(this, span));

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
		if (fileManager.hasFileAccess())
		{
			fileThings();
		}
	}

	//ToDo make base Activity for merging codes
	@Override
	protected void onStop()
	{
		ConfigManager.saveConfigs();
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		ConfigManager.saveConfigs();
		super.onDestroy();
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
			Toast.makeText(this, getString(R.string.no_storage_permission), Toast.LENGTH_SHORT).show();
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

				if (!new File(path).canRead()) return;

				String folderAbs = path.substring(0, path.lastIndexOf('/'));

				Folder folder = folderNames.get(folderAbs);
				if (folder == null)
				{
					folder = new Folder(FileManager.stringToUri(folderAbs));
					folderNames.put(folderAbs, folder);
				}
				ImageFile image = new ImageFile(FileManager.stringToUri(path));
				folder.images.add(image);
			}
		};
		String image_sort = ConfigSort.toSQLString(ConfigManager.getConfig(ConfigManager.Config.IMAGE_SORT));
		fileManager.allImageAndVideoLoop(image_sort, wrapper, MediaStore.MediaColumns.DATA);

		String folder_sort_data = ConfigManager.getConfig(ConfigManager.Config.FOLDER_SORT);
		Comparator<Folder> comparator;
		switch (ConfigSort.getSortType(folder_sort_data))
		{
			case MODIFICATION_DATE:
				comparator = Comparator.comparing(Folder::getModifiedDate);
				break;
			case CREATION_DATE:
				comparator = Comparator.comparing(Folder::getCreationDate);
				break;
			case SIZE:
				comparator = Comparator.comparing(Folder::getSize);
				break;
			default:
				comparator = Comparator.comparing(Folder::getName);
				break;
		}
		if (!ConfigSort.isAscending(folder_sort_data)) comparator = comparator.reversed();

		List<Folder> folders = folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).map(Map.Entry::getValue)
				.collect(Collectors.toList());
		List<Folder> foldersCopy = folders.stream()
				.filter(folder -> !folder.isHidden() || ConfigManager.getBooleanConfig(ConfigManager.Config.SHOW_HIDDEN))
				.collect(Collectors.toList());

		ConfigManager.addListener((c, v) -> {
			if (c == ConfigManager.Config.SHOW_HIDDEN)
			{
				boolean showHidden = Objects.equals(v, "1");
				((FolderAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					dirs.addAll(folders.stream().filter(folder -> !folder.isHidden() || showHidden).collect(Collectors.toList()));
				});
			}
		});

		recycler.setAdapter(new FolderAdapter(foldersCopy, fileManager));

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
				String text2 = text.toLowerCase(Locale.ROOT);
				((FolderAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					folders.forEach(folder -> {
						/*List<ImageFile> images = new ArrayList<>();
						folder.images.forEach(image -> {
							if (!image.getBasename().contains(text)) return;
							images.add(image);
						});
						if (images.size() == 0) return;
						Folder f = new Folder(folder);
						dirs.add(f);
						f.images.addAll(images);*/
						if (folder.getName().toLowerCase(Locale.ROOT).contains(text2)) dirs.add(new Folder(folder, true));
					});
				});
				return true;
			}
		});
	}
}

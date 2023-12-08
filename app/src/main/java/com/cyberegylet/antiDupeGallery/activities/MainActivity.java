package com.cyberegylet.antiDupeGallery.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.adapters.FolderAdapter;
import com.cyberegylet.antiDupeGallery.adapters.FolderAdapterAsync;
import com.cyberegylet.antiDupeGallery.backend.Backend;
import com.cyberegylet.antiDupeGallery.backend.ConfigManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import kotlin.io.encoding.Base64Kt;

public class MainActivity extends Activity
{
	private static final int MOVE_FOLDER_SELECT_ID = 1;
	private static final int COPY_FOLDER_SELECT_ID = 2;

	private FileManager fileManager;
	private RecyclerView recycler;
	private final ActivityManager activityManager = new ActivityManager(this);
	private List<Folder> folders;
	private FolderAdapterAsync.MySortedSet<Folder> foldersCopy;
	private FolderAdapterAsync.MySortedSet<Folder> folders2;

	private SQLiteDatabase database;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.database = SQLiteDatabase.openOrCreateDatabase(getDatabasePath("data.db"), null);
		Backend.init(this);

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
			final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
			final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
			PopupMenu popup = new PopupMenu(this, v);
			popup.inflate(R.menu.main_popup_menu);
			final int moveId;
			final int copyId;
			final int deleteId;
			if (selected.size() != 0)
			{
				moveId = View.generateViewId();
				copyId = View.generateViewId();
				deleteId = View.generateViewId();
				Menu menu = popup.getMenu();
				menu.add(Menu.NONE, moveId, Menu.NONE, R.string.popup_move);
				menu.add(Menu.NONE, copyId, Menu.NONE, R.string.popup_copy);
				menu.add(Menu.NONE, deleteId, Menu.NONE, R.string.popup_delete);
			}
			else deleteId = copyId = moveId = -1;
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
				else if (id == moveId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, MOVE_FOLDER_SELECT_ID);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, COPY_FOLDER_SELECT_ID);
				}
				else if (id == deleteId)
				{
					List<String> failedFolders = new ArrayList<>();
					for (BaseImageAdapter.ViewHolder tmp : selected)
					{
						FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
						Path p = Paths.get(holder.folder.getPath().getPath());
						if (!fileManager.deleteFolder(p)) failedFolders.add(holder.folder.getName());
					}
					if (failedFolders.size() == 0)
					{
						Toast.makeText(this, R.string.popup_delete_success, Toast.LENGTH_SHORT).show();
					}
					else
					{
						// ToDo error dialog
					}
				}
				else return false;
				return true;
			});
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
	protected void onResume()
	{
		super.onResume();
		boolean showHidden = ConfigManager.getBooleanConfig(ConfigManager.Config.SHOW_HIDDEN);
		/*((FolderAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
			dirs.clear();
			dirs.addAll(folders.stream().filter(folder -> !folder.isHidden() || showHidden).collect(Collectors.toList()));
		});*/
		if (recycler == null || recycler.getAdapter() == null) return;
		if (recycler == null || recycler.getAdapter() == null) return;
		((FolderAdapterAsync) recycler.getAdapter()).filter(dirs -> {
			dirs.clear();
			dirs.addAll(folders2.stream().filter(folder -> !folder.isHidden() || showHidden).collect(Collectors.toList()));
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK || data == null) return;
		final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
		final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
		Path path = Paths.get("/storage/emulated/0/" + data.getData().getPath().split(":")[1]); // ToDo this is very hacky
		List<String> failedFolders = new ArrayList<>();
		switch (requestCode)
		{
			case MOVE_FOLDER_SELECT_ID:
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
					Path p = Paths.get(holder.folder.getPath().getPath());
					if (!fileManager.moveFolder(p, path)) failedFolders.add(holder.folder.getName());
				}
				break;
			case COPY_FOLDER_SELECT_ID:
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
					Path p = Paths.get(holder.folder.getPath().getPath());
					if (!fileManager.copyFolder(p, path)) failedFolders.add(holder.folder.getName());
				}
				break;
		}
		if (failedFolders.size() == 0)
		{
			Toast.makeText(
					this,
					(requestCode == MOVE_FOLDER_SELECT_ID) ? R.string.popup_move_success : R.string.popup_copy_success,
					Toast.LENGTH_SHORT
			).show();
		}
		else
		{
			// ToDo error dialog
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
			Toast.makeText(this, getString(R.string.no_storage_permission), Toast.LENGTH_SHORT).show();
			finishAndRemoveTask();
		}
	}

	@SuppressLint("StaticFieldLeak")
	private void fileThings()
	{
		boolean inWork = true;

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
		folders2 = new FolderAdapterAsync.MySortedSet<>(comparator);
		foldersCopy = new FolderAdapterAsync.MySortedSet<>(comparator);

		FolderAdapterAsync adapter = new FolderAdapterAsync(foldersCopy, fileManager);
		recycler.setAdapter(adapter);

		new AsyncTask<Void, Void, Void>()
		{
			private long timeStart = 0;

			@Override
			protected void onPreExecute()
			{
				timeStart = System.nanoTime();
			}

			@Override
			protected Void doInBackground(Void... voids)
			{
				HashMap<String, Folder> folderNames = new HashMap<>();
				final int[] timeout = { 0 };
				FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
				{
					@Override
					public void run()
					{
						String path = getPath();

						if (!new File(path).canRead()) return;

						final String folderAbs = path.substring(0, path.lastIndexOf('/'));

						Folder folder = folderNames.get(folderAbs);
						if (folder == null)
						{
							folder = new Folder(FileManager.stringToUri(folderAbs));
							folderNames.put(folderAbs, folder);
							folders2.add(folder);
							if (!folder.isHidden() || ConfigManager.getBooleanConfig(ConfigManager.Config.SHOW_HIDDEN))
								foldersCopy.add(folder);
						}
						ImageFile image = new ImageFile(FileManager.stringToUri(path));

						long id = getID();

						Backend.queueFile(id, path);

						folder.images.add(image);

						if (timeout[0]++ == 20)
						{
							timeout[0] = 0;
							runOnUiThread(adapter::notifyDataSetChanged);
						}
					}
				};
				String image_sort = ConfigSort.toSQLString(ConfigManager.getConfig(ConfigManager.Config.IMAGE_SORT));
				fileManager.allImageAndVideoLoop(
						image_sort,
						wrapper,
						MediaStore.MediaColumns._ID,
						MediaStore.MediaColumns.DATA,
						MediaStore.MediaColumns.MIME_TYPE
				);
				return null;
			}

			@Override
			protected void onPostExecute(Void unused)
			{
				super.onPostExecute(unused);
				long time = System.nanoTime() - this.timeStart;
				Log.i("MainActivity", "media iteration took " + time + " ns");
				runOnUiThread(adapter::notifyDataSetChanged);
			}
		}.execute();

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
				((FolderAdapterAsync) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					for (Folder folder : folders2)
					{/*List<ImageFile> images = new ArrayList<>();
						folder.images.forEach(image -> {
							if (!image.getBasename().contains(text)) return;
							images.add(image);
						});
						if (images.size() == 0) return;
						Folder f = new Folder(folder);
						dirs.add(f);
						f.images.addAll(images);*/
						if (folder.getName().toLowerCase(Locale.ROOT).contains(text2)) dirs.add(new Folder(folder, true));
					}
				});
				return true;
			}
		});
	}

	private void fileThings2()
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

		folders = folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).map(Map.Entry::getValue)
				.collect(Collectors.toList());
		List<Folder> foldersCopy = folders.stream()
				.filter(folder -> !folder.isHidden() || ConfigManager.getBooleanConfig(ConfigManager.Config.SHOW_HIDDEN))
				.collect(Collectors.toList());

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

	private String getDbPath(String name)
	{
		return getDatabasePath(name).getAbsolutePath();
	}
}
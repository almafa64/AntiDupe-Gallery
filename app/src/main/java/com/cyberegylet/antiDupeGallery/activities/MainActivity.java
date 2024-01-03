package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.adapters.FolderAdapterAsync;
import com.cyberegylet.antiDupeGallery.backend.Backend;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.helpers.Utils;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends Activity
{
	private static final String TAG = "MainActivity";
	private static final String DATABASE_NAME = "data.db";

	private static final int MOVE_SELECTED_FOLDERS = 1;
	private static final int COPY_SELECTED_FOLDERS = 2;
	private static final int DELETE_SELECTED_FOLDERS = 3;

	private FileManager fileManager;
	private RecyclerView recycler;
	private final ActivityManager activityManager = new ActivityManager(this);
	private FolderAdapterAsync.MySortedSet<Folder> foldersCopy;
	private FolderAdapterAsync.MySortedSet<Folder> folders2;
	private SearchView search;

	public static SQLiteDatabase database;

	private static boolean hasBackendBeenCalled = false;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Config.init(this);

		if (Config.getStringProperty(Config.Property.PIN_LOCK).length() != 0 && ActivityManager.getParam(this,
				"login"
		) == null)
		{
			ActivityManager.switchActivity(this, PinActivity.class);
			return;
		}

		database = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(DATABASE_NAME), null);
		if (!hasBackendBeenCalled) Backend.init(this);
		hasBackendBeenCalled = true;

		setContentView(R.layout.main_activity);

		recycler = findViewById(R.id.items);
		int span = Config.getIntProperty(Config.Property.FOLDER_COLUMN_NUMBER);
		recycler.setLayoutManager(new GridLayoutManager(this, span));
		search = findViewById(R.id.search_bar);

		findViewById(R.id.more_button).setOnClickListener(v -> {
			final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
			final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
			PopupMenu popup = new PopupMenu(this, v);
			popup.inflate(R.menu.main_popup_menu);
			final int moveId;
			final int copyId;
			final int deleteId;
			final int infoId;
			if (selected.size() != 0)
			{
				moveId = View.generateViewId();
				copyId = View.generateViewId();
				deleteId = View.generateViewId();
				infoId = View.generateViewId();
				Menu menu = popup.getMenu();
				menu.add(Menu.NONE, moveId, Menu.NONE, R.string.popup_move);
				menu.add(Menu.NONE, copyId, Menu.NONE, R.string.popup_copy);
				menu.add(Menu.NONE, deleteId, Menu.NONE, R.string.popup_delete);
				menu.add(Menu.NONE, infoId, Menu.NONE, R.string.popup_info);
			}
			else deleteId = copyId = moveId = infoId = -1;
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
					startActivityForResult(intent, MOVE_SELECTED_FOLDERS);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, COPY_SELECTED_FOLDERS);
				}
				else if (id == deleteId)
				{
					onActivityResult(DELETE_SELECTED_FOLDERS, RESULT_OK, new Intent());
				}
				else if (id == infoId)
				{
					ViewGroup popupInfo = (ViewGroup) activityManager.makePopupWindow(R.layout.dialog_info)
							.getContentView();
					TextView name = popupInfo.findViewById(R.id.info_name);
					TextView count = popupInfo.findViewById(R.id.info_count);
					TextView path = popupInfo.findViewById(R.id.info_path);
					TextView size = popupInfo.findViewById(R.id.info_size);
					if (selected.size() == 1)
					{
						File f = ((FolderAdapterAsync.ViewHolder) selected.get(0)).getFolder().getFile();
						path.setText(f.getParent());
						name.setText(f.getName());
					}
					else
					{
						path.setVisibility(View.GONE);
						popupInfo.getChildAt(4).setVisibility(View.GONE);

						((TextView) popupInfo.getChildAt(0)).setText(R.string.popup_items_selected);
						name.setText(String.valueOf(selected.size()));
					}

					long sizeB = 0;
					long imageCount = 0;
					for (BaseImageAdapter.ViewHolder holder : selected)
					{
						Folder folder = ((FolderAdapterAsync.ViewHolder) holder).getFolder();
						sizeB += folder.getSize();
						imageCount += folder.images.size();
					}

					size.setText(Utils.getByteStringFromSize(sizeB));
					count.setText(String.valueOf(imageCount));
				}
				else return false;
				return true;
			});
			popup.show();
		});

		fileManager = new FileManager(this);
		if (fileManager.hasFileAccess()) fileThings();
	}

	//ToDo make base Activity for merging codes
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK || data == null) return;
		final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
		final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
		// ToDo test on sd card
		Log.d("app", String.valueOf(data));
		Path path = null;
		if (requestCode != DELETE_SELECTED_FOLDERS)
		{
			path = Paths.get("/storage/emulated/0/" + data.getData().getPath().split(":")[1]);
		}
		List<Folder> failedFolders = new ArrayList<>();
		int textId = 0;
		switch (requestCode)
		{
			case MOVE_SELECTED_FOLDERS:
				textId = R.string.popup_move_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
					Path p = Paths.get(holder.getFolder().getPath());
					if (!fileManager.moveFolder(p, path)) failedFolders.add(holder.getFolder());
				}
				break;
			case COPY_SELECTED_FOLDERS:
				textId = R.string.popup_copy_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
					Path p = Paths.get(holder.getFolder().getPath());
					if (!fileManager.copyFolder(p, path)) failedFolders.add(holder.getFolder());
				}
				break;
			case DELETE_SELECTED_FOLDERS:
				textId = R.string.popup_delete_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FolderAdapterAsync.ViewHolder holder = (FolderAdapterAsync.ViewHolder) tmp;
					Path p = Paths.get(holder.getFolder().getPath());
					if (!fileManager.deleteFolder(p)) failedFolders.add(holder.getFolder());
				}
				break;
		}
		if (failedFolders.size() == 0) Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
		else
		{
			ScrollView scroll = activityManager.makePopupWindow(R.layout.dialog_scroll).getContentView()
					.findViewById(R.id.dialog_scroll);
			for(Folder f : failedFolders)
			{
				TextView textView = new TextView(this);
				textView.setText(f.getPath());
				scroll.addView(textView);
			}
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

	private void fileThings()
	{
		boolean inWork = true;

		String folder_sort_data = Config.getStringProperty(Config.Property.FOLDER_SORT);
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
				comparator = Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT));
				break;
		}
		if (!ConfigSort.isAscending(folder_sort_data)) comparator = comparator.reversed();
		folders2 = new FolderAdapterAsync.MySortedSet<>(comparator);
		foldersCopy = new FolderAdapterAsync.MySortedSet<>(comparator);

		FolderAdapterAsync adapter = new FolderAdapterAsync(foldersCopy, fileManager);
		recycler.setAdapter(adapter);

		new MyAsyncTask()
		{
			private long timeStart = 0;

			@Override
			public void onPreExecute() { timeStart = System.currentTimeMillis(); }

			@Override
			public void doInBackground()
			{
				HashMap<String, Folder> folderNames = new HashMap<>();
				final int[] timeout = { 0 };
				FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
				{
					@Override
					public void run()
					{
						String path = getPath();

						File image = new File(path);
						if (!image.canRead()) return;

						final String folderAbs = path.substring(0, path.lastIndexOf('/'));

						Folder folder = folderNames.get(folderAbs);
						if (folder == null)
						{
							folder = new Folder(folderAbs);
							folderNames.put(folderAbs, folder);
							folders2.add(folder);
							if (!folder.isHidden() || Config.getBooleanProperty(Config.Property.SHOW_HIDDEN))
								foldersCopy.add(folder);
						}
						ImageFile imageFile = new ImageFile(image);

						long id = getID();
						Backend.queueFile(id, path);

						folder.addImage(imageFile);

						if (timeout[0]++ == 20)
						{
							timeout[0] = 0;
							runOnUiThread(adapter::notifyDataSetChanged);
						}
					}
				};
				String image_sort = ConfigSort.toSQLString(Config.getStringProperty(Config.Property.IMAGE_SORT));
				fileManager.allImageAndVideoLoop(image_sort,
						wrapper,
						MediaStore.MediaColumns._ID,
						MediaStore.MediaColumns.DATA
				);
			}

			@Override
			public void onPostExecute()
			{
				long time = System.currentTimeMillis() - this.timeStart;
				Log.i(TAG, "media iteration took " + time + " ms");
				runOnUiThread(adapter::notifyDataSetChanged);
			}
		}.execute();

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);

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
	}

	private String getDbPath(String name) { return getDatabasePath(name).getAbsolutePath(); }

	private void filterRecycle(String text)
	{
		String text2 = text.toLowerCase(Locale.ROOT);
		boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
		((FolderAdapterAsync) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
			dirs.clear();
			for (Folder folder : folders2)
			{
				if ((showHidden || !folder.isHidden()) && folder.getName().toLowerCase(Locale.ROOT).contains(text2))
				{
					dirs.add(new Folder(folder, true));
				}
			}
		});
	}
}
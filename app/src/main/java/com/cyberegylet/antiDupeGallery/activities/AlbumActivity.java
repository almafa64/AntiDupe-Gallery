package com.cyberegylet.antiDupeGallery.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.AlbumAdapter;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.backend.Backend;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.helpers.Utils;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AlbumActivity extends ImageListBaseActivity
{
	private static final int MOVE_SELECTED_ALBUMS = 1;
	private static final int COPY_SELECTED_ALBUMS = 2;
	private static final int DELETE_SELECTED_ALBUMS = 3;

	private List<Album> allAlbums;

	private static boolean hasBackendBeenCalled = false;

	public AlbumActivity() { super("AlbumActivity"); }

	@Override
	protected boolean myOnCreate(@Nullable Bundle savedInstanceState)
	{
		Config.init(this);

		if (Config.getStringProperty(Config.Property.PIN_LOCK).length() != 0 && ActivityManager.getParam(
				this,
				"login"
		) == null)
		{
			ActivityManager.switchActivity(this, PinActivity.class);
			return false;
		}

		if (!hasBackendBeenCalled) Backend.init(this);
		hasBackendBeenCalled = true;

		setContentView(R.layout.album_activity);
		contentSet();

		findViewById(R.id.more_button).setOnClickListener(v -> {
			final BaseImageAdapter adapter = (BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter());
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
				if (id == R.id.menu_settings)
				{
					activityManager.switchActivity(SettingsActivity.class);
				}
				else if (id == R.id.menu_about)
				{
					activityManager.switchActivity(AboutActivity.class);
				}
				else if (id == R.id.menu_filter)
				{
					List<String> paths = new ArrayList<>();
					for (BaseImageAdapter.ViewHolder holder : selected)
					{
						Album folder = ((AlbumAdapter.ViewHolder) holder).getAlbum();
						paths.add(folder.getPath());
					}
					activityManager.switchActivity(
							FilterActivity.class,
							new ActivityParameter<>("paths", paths.toArray(new String[0]))
					);
				}
				else if (id == moveId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, MOVE_SELECTED_ALBUMS);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, COPY_SELECTED_ALBUMS);
				}
				else if (id == deleteId)
				{
					new AlertDialog.Builder(this).setTitle(R.string.popup_delete)
							.setMessage(R.string.popup_delete_confirm).setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(
									android.R.string.ok,
									(dialog, whichButton) -> onActivityResult(
											DELETE_SELECTED_ALBUMS,
											RESULT_OK,
											new Intent()
									)
							).setNegativeButton(android.R.string.cancel, null).show();
				}
				else if (id == infoId)
				{
					ViewGroup popupInfo = (ViewGroup) activityManager.makePopupWindow(R.layout.dialog_info)
							.getContentView();
					TextView nameField = popupInfo.findViewById(R.id.info_name);
					TextView countField = popupInfo.findViewById(R.id.info_count);
					TextView pathField = popupInfo.findViewById(R.id.info_path);
					TextView sizeField = popupInfo.findViewById(R.id.info_size);
					TextView modDateField = popupInfo.findViewById(R.id.info_mdate);
					if (selected.size() == 1)
					{
						Album album = ((AlbumAdapter.ViewHolder) selected.get(0)).getAlbum();
						pathField.setText(album.getFile().getParent());
						nameField.setText(album.getName());
						modDateField.setText(Utils.msToDate(album.getModifiedDate()));
					}
					else
					{
						modDateField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_mdate_header).setVisibility(View.GONE);
						pathField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_path_header).setVisibility(View.GONE);

						((TextView) popupInfo.findViewById(R.id.info_name_header)).setText(R.string.popup_items_selected);
						nameField.setText(String.valueOf(selected.size()));
					}

					popupInfo.findViewById(R.id.info_cdate).setVisibility(View.GONE);
					popupInfo.findViewById(R.id.info_cdate_header).setVisibility(View.GONE);

					long sizeB = 0;
					long imageCount = 0;
					for (BaseImageAdapter.ViewHolder holder : selected)
					{
						Album folder = ((AlbumAdapter.ViewHolder) holder).getAlbum();
						sizeB += folder.getSize();
						imageCount += folder.getCount();
					}

					sizeField.setText(Utils.getByteStringFromSize(sizeB));
					countField.setText(String.valueOf(imageCount));
				}
				else return false;
				return true;
			});
			popup.show();
		});
		return true;
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
		if (requestCode != DELETE_SELECTED_ALBUMS)
		{
			path = Paths.get("/storage/emulated/0/" + data.getData().getPath().split(":")[1]);
		}
		List<Album> failedAlbums = new ArrayList<>();
		int textId = 0;
		switch (requestCode)
		{
			case MOVE_SELECTED_ALBUMS ->
			{
				textId = R.string.popup_move_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					AlbumAdapter.ViewHolder holder = (AlbumAdapter.ViewHolder) tmp;
					Album album = holder.getAlbum();
					Path p = Paths.get(album.getPath());
					if (!fileManager.moveAlbum(p, path))
					{
						failedAlbums.add(album);
						continue;
					}
					album.setFile(path.toFile());
					Cache.updateAlbum(album, p.toString());
				}
			}
			case COPY_SELECTED_ALBUMS ->
			{
				textId = R.string.popup_copy_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					AlbumAdapter.ViewHolder holder = (AlbumAdapter.ViewHolder) tmp;
					Album album = holder.getAlbum();
					Path p = Paths.get(holder.getAlbum().getPath());
					if (!fileManager.copyAlbum(p, path))
					{
						failedAlbums.add(album);
						continue;
					}
					Album newAlbum = new Album(path.toFile());
					Cache.addAlbum(newAlbum);
					allAlbums.add(newAlbum);
				}
			}
			case DELETE_SELECTED_ALBUMS ->
			{
				textId = R.string.popup_delete_folder_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					AlbumAdapter.ViewHolder holder = (AlbumAdapter.ViewHolder) tmp;
					Album album = holder.getAlbum();
					Path p = Paths.get(album.getPath());
					if (!fileManager.deleteAlbum(p))
					{
						failedAlbums.add(album);
						continue;
					}
					Cache.deleteAlbum(album);
					allAlbums.remove(album);
				}
			}
		}
		if (failedAlbums.size() == 0)
		{
			filterRecycle(search.getQuery().toString());
			Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
		}
		else
		{
			ScrollView scroll = activityManager.makePopupWindow(R.layout.dialog_scroll).getContentView()
					.findViewById(R.id.dialog_scroll);
			for (Album f : failedAlbums)
			{
				TextView textView = new TextView(this);
				textView.setText(f.getPath());
				scroll.addView(textView);
			}
		}
	}

	@Override
	protected void storageAccessGranted()
	{
		Comparator<Album> comparator = ConfigSort.getAlbumComparator();
		allAlbums = new ArrayList<>();
		List<Album> albums = new ArrayList<>();

		AlbumAdapter adapter = new AlbumAdapter(albums, fileManager);
		recycler.setAdapter(adapter);

		// ToDo remove deleted files
		new MyAsyncTask()
		{
			private long timeStart = 0;

			@Override
			public void onPreExecute() { timeStart = System.currentTimeMillis(); }

			@Override
			public void doInBackground()
			{
				HashMap<String, Album> albumNames = new HashMap<>();
				FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
				{
					final boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
					int timeout = 0;

					@Override
					public void run()
					{
						String path = getPath();

						File image = new File(path);
						if (!image.canRead()) return;

						final String albumPath = path.substring(0, path.lastIndexOf('/'));

						Album album = albumNames.get(albumPath);
						if (album == null)
						{
							album = new Album(albumPath);
							Cache.addAlbum(album);
							albumNames.put(albumPath, album);
							allAlbums.add(album);
							if (!album.isHidden() || showHidden)
							{
								albums.add(album);
								adapter.sort(comparator, false);
							}
						}

						long id = getId();
						//Backend.queueFile(id, path);

						ImageFile imageFile = new ImageFile(image, getMime(), id);
						album.addImage(imageFile);
						Cache.addMedia(imageFile, album);

						if (timeout++ == 30)
						{
							timeout = 0;
							runOnUiThread(adapter::notifyDataSetChanged);
						}
					}
				};
				String image_sort = ConfigSort.toSQLString(Config.getStringProperty(Config.Property.IMAGE_SORT));
				fileManager.allImageAndVideoLoop(
						image_sort,
						wrapper,
						MediaStore.MediaColumns._ID,
						MediaStore.MediaColumns.DATA,
						MediaStore.MediaColumns.MIME_TYPE
				);
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onPostExecute()
			{
				long time = System.currentTimeMillis() - this.timeStart;
				Log.i(TAG, "media iteration took " + time + " ms");
				runOnUiThread(() -> {
					adapter.notifyDataSetChanged();
					Toast.makeText(AlbumActivity.this, R.string.search_done, Toast.LENGTH_SHORT).show();
				});
			}
		}.execute();
	}

	@Override
	protected void filterRecycle(String text)
	{
		String text2 = text.toLowerCase(Locale.ROOT);
		boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
		AlbumAdapter adapter = (AlbumAdapter) Objects.requireNonNull(recycler.getAdapter());
		adapter.filter(dirs -> {
			dirs.clear();
			for (Album folder : allAlbums)
			{
				if ((showHidden || !folder.isHidden()) && folder.getName().toLowerCase(Locale.ROOT).contains(text2))
				{
					dirs.add(new Album(folder, true));
				}
			}
			dirs.sort(ConfigSort.getAlbumComparator());
		});
	}
}
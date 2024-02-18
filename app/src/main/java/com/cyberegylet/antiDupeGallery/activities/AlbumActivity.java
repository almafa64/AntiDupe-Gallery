package com.cyberegylet.antiDupeGallery.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import androidx.documentfile.provider.DocumentFile;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.AlbumAdapter;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.backend.Backend;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.Mimes;
import com.cyberegylet.antiDupeGallery.compose.AboutActivity;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask;
import com.cyberegylet.antiDupeGallery.helpers.RealPathUtil;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AlbumActivity extends ImageListBaseActivity
{
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
					activityManager.launchIntent(intent, moveLauncher);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					activityManager.launchIntent(intent, copyLauncher);
				}
				else if (id == deleteId)
				{
					new AlertDialog.Builder(this).setTitle(R.string.popup_delete)
							.setMessage(R.string.popup_delete_confirm).setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(
									android.R.string.ok,
									(dialog, whichButton) -> myOnActivityResult(
											DELETE_SELECTED,
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
	protected void myOnActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK || data == null) return;
		final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
		final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
		// ToDo test on sd card
		Log.d("app", String.valueOf(data));
		Path path = null;
		if (requestCode != DELETE_SELECTED)
		{
			Uri uri = data.getData();
			String dataPath = Objects.requireNonNull(uri).getPath();
			String[] parts = Objects.requireNonNull(dataPath).split(":");

			DocumentFile docFile = DocumentFile.fromTreeUri(this, uri);

			//Log.d("app", Objects.requireNonNull(DocumentFile.fromTreeUri(this, uri).getUri().toString()));

			// raw -> parts[1]
			// primary -> /storage/emulated/0/ + parts[1]
			//path = Paths.get("/storage/emulated/0/" + parts[1]);
			if (uri.toString().startsWith("content://com.android.providers.downloads.documents/tree/raw"))
			{
				path = Paths.get(parts[1]);
			}
			else if (dataPath.startsWith("/tree/primary:"))
			{
				path = Paths.get("/storage/emulated/0/" + parts[1]);
			}
			else path = Paths.get(RealPathUtil.getRealPath(this, Objects.requireNonNull(docFile).getUri()));
		}
		List<Album> failedAlbums = new ArrayList<>();
		int textId = 0;
		switch (requestCode)
		{
			case MOVE_SELECTED ->
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
			case COPY_SELECTED ->
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
			case DELETE_SELECTED ->
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

	// ToDo remove deleted files from db
	@Override
	protected void storageAccessGranted()
	{
		Comparator<Album> comparator = ConfigSort.getAlbumComparator();
		allAlbums = new ArrayList<>();
		List<Album> albums = new ArrayList<>();

		AlbumAdapter adapter = new AlbumAdapter(albums, fileManager);
		recycler.setAdapter(adapter);

		new MyAsyncTask()
		{
			private long timeStart = 0;

			@Override
			public void onPreExecute() { timeStart = System.currentTimeMillis(); }

			@Override
			public void doInBackground()
			{
				final boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);

				// https://github.com/SimpleMobileTools/Simple-Gallery/blob/master/app/src/main/kotlin/com/simplemobiletools/gallery/pro/helpers/MediaFetcher.kt#L84

				HashSet<String> folders = new LinkedHashSet<>();
				try (Cursor c = getContentResolver().query(
						FileManager.EXTERNAL_URI,
						new String[]{ MediaStore.MediaColumns.DATA },
						null,
						null,
						MediaStore.MediaColumns._ID + " DESC LIMIT 10"
				))
				{
					assert c != null;
					if (c.moveToFirst())
					{
						do
						{
							String path = FileManager.getParentPath(c.getString(0));
							folders.add(path);
						} while (c.moveToNext());
					}
				}

				List<String> args = new ArrayList<>();
				StringBuilder sb = new StringBuilder();

				for (String a : Mimes.PHOTO_EXTENSIONS)
				{
					sb.append(MediaStore.MediaColumns.DATA + " LIKE ? OR ");
					args.add("%" + a);
				}

				for (String a : Mimes.VIDEO_EXTENSIONS)
				{
					sb.append(MediaStore.MediaColumns.DATA + " LIKE ? OR ");
					args.add("%" + a);
				}

				String selection = sb.toString().trim();
				selection = selection.substring(0, selection.lastIndexOf("OR"));

				try (Cursor c = getContentResolver().query(
						FileManager.EXTERNAL_URI,
						new String[]{ MediaStore.MediaColumns.DATA },
						selection,
						args.toArray(new String[0]),
						null
				))
				{
					assert c != null;
					if (c.moveToFirst())
					{
						do
						{
							String path = c.getString(0);
							if (path == null) continue;
							folders.add(FileManager.getParentPath(path));
						} while (c.moveToNext());
					}
				}

				Cache.getCache().beginTransaction();
				for (String folder : folders)
				{
					File folderFile = new File(folder);
					if (!folderFile.canRead()) continue;
					Album album = new Album(folderFile);

					if (!album.isHidden() || showHidden)
					{
						albums.add(album);
						adapter.sort(comparator, false);
					}

					File[] files = folderFile.listFiles();
					if (files == null) continue;
					int timeout = 0;
					for (File file : files)
					{
						if (!file.canRead()) continue;

						String path = file.getPath();

						boolean isImage = Mimes.isImage(path);
						boolean isVideo = !isImage && Mimes.isVideo(path);

						if (!isImage && !isVideo) continue;
						ImageFile imageFile = new ImageFile(file, isImage ? Mimes.Type.IMAGE : Mimes.Type.VIDEO);

						album.addImage(imageFile);
						Cache.addMedia(imageFile);

						if (timeout++ == 50)
						{
							timeout = 0;
							runOnUiThread(adapter::notifyDataSetChanged);
						}
					}

					if (album.getCount() != 0)
					{
						Cache.addAlbum(album);
						allAlbums.add(album);
					}
					else albums.remove(album);
				}
				Cache.getCache().setTransactionSuccessful();
				Cache.getCache().endTransaction();

				/*HashMap<String, Album> albumNames = new HashMap<>();
				FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
				{
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
				);*/
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
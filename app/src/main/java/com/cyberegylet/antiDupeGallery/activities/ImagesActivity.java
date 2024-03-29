package com.cyberegylet.antiDupeGallery.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.adapters.ImagesAdapter;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.Mimes;
import com.cyberegylet.antiDupeGallery.compose.AboutActivity;
import com.cyberegylet.antiDupeGallery.helpers.ConfigSort;
import com.cyberegylet.antiDupeGallery.helpers.RealPathUtil;
import com.cyberegylet.antiDupeGallery.helpers.Utils;
import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ImagesActivity extends ImageListBaseActivity
{
	private final ActivityResultLauncher<Intent> moveLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			MOVE_SELECTED,
			o.getResultCode(),
			o.getData()
	));

	private final ActivityResultLauncher<Intent> copyLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			COPY_SELECTED,
			o.getResultCode(),
			o.getData()
	));

	private String currentFolder;
	private final List<ImageFile> allImages = new ArrayList<>();

	public ImagesActivity()
	{
		super("FolderViewActivity");
	}

	@Override
	protected boolean myOnCreate(@Nullable Bundle savedInstanceState)
	{
		setContentView(R.layout.images_activity);
		contentSet();

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		String path = (String) activityManager.getParam("path");

		String sort = ConfigSort.toMediaSQLString(Config.getStringProperty(Config.Property.IMAGE_SORT));
		try (Cursor cursor = database.query(
				Cache.Tables.MEDIA,
				new String[]{ Cache.Media.PATH, Cache.Media.MIME_TYPE, Cache.Media.ID },
				Cache.Media.ALBUM_PATH + " = ?",
				new String[]{ path },
				null,
				null,
				null
		))
		{
			if (!cursor.moveToFirst()) return false;
			int pathCol = cursor.getColumnIndexOrThrow(Cache.Media.PATH);
			int mimeCol = cursor.getColumnIndexOrThrow(Cache.Media.MIME_TYPE);
			int idCol = cursor.getColumnIndexOrThrow(Cache.Media.ID);
			do
			{
				File imageFile = new File(cursor.getString(pathCol));
				if (!imageFile.canRead()) continue;
				allImages.add(new ImageFile(
						imageFile,
						Mimes.Type.getEntries().get(cursor.getInt(mimeCol)),
						cursor.getLong(idCol)
				));
			} while (cursor.moveToNext());
		}

		findViewById(R.id.more_button).setOnClickListener(v -> {
			final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
			final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
			PopupMenu popup = new PopupMenu(this, v);
			popup.inflate(R.menu.main_popup_menu);
			Menu menu = popup.getMenu();
			menu.removeItem(R.id.menu_filter);
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
				menu.add(Menu.NONE, moveId, Menu.NONE, R.string.popup_move);
				menu.add(Menu.NONE, copyId, Menu.NONE, R.string.popup_copy);
				menu.add(Menu.NONE, deleteId, Menu.NONE, R.string.popup_delete);
				menu.add(Menu.NONE, infoId, Menu.NONE, R.string.popup_info);
			}
			else deleteId = copyId = moveId = infoId = -1;
			popup.setOnMenuItemClickListener(item -> {
				int id = item.getItemId();
				if (id == R.id.menu_settings) activityManager.switchActivity(SettingsActivity.class);
				else if (id == R.id.menu_about) activityManager.switchActivity(AboutActivity.class);
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
									android.R.string.yes,
									(dialog, whichButton) -> myOnActivityResult(
											DELETE_SELECTED,
											RESULT_OK,
											new Intent()
									)
							).setNegativeButton(android.R.string.no, null).show();
				}
				else if (id == infoId)
				{
					ViewGroup popupInfo = (ViewGroup) activityManager.makePopupWindow(R.layout.dialog_info)
							.getContentView();
					TextView nameField = popupInfo.findViewById(R.id.info_name);
					TextView countField = popupInfo.findViewById(R.id.info_count);
					TextView pathField = popupInfo.findViewById(R.id.info_path);
					TextView sizeField = popupInfo.findViewById(R.id.info_size);
					TextView creDateField = popupInfo.findViewById(R.id.info_cdate);
					TextView modDateField = popupInfo.findViewById(R.id.info_mdate);
					if (selected.size() == 1)
					{
						ImageFile imageFile = ((ImagesAdapter.ViewHolder) selected.get(0)).getImage();
						modDateField.setText(Utils.msToDate(imageFile.getModifiedDate()));
						creDateField.setText(Utils.msToDate(imageFile.getCreationDate()));
						pathField.setText(imageFile.getFile().getParent());
						nameField.setText(imageFile.getName());
					}
					else
					{
						creDateField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_cdate_header).setVisibility(View.GONE);
						modDateField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_mdate_header).setVisibility(View.GONE);
						pathField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_path_header).setVisibility(View.GONE);
						nameField.setVisibility(View.GONE);
						popupInfo.findViewById(R.id.info_name_header).setVisibility(View.GONE);
					}

					long sizeB = 0;
					for (BaseImageAdapter.ViewHolder holder : selected)
					{
						ImageFile image = ((ImagesAdapter.ViewHolder) holder).getImage();
						sizeB += image.getSize();
					}

					sizeField.setText(Utils.getByteStringFromSize(sizeB));

					((TextView) popupInfo.getChildAt(2)).setText(R.string.popup_items_selected);
					countField.setText(String.valueOf(selected.size()));
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
		// ToDo fix this, its very hacky
		Path path = null;
		if (requestCode != DELETE_SELECTED)
		{
			// ToDo path isnt tree, look into this if needs changes
			Uri uri = data.getData();
			Log.d("app", "uri: " + uri);
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
		List<ImageFile> failedImages = new ArrayList<>();
		int textId = 0;
		switch (requestCode)
		{
			case MOVE_SELECTED ->
			{
				textId = R.string.popup_move_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					ImagesAdapter.ViewHolder holder = (ImagesAdapter.ViewHolder) tmp;
					ImageFile imageFile = holder.getImage();
					Path p = Paths.get(imageFile.getPath());
					if (!fileManager.moveFile(p, path))
					{
						failedImages.add(imageFile);
						continue;
					}
					if (!p.equals(path))
					{
						allImages.remove(imageFile);
						Cache.updateMedia(imageFile);
					}
				}
			}
			case COPY_SELECTED ->
			{
				textId = R.string.popup_copy_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					ImagesAdapter.ViewHolder holder = (ImagesAdapter.ViewHolder) tmp;
					ImageFile imageFile = holder.getImage();
					Path p = Paths.get(imageFile.getPath());
					if (!fileManager.copyFile(p, path))
					{
						failedImages.add(imageFile);
						continue;
					}
					Path p2 = path.resolve(p.getFileName());
					ImageFile newImage = new ImageFile(p2.toFile(), Mimes.getMimeEnumType(p2.toString()));
					Album album = new Album(path.toString());
					Cache.addAlbum(album);
					Cache.addMedia(newImage);
					if (p.equals(path)) allImages.add(newImage);
				}
			}
			case DELETE_SELECTED ->
			{
				textId = R.string.popup_delete_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					ImagesAdapter.ViewHolder holder = (ImagesAdapter.ViewHolder) tmp;
					ImageFile imageFile = holder.getImage();
					Path p = Paths.get(imageFile.getPath());
					if (!fileManager.deleteFile(p))
					{
						failedImages.add(imageFile);
						continue;
					}
					Cache.deleteMedia(imageFile);
					allImages.remove(imageFile);
				}
			}
		}
		if (failedImages.size() == 0)
		{
			filterRecycle(search.getQuery().toString());
			Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
		}
		else
		{
			ScrollView scroll = activityManager.makePopupWindow(R.layout.dialog_scroll).getContentView()
					.findViewById(R.id.dialog_scroll);
			for (ImageFile i : failedImages)
			{
				TextView textView = new TextView(this);
				textView.setText(i.getPath());
				scroll.addView(textView);
			}
		}
	}

	@Override
	protected void storageAccessGranted()
	{
		List<ImageFile> imagesCopy = allImages.stream()
				.filter(image -> !image.isHidden() || Config.getBooleanProperty(Config.Property.SHOW_HIDDEN))
				.sorted(ConfigSort.getImageComparator()).collect(Collectors.toList());

		recycler.setAdapter(new ImagesAdapter(imagesCopy, fileManager));
	}

	@Override
	protected void filterRecycle(String text)
	{
		String text2 = text.toLowerCase(Locale.ROOT);
		boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
		ImagesAdapter adapter = (ImagesAdapter) Objects.requireNonNull(recycler.getAdapter());
		adapter.filter(files -> {
			files.clear();
			for (ImageFile image : allImages)
			{
				if ((showHidden || !image.isHidden()) && image.getName().toLowerCase(Locale.ROOT).contains(text2))
				{
					files.add(image);
				}
			}
			files.sort(ConfigSort.getImageComparator());
		});
	}
}
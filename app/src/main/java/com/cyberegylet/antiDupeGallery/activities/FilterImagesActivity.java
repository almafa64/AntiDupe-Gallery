package com.cyberegylet.antiDupeGallery.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.adapters.FilterImagesAdapter;
import com.cyberegylet.antiDupeGallery.backend.Cache;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.Mimes;
import com.cyberegylet.antiDupeGallery.helpers.RealPathUtil;
import com.cyberegylet.antiDupeGallery.helpers.Utils;
import com.cyberegylet.antiDupeGallery.helpers.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;

public class FilterImagesActivity extends AppCompatActivity
{
	private static final String TAG = "FilterImagesActivity";
	private static final int MOVE_SELECTED_IMAGES = 1;
	private static final int COPY_SELECTED_IMAGES = 2;
	private static final int DELETE_SELECTED_IMAGES = 3;

	private final ActivityManager activityManager = new ActivityManager(this);

	private final ActivityResultLauncher<Intent> moveLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			MOVE_SELECTED_IMAGES,
			o.getResultCode(),
			o.getData()
	));

	private final ActivityResultLauncher<Intent> copyLauncher = activityManager.registerLauncher(o -> myOnActivityResult(
			COPY_SELECTED_IMAGES,
			o.getResultCode(),
			o.getData()
	));

	private final List<ImageFile> allImages = new ArrayList<>();
	private SQLiteDatabase database;
	private RecyclerView recycler;
	private FileManager fileManager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.images_activity);

		database = Cache.getCache();
		recycler = findViewById(R.id.recycler);

		recycler.setLayoutManager(new LinearLayoutManager(this));

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());
		findViewById(R.id.more_button).setOnClickListener(v -> {
			final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
			final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
			PopupMenu popup = new PopupMenu(this, v);
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
				if (id == moveId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					//startActivityForResult(intent, MOVE_SELECTED_IMAGES);
					activityManager.launchIntent(intent, moveLauncher);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					//startActivityForResult(intent, COPY_SELECTED_IMAGES);
					activityManager.launchIntent(intent, copyLauncher);
				}
				else if (id == deleteId)
				{
					new AlertDialog.Builder(this).setTitle(R.string.popup_delete)
							.setMessage(R.string.popup_delete_confirm).setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(
									android.R.string.yes,
									(dialog, whichButton) -> myOnActivityResult(
											DELETE_SELECTED_IMAGES,
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
						ImageFile imageFile = ((FilterImagesAdapter.ViewHolder) selected.get(0)).getImage();
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
						ImageFile image = ((FilterImagesAdapter.ViewHolder) holder).getImage();
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

		fileManager = new FileManager(this);
		fileManager.requestStoragePermissions(b -> {
			if (b == null || b.length == 0) storageAccessGranted();
			else
			{
				Toast.makeText(this, getString(R.string.permission_storage_denied), Toast.LENGTH_SHORT).show();
				finishAndRemoveTask();
			}
			return Unit.INSTANCE;
		});
	}


	protected void myOnActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK || data == null) return;
		final BaseImageAdapter adapter = ((BaseImageAdapter) Objects.requireNonNull(recycler.getAdapter()));
		final List<BaseImageAdapter.ViewHolder> selected = adapter.getSelected;
		// ToDo fix this, its very hacky
		Path path = null;
		if (requestCode != DELETE_SELECTED_IMAGES)
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
		List<ImageFile> failedImages = new ArrayList<>();
		int textId = 0;
		switch (requestCode)
		{
			case MOVE_SELECTED_IMAGES ->
			{
				textId = R.string.popup_move_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FilterImagesAdapter.ViewHolder holder = (FilterImagesAdapter.ViewHolder) tmp;
					Path p = Paths.get(holder.getImage().getPath());
					ImageFile imageFile = holder.getImage();
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
			case COPY_SELECTED_IMAGES ->
			{
				textId = R.string.popup_copy_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FilterImagesAdapter.ViewHolder holder = (FilterImagesAdapter.ViewHolder) tmp;
					Path p = Paths.get(holder.getImage().getPath());
					ImageFile imageFile = holder.getImage();
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
			case DELETE_SELECTED_IMAGES ->
			{
				textId = R.string.popup_delete_file_success;
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					FilterImagesAdapter.ViewHolder holder = (FilterImagesAdapter.ViewHolder) tmp;
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
		if (failedImages.size() == 0) Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
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

	protected void storageAccessGranted()
	{
		String digestHex = (String) activityManager.getParam("digestHex");

		/*String digestPath = Cache.Tables.DIGESTS + "." + Cache.Digests.PATH;
		String mediaPath = Cache.Tables.MEDIA + "." + Cache.Media.PATH;
		String mediaId = Cache.Tables.MEDIA + "." + Cache.Media.ID;
		try (Cursor cursor = database.rawQuery(
				"SELECT " + digestPath + "," + Cache.Media.MIME_TYPE + "," + mediaId +
						" FROM " + Cache.Tables.DIGESTS +
						" INNER JOIN " + Cache.Tables.MEDIA + " ON" +
						" " + mediaPath + " = " + digestPath +
						" WHERE hex(" + Cache.Digests.DIGEST + ") like ?" +
						" ORDER BY " + digestPath, new String[]{ digestHex }))
		{*/
		String chash_id = Cache.Tables.CHASH + "." + Cache.CHash.MEDIA_ID;
		String chash_bytes = Cache.Tables.CHASH + "." + Cache.CHash.BYTES;
		try (Cursor cursor = database.rawQuery(
				"SELECT " + Cache.Media.PATH + "," + Cache.Media.MIME_TYPE + "," + Cache.Media.ID + " " +
						"FROM " + Cache.Tables.MEDIA + " " +
						"INNER JOIN " + Cache.Tables.CHASH + " ON " + Cache.Media.ID + " = " + chash_id + " " +
						"WHERE HEX(" + chash_bytes + ") = ? " +
						"ORDER BY " + Cache.Media.PATH,
				new String[]{ digestHex }
		))
		{
			if (!cursor.moveToFirst()) return;
			int pathCol = cursor.getColumnIndexOrThrow(Cache.Media.PATH);
			int idCol = cursor.getColumnIndexOrThrow(Cache.Media.ID);
			int mimeCol = cursor.getColumnIndexOrThrow(Cache.Media.MIME_TYPE);
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

		recycler.setAdapter(new FilterImagesAdapter(allImages, fileManager));
	}
}
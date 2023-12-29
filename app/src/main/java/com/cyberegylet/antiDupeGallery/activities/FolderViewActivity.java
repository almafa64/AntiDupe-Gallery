package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.BaseImageAdapter;
import com.cyberegylet.antiDupeGallery.adapters.ThumbnailAdapter;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.Utils;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class FolderViewActivity extends Activity
{
	private static final String TAG = "FolderViewActivity";

	private static final int MOVE_IMAGE_SELECT_ID = 1;
	private static final int COPY_IMAGE_SELECT_ID = 2;
	private FileManager fileManager;
	private RecyclerView recycler;
	private String currentFolder;
	private final ActivityManager activityManager = new ActivityManager(this);
	private List<ImageFile> images;
	private SearchView search;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_view);

		//currentFolder = (String) activityManager.getParam("currentFolder");
		images = activityManager.getListParam("images");

		recycler = findViewById(R.id.items);
		int span = Config.getIntProperty(Config.Property.IMAGE_COLUMN_NUMBER);
		recycler.setLayoutManager(new GridLayoutManager(this, span));
		search = findViewById(R.id.search_bar);

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());
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
					startActivityForResult(intent, MOVE_IMAGE_SELECT_ID);
				}
				else if (id == copyId)
				{
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, COPY_IMAGE_SELECT_ID);
				}
				else if (id == deleteId)
				{
					List<String> failedImages = new ArrayList<>();
					for (BaseImageAdapter.ViewHolder tmp : selected)
					{
						ThumbnailAdapter.ViewHolder holder = (ThumbnailAdapter.ViewHolder) tmp;
						Path p = Paths.get(holder.getImage().getPath());
						if (!fileManager.deleteFile(p)) failedImages.add(holder.getImage().getName());
					}
					if (failedImages.size() == 0)
					{
						Toast.makeText(this, R.string.popup_delete_file_success, Toast.LENGTH_SHORT).show();
					}
					else
					{
						// ToDo error dialog
					}
				}
				else if(id == infoId)
				{
					ViewGroup popupInfo = (ViewGroup) activityManager.MakePopupWindow(R.layout.dialog_info).getContentView();
					TextView name = popupInfo.findViewById(R.id.info_name);
					TextView count = popupInfo.findViewById(R.id.info_count);
					TextView path = popupInfo.findViewById(R.id.info_path);
					TextView size = popupInfo.findViewById(R.id.info_size);
					if(selected.size() == 1)
					{
						File f = ((ThumbnailAdapter.ViewHolder)selected.get(0)).getImage().getFile();
						path.setText(f.getParent());
						name.setText(f.getName());
					}
					else
					{
						path.setVisibility(View.GONE);
						popupInfo.getChildAt(4).setVisibility(View.GONE);
						name.setVisibility(View.GONE);
						popupInfo.getChildAt(0).setVisibility(View.GONE);
					}

					long sizeB = 0;
					for(BaseImageAdapter.ViewHolder holder : selected)
					{
						ImageFile image = ((ThumbnailAdapter.ViewHolder)holder).getImage();
						sizeB += image.getSize();
					}

					size.setText(Utils.getByteStringFromSize(sizeB));

					((TextView)popupInfo.getChildAt(2)).setText(R.string.popup_items_selected);
					count.setText(String.valueOf(selected.size()));
				}
				else return false;
				return true;
			});
			popup.show();
		});

		fileManager = new FileManager(this);
		if (fileManager.hasFileAccess()) fileThings();
	}

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
		String text2 = search.getQuery().toString().toLowerCase(Locale.ROOT);
		boolean showHidden = Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
		((ThumbnailAdapter) recycler.getAdapter()).filter(dirs -> {
			dirs.clear();
			dirs.addAll(images.stream()
					.filter(image -> (!image.isHidden() || showHidden) && image.getName().toLowerCase(Locale.ROOT).contains(text2))
					.collect(Collectors.toList()));
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
			case MOVE_IMAGE_SELECT_ID:
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					ThumbnailAdapter.ViewHolder holder = (ThumbnailAdapter.ViewHolder) tmp;
					Path p = Paths.get(holder.getImage().getPath());
					if (!fileManager.moveFile(p, path)) failedFolders.add(holder.getImage().getName());
				}
				break;
			case COPY_IMAGE_SELECT_ID:
				for (BaseImageAdapter.ViewHolder tmp : selected)
				{
					ThumbnailAdapter.ViewHolder holder = (ThumbnailAdapter.ViewHolder) tmp;
					Path p = Paths.get(holder.getImage().getPath());
					if (!fileManager.copyFile(p, path)) failedFolders.add(holder.getImage().getName());
				}
				break;
		}
		if (failedFolders.size() == 0)
		{
			Toast.makeText(
					this,
					(requestCode == MOVE_IMAGE_SELECT_ID) ? R.string.popup_move_file_success : R.string.popup_copy_file_success,
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

	private void fileThings()
	{
		List<ImageFile> imagesCopy = images.stream()
				.filter(image -> !image.isHidden() || Config.getBooleanProperty(Config.Property.SHOW_HIDDEN)).collect(Collectors.toList());

		recycler.setAdapter(new ThumbnailAdapter(imagesCopy, fileManager));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);

		search.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query) { return false; }

			@Override
			public boolean onQueryTextChange(String text)
			{
				String text2 = text.toLowerCase(Locale.ROOT);
				boolean hide_hidden = !Config.getBooleanProperty(Config.Property.SHOW_HIDDEN);
				((ThumbnailAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					for (ImageFile image : images)
					{
						if (hide_hidden && image.isHidden()) continue;
						if (image.getName().toLowerCase(Locale.ROOT).contains(text2)) dirs.add(image);
					}
				});
				return true;
			}
		});
	}
}

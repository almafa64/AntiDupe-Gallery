package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.adapters.ThumbnailAdapter;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FolderViewActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView recycler;
	private String currentFolder;
	private final ActivityManager activityManager = new ActivityManager(this);
	private List<ImageFile> images;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_view);

		//currentFolder = (String) activityManager.getParam("currentFolder");
		images = activityManager.getListParam("images");

		recycler = findViewById(R.id.items);
		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		findViewById(R.id.more_button).setOnClickListener(v -> {
			Toast.makeText(this, "not done", Toast.LENGTH_SHORT).show();
			/*PopupMenu popup = new PopupMenu(this, v);
			popup.getMenuInflater().inflate(R.menu., popup.getMenu()).show();*/
			// TODO make popup
		});

		fileManager = new FileManager(this);
		if (fileManager.hasReadAccess()) fileThings();
	}

	private void fileThings()
	{
		List<ImageFile> imagesCopy = new ArrayList<>(images);

		recycler.setAdapter(new ThumbnailAdapter(imagesCopy, fileManager));

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
				((ThumbnailAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					images.forEach(image -> {
						if (!image.getBasename().toLowerCase(Locale.ROOT).contains(text2)) return;
						dirs.add(image);
					});
				});
				return true;
			}
		});
	}
}

package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.adapters.ThumbnailAdapter;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FolderViewActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView recycler;
	private String currentFolder;
	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_view);

		currentFolder = (String) activityManager.getParam("currentFolder");

		recycler = findViewById(R.id.items);
		/*findViewById(R.id.downBut).setOnClickListener(v -> items.scrollToPosition(images.size() - 1));
		findViewById(R.id.upBut).setOnClickListener(v -> items.scrollToPosition(0));*/
		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());

		findViewById(R.id.more_button).setOnClickListener(v -> {
			Toast.makeText(this, "too bad", Toast.LENGTH_SHORT).show();
			/*PopupMenu popup = new PopupMenu(this, v);
			popup.getMenuInflater().inflate(R.menu., popup.getMenu()).show();*/
			// TODO make popup
		});

		fileManager = new FileManager(this);
		if (fileManager.hasReadAccess()) fileThings();
	}

	private void fileThings()
	{
		ArrayList<ImageFile> images = new ArrayList<>();
		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				//if (path.contains("/.")) return; // check if file is in empty directory
				int lastSeparator = path.lastIndexOf('/');

				if (lastSeparator == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				if (!path.substring(0, lastSeparator)
						.equals(currentFolder)/* || !new File(path).canRead()*/) // commented out because very slow
				{
					return;
				}

				images.add(new ImageFile(FileManager.stringToUri(path)));
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoInFolderLoop(currentFolder, sort, wrapper, MediaStore.MediaColumns.DATA);

		List<ImageFile> imagesCopy = new ArrayList<>(images);

		recycler.setAdapter(new ThumbnailAdapter(images, fileManager));

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
				((ThumbnailAdapter) Objects.requireNonNull(recycler.getAdapter())).filter(dirs -> {
					dirs.clear();
					imagesCopy.forEach(image -> {
						if (!image.getBasename().contains(text)) return;
						images.add(image);
					});
				});
				return true;
			}
		});
	}
}

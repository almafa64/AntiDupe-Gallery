package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.adapters.ThumbnailAdapter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.backend.Activites.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.util.ArrayList;

public class FolderViewActivity extends Activity
{
	private FileManager fileManager;
	private RecyclerView items;
	ArrayList<ImageFile> images = new ArrayList<>();

	private String currentFolder;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_view);

		currentFolder = (String) ActivityManager.getParam(this, "currentFolder");

		items = findViewById(R.id.items);
		findViewById(R.id.downBut).setOnClickListener(v -> items.scrollToPosition(images.size() - 1));
		findViewById(R.id.upBut).setOnClickListener(v -> items.scrollToPosition(0));
		findViewById(R.id.back_button).setOnClickListener(v -> ActivityManager.goBack(this));

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
		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				//if (path.contains("/.")) return; // check if file is in empty directory
				int lastSeparator = path.lastIndexOf('/');

				if (lastSeparator == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				if(!path.substring(0, lastSeparator).equals(currentFolder))
				{
					return;
				}

				images.add(new ImageFile(fileManager.stringToUri(path), path.substring(lastSeparator + 1)));
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoInFolderLoop(currentFolder, sort, wrapper, MediaStore.MediaColumns.DATA);

		items.setAdapter(new ThumbnailAdapter(images, fileManager, item -> {
			// TODO open files
		}));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
	}
}

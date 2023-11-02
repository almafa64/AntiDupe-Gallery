package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.Adapters.ThumbnailAdapter;
import com.cyberegylet.antiDupeGallery.Models.ImageFile;
import com.cyberegylet.antiDupeGallery.backend.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.util.ArrayList;
import java.util.Arrays;

public class FolderMain extends Activity
{
	private FileManager fileManager;
	private RecyclerView recyclerView;
	ArrayList<ImageFile> images = new ArrayList<>();

	private String currentFolder;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_files);

		currentFolder = (String) ActivityManager.getParam(this, "currentFolder");

		recyclerView = findViewById(R.id.recycle);
		findViewById(R.id.downBut).setOnClickListener(v -> recyclerView.scrollToPosition(images.size() - 1));
		findViewById(R.id.upBut).setOnClickListener(v -> recyclerView.scrollToPosition(0));
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
				int lastThing = path.lastIndexOf('/');

				if (lastThing == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				if(!path.substring(0, lastThing).equals(currentFolder)) return;

				images.add(new ImageFile(fileManager.stringToUri(path), path.substring(lastThing + 1)));
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoInFolderLoop(currentFolder, sort, wrapper, MediaStore.MediaColumns.DATA);

		recyclerView.setAdapter(new ThumbnailAdapter(images, fileManager, item -> {
			// TODO open files
		}));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
	}
}

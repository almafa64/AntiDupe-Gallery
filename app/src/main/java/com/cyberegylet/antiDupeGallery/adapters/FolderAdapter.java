package com.cyberegylet.antiDupeGallery.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.FolderViewActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.models.ImageFolder;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class FolderAdapter extends ThumbnailAdapter
{
	public FolderAdapter(List<ImageFile> images, FileManager fileManager)
	{
		super(images,
				fileManager,
				item -> ActivityManager.switchActivity(fileManager.activity,
						FolderViewActivity.class,
						new ActivityParameter("currentFolder",
								Objects.requireNonNull(new File(Objects.requireNonNull(item.getPath().getPath())).getParentFile())
										.getAbsolutePath()
						)
				)
		);
	}

	public class ViewHolder extends ThumbnailAdapter.ViewHolder
	{
		public TextView name;
		public TextView count;

		public ViewHolder(View itemView)
		{
			super(itemView);
			name = itemView.findViewById(R.id.folderName);
			count = itemView.findViewById(R.id.fileCount);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.folder_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull ThumbnailAdapter.ViewHolder holder, int position)
	{
		ImageFolder imageFolder = (ImageFolder) images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFolder.getPath());
		ViewHolder thisHolder = (ViewHolder) holder;
		thisHolder.name.setText(imageFolder.getBasename());
		thisHolder.count.setText(String.valueOf(imageFolder.getFileCount()));
	}
}
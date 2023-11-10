package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.FolderViewActivity;
import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.List;

public class FolderAdapter extends BaseImageAdapter
{
	public interface FilterRun
	{
		void filter(List<Folder> folders);
	}

	private final List<Folder> folders;

	public FolderAdapter(List<Folder> folders, FileManager fileManager)
	{
		super(fileManager);
		this.folders = folders;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		public TextView name;
		public TextView count;

		public ViewHolder(View itemView)
		{
			super(itemView, pos -> ActivityManager.switchActivity(fileManager.activity,
					FolderViewActivity.class,
					new ActivityParameter(
							"currentFolder",
							folders.get(pos).path.getPath()
					)
			));
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
	public void onBindViewHolder(@NonNull BaseImageAdapter.ViewHolder holder, int position)
	{
		Folder folder = folders.get(position);
		ImageFile image = folder.images.get(0);
		fileManager.thumbnailIntoImageView(holder.img, image.getPath());
		ViewHolder thisHolder = (ViewHolder) holder;
		thisHolder.name.setText(folder.name);
		thisHolder.count.setText(String.valueOf(folder.images.size()));
	}

	@Override
	public int getItemCount()
	{
		return folders.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void filter(FilterRun filterRun)
	{
		filterRun.filter(folders);
		notifyDataSetChanged();
	}
}
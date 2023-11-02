package com.cyberegylet.antiDupeGallery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.R;

import java.util.List;

public class FolderAdapter extends ThumbnailAdapter
{
	public FolderAdapter(List<ImageFile> images, FileManager fileManager, OnItemClickListener listener)
	{
		super(images, fileManager, listener);
	}

	public static class ViewHolder extends ThumbnailAdapter.ViewHolder
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
	public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_card, parent, false);
		return new FolderAdapter.ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull ThumbnailAdapter.ViewHolder holder, int position)
	{
		// TODO Optimize?
		super.onBindViewHolder(holder, position);
		ImageFile data = images.get(position);
		FolderAdapter.ViewHolder thisHolder = (FolderAdapter.ViewHolder) holder;
		thisHolder.name.setText(data.name);
		thisHolder.count.setText(String.valueOf(data.fileCount));
	}
}
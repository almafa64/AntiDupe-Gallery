package com.cyberegylet.antiDupeGallery.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.Models.ImageFile;
import com.cyberegylet.antiDupeGallery.R;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>
{
	public interface OnItemClickListener
	{
		void onItemClick(ImageFile imageFile);
	}

	protected final List<ImageFile> images;

	protected final FileManager fileManager;

	protected final OnItemClickListener listener;

	public ThumbnailAdapter(List<ImageFile> images, FileManager fileManager, OnItemClickListener listener)
	{
		this.images = images;
		this.fileManager = fileManager;
		this.listener = listener;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView img;

		public ViewHolder(View itemView)
		{
			super(itemView);
			img = itemView.findViewById(R.id.image);
		}
	}

	@NonNull
	@Override
	public ThumbnailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_card, parent, false);
		return new ThumbnailAdapter.ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull ThumbnailAdapter.ViewHolder holder, int position)
	{
		// TODO Optimize?
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.uri);
		holder.img.setOnClickListener(v -> listener.onItemClick(imageFile));
	}

	@Override
	public int getItemCount()
	{
		return images.size();
	}
}

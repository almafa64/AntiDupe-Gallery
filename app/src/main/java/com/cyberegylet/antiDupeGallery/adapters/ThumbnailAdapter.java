package com.cyberegylet.antiDupeGallery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.ImageViewActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;
import com.cyberegylet.antiDupeGallery.R;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>
{
	public interface OnItemClickListener
	{
		void onItemClick(ImageFile imageFile);
	}

	protected final List<ImageFile> images;
	protected final FileManager fileManager;
	protected final OnItemClickListener clickListener;
	protected boolean inSelectMode = false;
	protected List<View> selected = new ArrayList<>();

	protected void selectView(View view)
	{
		view.setVisibility(View.VISIBLE);
		selected.add(view);
		if (!inSelectMode) inSelectMode = true;
	}

	protected void unSelectView(View view)
	{
		view.setVisibility(View.INVISIBLE);
		selected.remove(view);
		if (selected.size() == 0) inSelectMode = false;
	}

	protected ThumbnailAdapter(
			List<ImageFile> images, FileManager fileManager, OnItemClickListener clickListener
	)
	{
		this.images = images;
		this.fileManager = fileManager;
		this.clickListener = clickListener;
		setHasStableIds(true);
	}

	public ThumbnailAdapter(List<ImageFile> images, FileManager fileManager)
	{
		this(images,
				fileManager,
				item -> ActivityManager.switchActivity(fileManager.activity,
						ImageViewActivity.class,
						new ActivityParameter("imageUri", item.getPath())
				)
		);
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

	@Override
	public int getItemViewType(int position) { return position; }

	protected void onCreateViewHolderShare(View view, int position)
	{
		ImageFile file = images.get(position);
		// view = ConstraintLayout (the parent of ImageView)
		view.setOnClickListener(v -> {
			if (inSelectMode)
			{
				ImageView selectedImg = v.findViewById(R.id.selected_logo);
				if (selectedImg.getVisibility() == View.INVISIBLE)
				{
					selectView(selectedImg);
				}
				else
				{
					unSelectView(selectedImg);
				}
			}
			else clickListener.onItemClick(file);
		});
		view.setOnLongClickListener(v -> {
			ImageView selectedImg = v.findViewById(R.id.selected_logo);
			if(selectedImg.getVisibility() == View.INVISIBLE) selectView(selectedImg);
			return true;
		});
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_card, parent, false);
		onCreateViewHolderShare(contactView, viewType);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull ThumbnailAdapter.ViewHolder holder, int position)
	{
		// TODO Optimize?
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.getPath());
	}

	@Override
	public int getItemCount()
	{
		return images.size();
	}
}

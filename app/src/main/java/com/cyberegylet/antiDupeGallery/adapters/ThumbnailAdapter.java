package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
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

	public interface FilterRun
	{
		void filter(List<ImageFile> images);
	}

	protected final List<ImageFile> images;
	protected final FileManager fileManager;
	protected final OnItemClickListener clickListener;
	protected boolean inSelectMode = false;
	protected List<View> selected = new ArrayList<>();

	protected LayoutInflater layoutInflater;

	protected void selectView(View view)
	{
		view.setVisibility(View.VISIBLE);
		selected.add(view);
		if (!inSelectMode)
		{
			inSelectMode = true;
			// TODO add multi-select options to popup (move, copy, info, etc)
		}
	}

	protected void unSelectView(View view)
	{
		view.setVisibility(View.INVISIBLE);
		selected.remove(view);
		if (selected.size() == 0)
		{
			inSelectMode = false;
			// TODO remove multi-select options from popup (move, copy, info, etc)
		}
	}

	protected ThumbnailAdapter(
			List<ImageFile> images, FileManager fileManager, OnItemClickListener clickListener
	)
	{
		this.images = images;
		this.fileManager = fileManager;
		this.clickListener = clickListener;
		setHasStableIds(true);
		layoutInflater = LayoutInflater.from(fileManager.activity);
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

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView img;

		public ViewHolder(View itemView)
		{
			super(itemView);
			img = itemView.findViewById(R.id.image);

			// view = ConstraintLayout (the parent of ImageView)
			itemView.setOnClickListener(v -> {
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
				else clickListener.onItemClick(images.get(getAdapterPosition()));
			});
			itemView.setOnLongClickListener(v -> {
				ImageView selectedImg = v.findViewById(R.id.selected_logo);
				if (selectedImg.getVisibility() == View.INVISIBLE) selectView(selectedImg);
				return true;
			});
		}
	}

	@Override
	public int getItemViewType(int position) { return position; }
	@Override
	public long getItemId(int position) { return position; }

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.thumbnail_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull ThumbnailAdapter.ViewHolder holder, int position)
	{
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.getPath());
	}

	@Override
	public int getItemCount()
	{
		return images.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void filter(FilterRun filterRun)
	{
		filterRun.filter(images);
		notifyDataSetChanged();
	}
}

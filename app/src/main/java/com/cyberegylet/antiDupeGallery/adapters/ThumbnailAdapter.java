package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.activities.ImageViewActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.List;

public class ThumbnailAdapter extends BaseImageAdapter
{
	public interface FilterRun
	{
		void filter(List<ImageFile> images);
	}

	private final List<ImageFile> images;

	public ThumbnailAdapter(List<ImageFile> images, FileManager fileManager)
	{
		super(fileManager);
		this.images = images;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		private ImageFile image;

		public ViewHolder(View itemView)
		{
			super(itemView,
					pos -> ActivityManager.switchActivity(fileManager.activity,
							ImageViewActivity.class,
							new ActivityParameter("imagePath", images.get(pos).getPath())
					)
			);
		}

		public void reIndexImage()
		{
			image = images.get(getAdapterPosition());
		}

		public ImageFile getImage() { return image; }
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.thumbnail_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull BaseImageAdapter.ViewHolder holder, int position)
	{
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.getPath());
		((ViewHolder) holder).reIndexImage();
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

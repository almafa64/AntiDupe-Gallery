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

import java.util.Comparator;
import java.util.List;

public class ImagesAdapter extends BaseImageAdapter
{
	public interface FilterRun
	{
		void filter(List<ImageFile> images);
	}

	private final List<ImageFile> images;

	public ImagesAdapter(List<ImageFile> images, FileManager fileManager)
	{
		super(fileManager);
		this.images = images;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		private ImageFile image;

		public ViewHolder(View itemView)
		{
			super(itemView, pos -> ActivityManager.switchActivity(
					fileManager.activity,
					ImageViewActivity.class,
					new ActivityParameter<>("imagePath", images.get(pos).getPath())
			));
		}

		@Override
		public void reIndex() { image = images.get(getAdapterPosition()); }

		public ImageFile getImage() { return image; }
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.image_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull BaseImageAdapter.ViewHolder holder, int position)
	{
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.getPath());
		holder.reIndex();
	}

	@Override
	public int getItemCount() { return images.size(); }

	@SuppressLint("NotifyDataSetChanged")
	public void filter(FilterRun filterRun)
	{
		filterRun.filter(images);
		notifyDataSetChanged();
	}

	public void sort(Comparator<ImageFile> comparator, boolean update)
	{
		images.sort(comparator);
		if (update) notifyItemRangeChanged(0, getItemCount());
	}
}

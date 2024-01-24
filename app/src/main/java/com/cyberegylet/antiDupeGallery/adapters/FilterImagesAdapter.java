package com.cyberegylet.antiDupeGallery.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.activities.ImageViewActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.List;

public class FilterImagesAdapter extends BaseImageAdapter
{
	private final List<ImageFile> images;

	public FilterImagesAdapter(List<ImageFile> images, FileManager fileManager)
	{
		super(fileManager);
		this.images = images;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		private ImageFile image;
		public final TextView pathField;
		public final CheckBox checkBox;

		public ViewHolder(View itemView)
		{
			super(itemView, null);
			img = itemView.findViewById(R.id.image);

			pathField = itemView.findViewById(R.id.filter_path);
			checkBox = itemView.findViewById(R.id.filter_check);

			// view = ConstraintLayout (the parent of ImageView)
			img.setOnClickListener(v -> {
				if (selected.size() > 0)
				{
					if (checkBox.isChecked()) unSelectView(this, null);
					else selectView(this, null);
				}
				else
				{
					ActivityManager.switchActivity(
							fileManager.activity,
							ImageViewActivity.class,
							new ActivityParameter("imagePath", images.get(getAdapterPosition()).getPath())
					);
				}
			});
			checkBox.setOnCheckedChangeListener((v, check) -> {
				if (check) selectView(this, null);
				else unSelectView(this, null);
			});
		}

		public ImageFile getImage() { return image; }

		@Override
		public void reIndex() { image = images.get(getAdapterPosition()); }
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.filter_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull BaseImageAdapter.ViewHolder holder, int position)
	{
		ImageFile imageFile = images.get(position);
		fileManager.thumbnailIntoImageView(holder.img, imageFile.getPath());
		((ViewHolder) holder).pathField.setText(imageFile.getPath());
		holder.reIndex();
	}

	@Override
	public int getItemCount() { return images.size(); }
}
package com.cyberegylet.antiDupeGallery.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.activities.FilterImagesActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.List;

public class FilterAdapter extends BaseImageAdapter
{
	private final List<FilteredAlbum> albums;

	public FilterAdapter(List<FilteredAlbum> albums, FileManager fileManager)
	{
		super(fileManager);
		this.albums = albums;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		public final TextView nameField;
		public final TextView countField;
		private FilteredAlbum album;

		public ViewHolder(View itemView)
		{
			super(itemView, pos -> ActivityManager.switchActivity(
					fileManager.activity,
					FilterImagesActivity.class,
					new ActivityParameter("digestHex", albums.get(pos).getDigestHex())
			));
			nameField = itemView.findViewById(R.id.folderName);
			countField = itemView.findViewById(R.id.fileCount);
		}

		@Override
		public void reIndex() { album = albums.get(getAdapterPosition()); }

		public FilteredAlbum getAlbum() { return album; }
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View contactView = layoutInflater.inflate(R.layout.album_card, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull BaseImageAdapter.ViewHolder holder, int position)
	{
		FilteredAlbum album = albums.get(position);
		if (album.getIndexImage() != null)
		{
			ImageFile image = album.getIndexImage();
			fileManager.thumbnailIntoImageView(holder.img, image.getPath());
		}
		ViewHolder thisHolder = (ViewHolder) holder;
		thisHolder.nameField.setText(album.getName());
		thisHolder.countField.setText(String.valueOf(album.getCount()));
		holder.reIndex();
	}

	@Override
	public int getItemCount() { return albums.size(); }
}
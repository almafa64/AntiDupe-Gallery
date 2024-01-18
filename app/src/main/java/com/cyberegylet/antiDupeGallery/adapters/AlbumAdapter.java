package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.activities.ImagesActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.Comparator;
import java.util.TreeSet;

public class AlbumAdapter extends BaseImageAdapter
{
	public static class MySortedSet<T> extends TreeSet<T>
	{
		public MySortedSet(Comparator<T> comparator) { super(comparator); }

		public T get(int i)
		{
			int tmp = 0;
			for (T e : this)
			{
				if (tmp++ == i) return e;
			}
			return null;
		}
	}

	public interface FilterRun
	{
		void filter(MySortedSet<Album> folders);
	}

	private final MySortedSet<Album> albums;

	public AlbumAdapter(MySortedSet<Album> albums, FileManager fileManager)
	{
		super(fileManager);
		this.albums = albums;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		public final TextView nameField;
		public final TextView countField;
		private Album album;

		public ViewHolder(View itemView)
		{
			super(itemView, pos -> ActivityManager.switchActivity(
					fileManager.activity,
					ImagesActivity.class,
					new ActivityParameter("path", albums.get(pos).getPath())
			));
			nameField = itemView.findViewById(R.id.folderName);
			countField = itemView.findViewById(R.id.fileCount);
		}

		@Override
		public void reIndex() { album = albums.get(getAdapterPosition()); }

		public Album getAlbum() { return album; }
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
		Album album = albums.get(position);
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
	public int getItemCount()
	{
		return albums.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void filter(FilterRun filterRun)
	{
		filterRun.filter(albums);
		notifyDataSetChanged();
	}
}
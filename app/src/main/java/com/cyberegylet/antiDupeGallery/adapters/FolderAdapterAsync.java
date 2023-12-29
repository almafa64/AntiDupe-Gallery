package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.activities.FolderViewActivity;
import com.cyberegylet.antiDupeGallery.backend.FileManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityParameter;
import com.cyberegylet.antiDupeGallery.models.Folder;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.Comparator;
import java.util.TreeSet;

public class FolderAdapterAsync extends BaseImageAdapter
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
		void filter(MySortedSet<Folder> folders);
	}

	private final MySortedSet<Folder> folders;

	public FolderAdapterAsync(MySortedSet<Folder> folders, FileManager fileManager)
	{
		super(fileManager);
		this.folders = folders;
	}

	public class ViewHolder extends BaseImageAdapter.ViewHolder
	{
		public final TextView name;
		public final TextView count;
		private Folder folder;

		public ViewHolder(View itemView)
		{
			super(itemView, pos -> ActivityManager.switchActivity(fileManager.activity,
					FolderViewActivity.class,
					new ActivityParameter("images", folders.get(pos).images)
			));
			name = itemView.findViewById(R.id.folderName);
			count = itemView.findViewById(R.id.fileCount);
		}

		public void reIndexFolder()
		{
			folder = folders.get(getAdapterPosition());
		}

		public Folder getFolder()
		{
			return folder;
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
		if (folder.images.size() > 0)
		{
			ImageFile image = folder.images.get(0);
			fileManager.thumbnailIntoImageView(holder.img, image.getPath());
		}
		ViewHolder thisHolder = (ViewHolder) holder;
		thisHolder.name.setText(folder.getName());
		thisHolder.count.setText(String.valueOf(folder.images.size()));
		thisHolder.reIndexFolder();
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
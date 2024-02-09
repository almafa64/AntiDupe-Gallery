package com.cyberegylet.antiDupeGallery.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseImageAdapter extends RecyclerView.Adapter<BaseImageAdapter.ViewHolder>
{
	public interface OnItemClickListener
	{
		void onItemClick(int pos);
	}

	protected final FileManager fileManager;
	protected final List<ViewHolder> selected = new ArrayList<>();
	public final List<ViewHolder> getSelected = Collections.unmodifiableList(selected);

	protected final LayoutInflater layoutInflater;

	public BaseImageAdapter(FileManager fileManager)
	{
		this.fileManager = fileManager;
		layoutInflater = LayoutInflater.from(fileManager.activity);
	}

	protected void selectView(ViewHolder holder, ImageView logo)
	{
		if (logo != null) logo.setVisibility(View.VISIBLE);
		selected.add(holder);
	}

	protected void unSelectView(ViewHolder holder, ImageView logo)
	{
		if (logo != null) logo.setVisibility(View.INVISIBLE);
		selected.remove(holder);
	}

	public abstract class ViewHolder extends RecyclerView.ViewHolder
	{
		public final ImageView img;

		public ViewHolder(View itemView, OnItemClickListener listener)
		{
			super(itemView);
			img = itemView.findViewById(R.id.image);

			if (listener == null) return;

			// view = ConstraintLayout (the parent of ImageView)
			itemView.setOnClickListener(v -> {
				if (selected.size() > 0)
				{
					ImageView selectedImg = v.findViewById(R.id.selected_logo);
					if (selectedImg.getVisibility() >= View.INVISIBLE) selectView(this, selectedImg);
					else unSelectView(this, selectedImg);
				}
				else
				{
					listener.onItemClick(getAdapterPosition());
				}
			});
			itemView.setOnLongClickListener(v -> {
				ImageView selectedImg = v.findViewById(R.id.selected_logo);
				if (selectedImg.getVisibility() >= View.INVISIBLE) selectView(this, selectedImg);
				return true;
			});
		}

		public abstract void reIndex();
	}

	@Override
	public int getItemViewType(int position) { return position; }

	@Override
	public long getItemId(int position) { return position; }

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recycler)
	{
		RecyclerView.LayoutManager manager = recycler.getLayoutManager();
		if (!(manager instanceof GridLayoutManager gridLayoutManager)) return;

		BaseImageAdapter adapter = this;

		ScaleGestureDetector gestureDetector = new ScaleGestureDetector(
				fileManager.context,
				new ScaleGestureDetector.SimpleOnScaleGestureListener()
				{
					@Override
					public boolean onScale(@NonNull ScaleGestureDetector detector)
					{
						if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 300)
						{
							float deltaSpan = detector.getCurrentSpan() - detector.getPreviousSpan();
							if (deltaSpan < -1)
							{
								gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount() + 1);
							}
							else if (deltaSpan > 1)
							{
								int span = gridLayoutManager.getSpanCount();
								if (span > 1) gridLayoutManager.setSpanCount(span - 1);
								else return false;
							}
							else return false;
							notifyItemRangeChanged(0, getItemCount());
							return true;
						}
						return false;
					}

					@Override
					public void onScaleEnd(@NonNull ScaleGestureDetector detector)
					{
						Config.Property key;
						if (adapter instanceof AlbumAdapter) key = Config.Property.ALBUM_COLUMN_NUMBER;
						else key = Config.Property.IMAGE_COLUMN_NUMBER;
						Config.setIntProperty(key, gridLayoutManager.getSpanCount());
						Config.save();
					}
				}
		);

		recycler.setOnTouchListener((v, event) -> {
			if (selected.size() == 0) gestureDetector.onTouchEvent(event);
			return false;
		});
	}
}

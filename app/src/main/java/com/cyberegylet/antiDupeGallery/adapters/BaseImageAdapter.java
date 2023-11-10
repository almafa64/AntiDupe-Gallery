package com.cyberegylet.antiDupeGallery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseImageAdapter extends RecyclerView.Adapter<BaseImageAdapter.ViewHolder>
{
	public interface OnItemClickListener
	{
		void onItemClick(int pos);
	}

	protected final FileManager fileManager;
	protected boolean inSelectMode = false;
	protected List<View> selected = new ArrayList<>();

	protected LayoutInflater layoutInflater;

	public BaseImageAdapter(FileManager fileManager)
	{
		this.fileManager = fileManager;
		layoutInflater = LayoutInflater.from(fileManager.activity);
	}

	protected void selectView(ImageView logo)
	{
		logo.setVisibility(View.VISIBLE);
		selected.add(logo); // TODO should add layout or image rather than logo
		if (!inSelectMode)
		{
			inSelectMode = true;
			// TODO add multi-select options to popup (move, copy, info, etc)
		}
	}

	protected void unSelectView(ImageView logo)
	{
		logo.setVisibility(View.INVISIBLE);
		selected.remove(logo); // TODO should add layout or image rather than logo
		if (selected.size() == 0)
		{
			inSelectMode = false;
			// TODO remove multi-select options from popup (move, copy, info, etc)
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView img;
		public ViewHolder(View itemView, OnItemClickListener listener)
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
				else
				{
					listener.onItemClick(getAdapterPosition());
				}
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
}

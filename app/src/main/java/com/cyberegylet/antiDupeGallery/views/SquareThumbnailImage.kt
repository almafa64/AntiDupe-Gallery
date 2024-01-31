package com.cyberegylet.antiDupeGallery.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class SquareThumbnailImage extends AppCompatImageView
{

	public SquareThumbnailImage(@NonNull Context context) { super(context); }

	public SquareThumbnailImage(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

	public SquareThumbnailImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//noinspection SuspiciousNameCombination
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}

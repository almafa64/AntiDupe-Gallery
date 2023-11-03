package com.cyberegylet.antiDupeGallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MotionEventCompat;

import com.bumptech.glide.Glide;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

import java.io.Serializable;

public class ImageViewActivity extends Activity implements Serializable
{
	private static final float MIN_SCALE = 0.5f;
	private static final float MAX_SCALE = 5.0f;
	private static final float SCALE_SNAP_MIN = 0.9f;
	private static final float SCALE_SNAP_MAX = 1.1f;
	private static final float DRAG_X_SNAP_MIN = -40.0f;
	private static final float DRAG_X_SNAP_MAX = 40.0f;
	private static final float DRAG_Y_SNAP_MIN = -40.0f;
	private static final float DRAG_Y_SNAP_MAX = 40.0f;

	private Uri imageUri;
	private ImageView imageView;
	private ConstraintLayout imageContainer;
	private ScaleGestureDetector scaleGestureDetector;
	private float scaleFactor = 1.0f;
	private float offsetX = 0.0f;
	private float offsetY = 0.0f;
	private int activePointerID = MotionEvent.INVALID_POINTER_ID;
	private float lastPointerX = 0.0f;
	private float lastPointerY = 0.0f;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_view);

		imageUri = (Uri) ActivityManager.getParam(this, "imageUri");

		imageView = findViewById(R.id.imageView);
		imageContainer = findViewById(R.id.imageContainer);
		imageContainer.setPadding(0, 0, 0, 0);

		scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

		imageContainer.setOnTouchListener((v, event) -> onImageContainerTouchEvent(event));

		Glide.with(this.getApplicationContext()).load(imageUri).into(imageView);
	}

	private void onScaleFactorChange() {
		imageView.setScaleX(scaleFactor);
		imageView.setScaleY(scaleFactor);
		Log.i("image scaling", "current factor: " + scaleFactor);
	}

	private void onOffsetChange()
	{
		imageView.setTranslationX(offsetX);
		imageView.setTranslationY(offsetY);
		Log.i("image moving", "xoff: " + offsetX + ", yoff: " + offsetY);
	}

	private boolean onImageContainerTouchEvent(MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);

		final int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
			{
				final int pointerIndex = event.getActionIndex();
				final float x = event.getX(pointerIndex);
				final float y = event.getX(pointerIndex);
				lastPointerX = x;
				lastPointerY = y;
				activePointerID = event.getPointerId(pointerIndex);
				Log.i("pointer down", "pointerX: " + lastPointerX + ", pointerY: " + lastPointerY);

				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				final int pointerIndex = event.findPointerIndex(activePointerID);

				final float x = event.getX(pointerIndex);
				final float y = event.getY(pointerIndex);

				final float dx = x - lastPointerX;
				final float dy = y - lastPointerY;

				Log.i("pointer move", "x: " + x + ", y: " + y + ", lastPointerX: " + lastPointerX + ", lastPointerY: " + lastPointerY);

				offsetX += dx;
				offsetY += dy;

				Log.i("pointer move", "dx: " + dx + ", dy: " + dy);

				onOffsetChange();

				lastPointerX = x;
				lastPointerY = y;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				final int pointerIndex = event.findPointerIndex(activePointerID);
				final int pointerId = event.getPointerId(pointerIndex);

				if (pointerId == activePointerID)
				{
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					lastPointerX = event.getX(newPointerIndex);
					lastPointerY = event.getY(newPointerIndex);
					activePointerID = event.getPointerId(newPointerIndex);
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			{
				activePointerID = MotionEvent.INVALID_POINTER_ID;
				if (scaleFactor >= SCALE_SNAP_MIN && scaleFactor <= SCALE_SNAP_MAX) {
					scaleFactor = 1.0f;
					onScaleFactorChange();
				}

				if (offsetX >= DRAG_X_SNAP_MIN && offsetX <= DRAG_X_SNAP_MAX) {
					offsetX = 0.0f;
					onOffsetChange();
				}

				if (offsetY >= DRAG_Y_SNAP_MIN && offsetY <= DRAG_Y_SNAP_MAX) {
					offsetY = 0.0f;
					onOffsetChange();
				}

				break;
			}
		}

		return true;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
			onScaleFactorChange();
			return true;
		}
	}
}

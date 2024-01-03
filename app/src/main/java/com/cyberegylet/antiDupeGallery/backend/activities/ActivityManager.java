package com.cyberegylet.antiDupeGallery.backend.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ActivityManager
{
	public final Activity activity;

	public ActivityManager(Activity activity) { this.activity = activity; }

	public PopupWindow makePopupWindow(int layoutId) { return makePopupWindow(activity, layoutId, null); }

	public PopupWindow makePopupWindow(int layoutId, PopupWindow.OnDismissListener listener)
	{
		return makePopupWindow(activity, layoutId, listener);
	}

	public static PopupWindow makePopupWindow(Activity activity, int layoutId)
	{
		return makePopupWindow(activity, layoutId, null);
	}

	public static PopupWindow makePopupWindow(Activity activity, int layoutId, PopupWindow.OnDismissListener listener)
	{
		ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
		ViewGroup popup = (ViewGroup) activity.getLayoutInflater().inflate(layoutId, root, false);
		PopupWindow window = new PopupWindow(
				popup,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				true
		);
		ActivityManager.applyDim(root, 0.5f);
		window.showAtLocation(root, Gravity.CENTER, 0, 0);
		window.setOnDismissListener(() -> {
			if (listener != null) listener.onDismiss();
			ActivityManager.clearDim(root);
		});
		return window;
	}

	public void switchActivity(Class<? extends Activity> newActivity, ActivityParameter... params)
	{
		switchActivity(activity, newActivity, params);
	}

	public static void switchActivity(
			Activity activity,
			Class<? extends Activity> newActivity,
			ActivityParameter... params
	)
	{
		Intent intent = new Intent(activity, newActivity);
		for (ActivityParameter param : params)
		{
			switch (param.type)
			{
				case INT:
					intent.putExtra(param.name, (Integer) param.data);
					break;
				case STRING:
					intent.putExtra(param.name, (String) param.data);
					break;
				case STRING_ARR:
					intent.putExtra(param.name, (String[]) param.data);
					break;
				case BOOL:
					intent.putExtra(param.name, (Boolean) param.data);
					break;
				case URI:
					intent.putExtra(param.name, (Uri) param.data);
					break;
				case PARCELABLE:
					//noinspection unchecked
					intent.putParcelableArrayListExtra(param.name, (ArrayList<? extends Parcelable>) param.data);
					break;
			}
		}
		activity.startActivity(intent);
	}

	public void goBack() { goBack(activity); }

	public static void goBack(Activity activity) { activity.finish(); }

	public Object getParam(String name) { return getParam(activity, name); }

	public static Object getParam(Activity activity, String name)
	{
		Bundle b = activity.getIntent().getExtras();
		return b == null ? null : b.get(name);
	}

	public <T extends Parcelable> ArrayList<T> getListParam(String name) { return getListParam(activity, name); }

	public static <T extends Parcelable> ArrayList<T> getListParam(Activity activity, String name)
	{
		return activity.getIntent().getParcelableArrayListExtra(name);
	}

	public static void applyDim(@NonNull ViewGroup parent, float dimAmount)
	{
		Drawable dim = new ColorDrawable(Color.BLACK);
		dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
		dim.setAlpha((int) (255 * dimAmount));
		parent.getOverlay().add(dim);
	}

	public static void clearDim(@NonNull ViewGroup parent) { parent.getOverlay().clear(); }
}
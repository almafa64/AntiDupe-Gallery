package com.cyberegylet.antiDupeGallery.backend;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cyberegylet.antiDupeGallery.R;

public class SimpleActivityGenerator
{
	private final Activity activity;
	private final LinearLayout scrollLayout;
	private LinearLayout curLinearLayout = null;
	private final Resources res;
	private final float toPx;
	private final boolean hasIcons;

	public SimpleActivityGenerator(Activity activity, boolean hasIcons, int titleTextID)
	{
		this.activity = activity;
		this.hasIcons = hasIcons;
		res = activity.getResources();
		toPx = res.getDisplayMetrics().density;
		RelativeLayout root = new RelativeLayout(activity);
		activity.addContentView(root,
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		);

		ImageButton back = new ImageButton(new ContextThemeWrapper(activity, R.style.BackArrow), null, 0);
		back.setId(R.id.back_button);
		back.setTooltipText(res.getText(R.string.back_arrow_text));
		back.setContentDescription(res.getText(R.string.back_arrow_text));
		RelativeLayout.LayoutParams backParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);
		backParam.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
		backParam.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		back.setLayoutParams(backParam);

		TextView title = new TextView(activity);
		RelativeLayout.LayoutParams textParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		textParam.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		textParam.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
		textParam.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.back_button);
		textParam.addRule(RelativeLayout.END_OF, R.id.back_button);
		title.setLayoutParams(textParam);
		title.setGravity(Gravity.CENTER_VERTICAL);
		title.setTextDirection(View.TEXT_DIRECTION_LOCALE);
		title.setTextAppearance(R.style.MainHeader);
		title.setText(res.getText(titleTextID));

		ScrollView scroll = new ScrollView(activity);
		RelativeLayout.LayoutParams scrollParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		scrollParam.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
		scrollParam.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
		scrollParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		scrollParam.addRule(RelativeLayout.BELOW, R.id.back_button);
		scroll.setLayoutParams(scrollParam);

		scrollLayout = new LinearLayout(activity);
		ViewGroup.LayoutParams linearParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);
		scrollLayout.setLayoutParams(linearParam);
		scrollLayout.setOrientation(LinearLayout.VERTICAL);

		root.addView(back);
		root.addView(title);
		root.addView(scroll);
		scroll.addView(scrollLayout);
	}

	public void newHeader(int titleTextID)
	{
		if (curLinearLayout != null)
		{
			View divider = new View(new ContextThemeWrapper(activity, R.style.Divider), null, 0);
			scrollLayout.addView(divider);
		}

		TextView header = new TextView(activity);
		LinearLayout.LayoutParams headerParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				res.getDimensionPixelSize(R.dimen.simple_activity_row_height)
		);
		if (hasIcons) headerParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_margin));
		else headerParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_small_margin));
		header.setLayoutParams(headerParam);
		header.setText(res.getText(titleTextID));
		header.setTextDirection(View.TEXT_DIRECTION_LOCALE);
		header.setTextAppearance(R.style.AboutHeader);
		header.setGravity(Gravity.CENTER_VERTICAL);

		scrollLayout.addView(header);
	}

	private void addRipple()
	{
		TypedArray typedArray = activity.obtainStyledAttributes(new int[]{ android.R.attr.selectableItemBackground });
		curLinearLayout.setBackgroundResource(typedArray.getResourceId(0, 0));
		typedArray.recycle();
	}

	public int addRow() { return addRow(null, null, null); }
	public int addRow(Integer textID) { return addRow(textID, null, null); }

	public int addRow(Integer textID, Integer iconID) { return addRow(textID, null, iconID); }

	public int addRow(Integer textID, View customView) { return addRow(textID, customView, null); }

	public int addRow(Integer textID, View customView, Integer iconID)
	{
		curLinearLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				res.getDimensionPixelSize(R.dimen.simple_activity_row_height)
		);
		curLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		curLinearLayout.setLayoutParams(params);
		scrollLayout.addView(curLinearLayout);

		int id = View.generateViewId();
		curLinearLayout.setId(id);
		if(textID == null) return id;

		TextView text = new TextView(activity);
		LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		textParam.weight = 1;
		if (hasIcons)
		{
			if (iconID != null)
			{
				ImageView image = new ImageView(activity);
				LinearLayout.LayoutParams imageParam = new LinearLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.simple_activity_margin),
						(int) (50 * toPx)
				);
				imageParam.topMargin = (int) (10 * toPx);
				image.setLayoutParams(imageParam);
				image.setImageResource(iconID);
				curLinearLayout.addView(image);
			}
			else textParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_margin));
		}
		else textParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_small_margin));
		text.setLayoutParams(textParam);
		text.setText(res.getText(textID));
		text.setTextDirection(View.TEXT_DIRECTION_LOCALE);
		text.setTextAppearance(R.style.AboutBody);
		text.setGravity(Gravity.CENTER_VERTICAL);

		curLinearLayout.addView(text);
		if (customView != null)
		{
			if(customView.getClass().equals(Button.class))
			{
				curLinearLayout.setOnClickListener(v -> customView.performClick());
				addRipple();
				return id;
			}
			LinearLayout.LayoutParams customParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.MATCH_PARENT
			);
			customParam.setMarginEnd(res.getDimensionPixelSize(R.dimen.simple_activity_small_margin));
			customView.setLayoutParams(customParam);
			curLinearLayout.addView(customView);
			if (customView instanceof Checkable)
			{
				curLinearLayout.setOnClickListener(v -> customView.performClick());
				addRipple();
			}
		}

		return id;
	}
}

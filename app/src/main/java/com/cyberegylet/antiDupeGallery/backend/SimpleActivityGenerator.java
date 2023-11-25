package com.cyberegylet.antiDupeGallery.backend;

import android.app.Activity;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

	public SimpleActivityGenerator(Activity activity, int titleTextID)
	{
		this.activity = activity;
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
		LinearLayout.LayoutParams headerParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_activity_row_height));
		headerParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_margin));
		header.setLayoutParams(headerParam);
		header.setText(res.getText(titleTextID));
		header.setTextDirection(View.TEXT_DIRECTION_LOCALE);
		header.setTextAppearance(R.style.AboutHeader);
		header.setGravity(Gravity.CENTER_VERTICAL);

		curLinearLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				(ViewGroup.LayoutParams.MATCH_PARENT)
		);
		curLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

		scrollLayout.addView(header);
		scrollLayout.addView(curLinearLayout);
	}

	public void addRow(int textID) { addRow(null, textID, null); }

	public void addRow(Integer iconID, int textID) { addRow(iconID, textID, null); }

	public void addRow(Integer iconID, int textID, View customView)
	{
		TextView text = new TextView(activity);
		LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_activity_row_height));
		textParam.weight = 1;
		if (iconID != null)
		{
			ImageView image = new ImageView(activity);
			LinearLayout.LayoutParams imageParam = new LinearLayout.LayoutParams((int) (70 * toPx), (int) (50 * toPx));
			imageParam.topMargin = (int) (10 * toPx);
			image.setLayoutParams(imageParam);
			image.setImageResource(iconID);
			curLinearLayout.addView(image);
		}
		else textParam.setMarginStart(res.getDimensionPixelSize(R.dimen.simple_activity_margin));
		text.setLayoutParams(textParam);
		text.setText(res.getText(textID));
		text.setTextDirection(View.TEXT_DIRECTION_LOCALE);
		text.setTextAppearance(R.style.AboutBody);
		text.setGravity(Gravity.CENTER_VERTICAL);

		curLinearLayout.addView(text);
		if (customView != null)
		{
			LinearLayout.LayoutParams customParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, res.getDimensionPixelSize(R.dimen.simple_activity_row_height));
			customParam.setMarginStart((int) (10 * toPx));
			curLinearLayout.addView(customView);
		}
	}
}

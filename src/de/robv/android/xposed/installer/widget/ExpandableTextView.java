package de.robv.android.xposed.installer.widget;

import android.content.Context;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.installer.R;

public class ExpandableTextView extends LinearLayout {

	private final TextView textView;
	private final ImageView imageView;

	private final static int MAX_LINES = 5;

	public ExpandableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);

		textView = new TextView(context);
		imageView = new ImageView(context);

		textView.setMaxLines(MAX_LINES);

		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.RIGHT;
		imageView.setLayoutParams(layoutParams);
		imageView.setImageResource(R.drawable.ic_expand);

		addView(textView);
		addView(imageView);
	}

	public void setText(CharSequence text) {
		textView.setText(text);
	}

	/** Collapses or expands the view if necessary.
	 * Should be called from within an OnClickListener and when the view is created or in the custom
	 * adapter's getView if in a ListView, as the collapsed state must be managed there to avoid it
	 * getting recycled, */
	public void collapseView(boolean collapse) {
		if (textView.getLineCount() <= MAX_LINES) {
			imageView.setVisibility(GONE);
			return;
		} else {
			imageView.setVisibility(VISIBLE);
		}

		if (collapse) {
			textView.setMaxLines(MAX_LINES);
			imageView.setRotationX(0);
		} else {
			textView.setMaxLines(Integer.MAX_VALUE);
			imageView.setRotationX(180);
		}
	}

}

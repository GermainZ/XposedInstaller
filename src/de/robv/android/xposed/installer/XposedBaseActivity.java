package de.robv.android.xposed.installer;

import android.app.Activity;
import android.os.Bundle;

import de.robv.android.xposed.installer.util.NavUtil;

public abstract class XposedBaseActivity extends Activity {
	public boolean leftActivityWithSlideAnim = false;

	private boolean mDarkThemeEnabled;
	private boolean mBlackBackground;

	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);

		mDarkThemeEnabled = XposedApp.getPreferences().getBoolean("use_dark_theme", false);
		mBlackBackground = XposedApp.getPreferences().getBoolean("use_black_bg", false);
		if (mDarkThemeEnabled) {
			if (mBlackBackground)
				setTheme(R.style.Theme_Black);
			else
				setTheme(R.style.Theme_Dark);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		boolean darkThemeEnabled = XposedApp.getPreferences().getBoolean("use_dark_theme", false);
		boolean blackBackground = XposedApp.getPreferences().getBoolean("use_black_bg", false);
		if ((mDarkThemeEnabled != darkThemeEnabled) || (mBlackBackground != blackBackground)) {
			mDarkThemeEnabled = darkThemeEnabled;
			mBlackBackground = blackBackground;
			recreate();
		}

		if (leftActivityWithSlideAnim)
			NavUtil.setTransitionSlideLeave(this);
		leftActivityWithSlideAnim = false;
	}

	public void setLeftWithSlideAnim(boolean newValue) {
		this.leftActivityWithSlideAnim = newValue;
	}
}

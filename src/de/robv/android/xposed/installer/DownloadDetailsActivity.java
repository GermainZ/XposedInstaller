package de.robv.android.xposed.installer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import de.robv.android.xposed.installer.repo.Module;
import de.robv.android.xposed.installer.repo.ModuleGroup;
import de.robv.android.xposed.installer.util.RepoLoader;

public class DownloadDetailsActivity extends XposedDropdownNavActivity {

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private String[] mPages;
	private String mPackageName;
	private Bundle args;

	private static final int DOWNLOAD_DESCRIPTION = 0;
	private static final int DOWNLOAD_VERSIONS = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_download_details);

		mPackageName = getIntent().getData().getSchemeSpecificPart();

		ModuleGroup moduleGroup = RepoLoader.getInstance().waitForFirstLoadFinished().getModuleGroup(mPackageName);
		Module module = moduleGroup.getModule();

		args = new Bundle();
		args.putParcelable("module", module);

		mPages = new String[]{getString(R.string.description_page), getString(R.string.versions_page)};
		mPager = (ViewPager) findViewById(R.id.download_pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean navigateViaIntent() {
		return true;
	}

	@Override
	protected Intent getParentIntent() {
		Intent intent = new Intent(this, XposedInstallerActivity.class);
		intent.putExtra(XposedInstallerActivity.EXTRA_OPEN_TAB, TAB_DOWNLOAD);
		return intent;
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case DOWNLOAD_DESCRIPTION:
					return DownloadDetailsFragment.newInstance(args);
				case DOWNLOAD_VERSIONS:
					return DownloadDetailsVersionsFragment.newInstance(args);
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return mPages.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mPages[position];
		}
	}

}

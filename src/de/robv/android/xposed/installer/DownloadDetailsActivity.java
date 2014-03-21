package de.robv.android.xposed.installer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import de.robv.android.xposed.installer.repo.Module;
import de.robv.android.xposed.installer.repo.ModuleGroup;
import de.robv.android.xposed.installer.util.RepoLoader;

public class DownloadDetailsActivity extends XposedDropdownNavActivity implements RepoLoader.RepoListener {

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private String[] mPages;
	private String mPackageName;
	private Bundle args;

	private DownloadDetailsFragment downloadDescriptionFragment;
	private DownloadDetailsVersionsFragment downloadVersionsFragment;

	private static final int DOWNLOAD_DESCRIPTION = 0;
	private static final int DOWNLOAD_VERSIONS = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_download_details);

		mPackageName = getIntent().getData().getSchemeSpecificPart();

		ModuleGroup moduleGroup = RepoLoader.getInstance().waitForFirstLoadFinished().getModuleGroup(mPackageName);
		Module module = null;
		if (moduleGroup != null) {
			module = moduleGroup.getModule();
		} else {
			RepoLoader.getInstance().triggerReload(true);
			Toast.makeText(this, getString(R.string.download_repo_reload), Toast.LENGTH_LONG).show();
		}

		args = new Bundle();
		args.putParcelable("module", module);

		mPages = new String[]{getString(R.string.description_page), getString(R.string.versions_page)};
		mPager = (ViewPager) findViewById(R.id.download_pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		RepoLoader.getInstance().addListener(this, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_download_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.menu_refresh:
				RepoLoader.getInstance().triggerReload(true);
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
					downloadDescriptionFragment = DownloadDetailsFragment.newInstance(args);
					return downloadDescriptionFragment;
				case DOWNLOAD_VERSIONS:
					downloadVersionsFragment = DownloadDetailsVersionsFragment.newInstance(args);
					return downloadVersionsFragment;
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

	@Override
	public void onRepoReloaded(RepoLoader repoLoader) {
		ModuleGroup moduleGroup = repoLoader.getModuleGroup(mPackageName);
		if (moduleGroup != null) {
			Module module = moduleGroup.getModule();
			downloadVersionsFragment.update(module);
			downloadDescriptionFragment.update(module);
		} else {
			// This can happen if the module has been removed from the repository.
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(DownloadDetailsActivity.this, getString(R.string.download_repo_removed), Toast.LENGTH_LONG).show();
				}
			});
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		RepoLoader.getInstance().removeListener(this);
	}

}

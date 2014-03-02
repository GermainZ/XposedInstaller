package de.robv.android.xposed.installer;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import de.robv.android.xposed.installer.repo.Module;
import de.robv.android.xposed.installer.repo.ModuleGroup;
import de.robv.android.xposed.installer.repo.ModuleVersion;
import de.robv.android.xposed.installer.repo.RepoParser;
import de.robv.android.xposed.installer.util.DownloadsUtil;
import de.robv.android.xposed.installer.util.HashUtil;
import de.robv.android.xposed.installer.util.RepoLoader;
import de.robv.android.xposed.installer.widget.DownloadView;

public class DownloadDetailsVersionsFragment extends ListFragment {

	public static final String ARGUMENT_PACKAGE = "package";
	private static Module mModule;
	private static VersionsAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();

		String mPackageName = args.getString(ARGUMENT_PACKAGE);
		ModuleGroup moduleGroup = RepoLoader.getInstance().waitForFirstLoadFinished().getModuleGroup(mPackageName);
		mModule = moduleGroup.getModule();

		mAdapter = new VersionsAdapter(getActivity());
		mAdapter.addAll(mModule.versions);
		setListAdapter(mAdapter);

		getListView().setDivider(null);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int eightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
		getListView().setDividerHeight(eightDp);
		getListView().setPadding(eightDp, eightDp, eightDp, eightDp);
		getListView().setClipToPadding(false);

	}

	public static DownloadDetailsVersionsFragment newInstance(String packageName) {
		DownloadDetailsVersionsFragment fragment = new DownloadDetailsVersionsFragment();

		Bundle args = new Bundle();
		args.putString(ARGUMENT_PACKAGE, packageName);
		fragment.setArguments(args);

		return fragment;
	}

	static class ViewHolder {
		TextView txtVersion;
		TextView txtBranch;
		DownloadView downloadView;
		TextView txtChangesTitle;
		TextView txtChanges;
	}

	private class VersionsAdapter extends ArrayAdapter<ModuleVersion> {

		public VersionsAdapter(Context context) {
			super(context, R.layout.list_item_version);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_item_version, null, true);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.txtVersion = (TextView) view.findViewById(R.id.txtVersion);
				viewHolder.txtBranch = (TextView) view.findViewById(R.id.txtBranch);
				viewHolder.downloadView = (DownloadView) view.findViewById(R.id.downloadView);
				viewHolder.txtChangesTitle = (TextView) view.findViewById(R.id.txtChangesTitle);
				viewHolder.txtChanges = (TextView) view.findViewById(R.id.txtChanges);
				view.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			ModuleVersion item = (ModuleVersion) getItem(position);

			holder.txtVersion.setText(item.name);
			if (item.branch != null && !item.branch.isEmpty()) {
				holder.txtBranch.setText(getResources().getString(R.string.branch_display, item.branch));
				holder.txtBranch.setVisibility(View.VISIBLE);
			} else {
				holder.txtBranch.setVisibility(View.GONE);
			}

			holder.downloadView.setUrl(item.downloadLink);
			holder.downloadView.setTitle(mModule.name);
			holder.downloadView.setDownloadFinishedCallback(new DownloadModuleCallback(item));

			if (item.changelog != null && !item.changelog.isEmpty()) {
				holder.txtChangesTitle.setVisibility(View.VISIBLE);
				holder.txtChanges.setVisibility(View.VISIBLE);

				if (item.changelogIsHtml) {
					holder.txtChanges.setText(RepoParser.parseSimpleHtml(item.changelog));
					holder.txtChanges.setMovementMethod(LinkMovementMethod.getInstance());
				} else {
					holder.txtChanges.setText(item.changelog);
					holder.txtChanges.setMovementMethod(null);
				}

			} else {
				holder.txtChangesTitle.setVisibility(View.GONE);
				holder.txtChanges.setVisibility(View.GONE);
			}

			return view;
		}
	}

	private static class DownloadModuleCallback implements DownloadsUtil.DownloadFinishedCallback {
		private final ModuleVersion moduleVersion;

		public DownloadModuleCallback(ModuleVersion moduleVersion) {
			this.moduleVersion = moduleVersion;
		}

		@Override
		public void onDownloadFinished(Context context, DownloadsUtil.DownloadInfo info) {
			File localFile = new File(info.localFilename);
			if (!localFile.isFile())
				return;

			if (moduleVersion.md5sum != null && !moduleVersion.md5sum.isEmpty()) {
				try {
					String actualMd5Sum = HashUtil.md5(localFile);
					if (!moduleVersion.md5sum.equals(actualMd5Sum)) {
						Toast.makeText(context, context.getString(R.string.download_md5sum_incorrect,
								actualMd5Sum, moduleVersion.md5sum), Toast.LENGTH_LONG).show();

						return;
					}
				} catch (Exception e) {
					Toast.makeText(context, context.getString(R.string.download_could_not_read_file,
							e.getMessage()), Toast.LENGTH_LONG).show();
					return;
				}
			}

			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(info.localFilename, 0);

			if (packageInfo == null) {
				Toast.makeText(context, R.string.download_no_valid_apk, Toast.LENGTH_LONG).show();
				return;
			}

			if (!packageInfo.packageName.equals(moduleVersion.module.packageName)) {
				Toast.makeText(context,
						context.getString(R.string.download_incorrect_package_name,
								packageInfo.packageName, moduleVersion.module.packageName),
						Toast.LENGTH_LONG).show();

				return;
			}

			Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
			installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.fromFile(localFile), DownloadsUtil.MIME_TYPE_APK);
			//installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
			//installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
			installIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);
			context.startActivity(installIntent);
		}
	}
}

package de.robv.android.xposed.installer;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.robv.android.xposed.installer.callback.DownloadModuleCallback;
import de.robv.android.xposed.installer.repo.Module;
import de.robv.android.xposed.installer.repo.ModuleGroup;
import de.robv.android.xposed.installer.repo.ModuleVersion;
import de.robv.android.xposed.installer.repo.RepoParser;
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

}

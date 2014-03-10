package de.robv.android.xposed.installer;

import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.robv.android.xposed.installer.callback.DownloadModuleCallback;
import de.robv.android.xposed.installer.repo.Module;
import de.robv.android.xposed.installer.repo.ModuleVersion;
import de.robv.android.xposed.installer.repo.RepoParser;
import de.robv.android.xposed.installer.util.AnimatorUtil;
import de.robv.android.xposed.installer.widget.DownloadView;
import de.robv.android.xposed.installer.widget.ExpandableTextView;

public class DownloadDetailsFragment extends Fragment {
	private boolean expanded = false;

	public static DownloadDetailsFragment newInstance(Bundle args) {
		DownloadDetailsFragment fragment = new DownloadDetailsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
		if (activity instanceof XposedDropdownNavActivity)
			((XposedDropdownNavActivity) activity).setNavItem(XposedDropdownNavActivity.TAB_DOWNLOAD);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.download_details, container, false);

		Bundle args = getArguments();
		Module module = args.getParcelable("module");

		if (module != null)
			setContent(view, module, inflater);

		return view;
	}

	private void setContent(View view, Module module, LayoutInflater inflater) {
		TextView title = (TextView) view.findViewById(R.id.download_title);
		title.setText(module.name);
		getActivity().getActionBar().setTitle(R.string.tabDownload);

		TextView author = (TextView) view.findViewById(R.id.download_author);
		if (module.author != null && !module.author.isEmpty())
			author.setText(getString(R.string.download_author, module.author));
		else
			author.setText(R.string.download_unknown_author);

		TextView txtVersion = (TextView) view.findViewById(R.id.txtVersion);
		TextView txtBranch = (TextView) view.findViewById(R.id.txtBranch);
		DownloadView downloadView = (DownloadView) view.findViewById(R.id.downloadView);
		TextView txtChangesTitle = (TextView) view.findViewById(R.id.txtChangesTitle);
		final ExpandableTextView txtChanges = (ExpandableTextView) view.findViewById(R.id.txtChanges);

		ModuleVersion latestVersion = module.versions.get(0);

		txtVersion.setText(latestVersion.name);
		if (latestVersion.branch != null && !latestVersion.branch.isEmpty()) {
			txtBranch.setText(getResources().getString(R.string.branch_display, latestVersion.branch));
			txtBranch.setVisibility(View.VISIBLE);
		} else {
			txtBranch.setVisibility(View.GONE);
		}

		downloadView.setUrl(latestVersion.downloadLink);
		downloadView.setTitle(module.name);
		downloadView.setDownloadFinishedCallback(new DownloadModuleCallback(latestVersion));

		if (latestVersion.changelog != null && !latestVersion.changelog.isEmpty()) {
			txtChangesTitle.setVisibility(View.VISIBLE);
			txtChanges.setVisibility(View.VISIBLE);

			if (latestVersion.changelogIsHtml) {
				txtChanges.setText(RepoParser.parseSimpleHtml(latestVersion.changelog));
				txtChanges.setMovementMethod(LinkMovementMethod.getInstance());
			} else {
				txtChanges.setText(latestVersion.changelog);
				txtChanges.setMovementMethod(null);
			}

			txtChanges.post(new Runnable() {
				@Override
				public void run() {
					txtChanges.collapseView(true);
				}
			});
			txtChanges.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					txtChanges.collapseView(expanded);
					expanded = !expanded;
				}
			});

		} else {
			txtChangesTitle.setVisibility(View.GONE);
			txtChanges.setVisibility(View.GONE);
		}

		TextView description = (TextView) view.findViewById(R.id.download_description);
		if (module.description != null) {
			if (module.descriptionIsHtml) {
				description.setText(RepoParser.parseSimpleHtml(module.description));
				description.setMovementMethod(LinkMovementMethod.getInstance());
			} else {
				description.setText(module.description);
			}
		} else {
			description.setVisibility(View.GONE);
		}

		ViewGroup moreInfoContainer = (ViewGroup) view.findViewById(R.id.download_moreinfo_container);
		moreInfoContainer.removeAllViews();
		for (Pair<String,String> moreInfoEntry : module.moreInfo) {
			TextView moreInfoView = (TextView) inflater.inflate(R.layout.download_moreinfo, moreInfoContainer, false);

			SpannableStringBuilder ssb = new SpannableStringBuilder(moreInfoEntry.first);
			ssb.append(": ");
			ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			ssb.append(moreInfoEntry.second);
			moreInfoView.setText(ssb);

			moreInfoContainer.addView(moreInfoView);
		}
	}

	@Override
	public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
		return AnimatorUtil.createSlideAnimation(this, nextAnim);
	}

	public void update(final Module module) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setContent(getView(), module, LayoutInflater.from(getActivity()));
			}
		});
	}

}

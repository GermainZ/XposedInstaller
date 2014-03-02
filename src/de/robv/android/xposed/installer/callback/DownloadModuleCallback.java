package de.robv.android.xposed.installer.callback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

import de.robv.android.xposed.installer.R;
import de.robv.android.xposed.installer.repo.ModuleVersion;
import de.robv.android.xposed.installer.util.DownloadsUtil;
import de.robv.android.xposed.installer.util.HashUtil;

public class DownloadModuleCallback implements DownloadsUtil.DownloadFinishedCallback {
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
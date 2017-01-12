package eviluess.pkg.Utilities;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

public class PackApp {

	private static final String TAG = "PackApp";

	private static final String STR_SYSAPP = "/system/app/";
	@SuppressLint("SdCardPath") private static final String STR_APPDATA = "/data/data/";
	private static final String STR_SYSDALVIKCACHE = "/data/dalvik-cache/system@app@";
	@SuppressWarnings("unused")
	private static final String STR_APPDALVIKCACHE = "/data/dalvik-cache/data@app@";

	protected static final String PKG_BUSYBOX = "stericson.busybox";
	
	static public boolean restart(final Context context) {
		
		PackageManager manager = context.getPackageManager();  
		
		Log.e(TAG, "Restarting " + context.getPackageName());
		
		Intent i = manager.getLaunchIntentForPackage(context.getPackageName());
		
		if (i == null)
			return false;
		
		context.startActivity(i);
		
		return true;
		
	}

	static public boolean removeConflictingPackage(final Context context,
			final String packageName, CharSequence reason) {

		PackageManager manager = context.getPackageManager();
		final PackageInfo info;

		try {
			info = manager.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return true;
		}

		if (info == null)
			return true;

		Builder alert = new AlertDialog.Builder(context)
				.setTitle(R.string.packAppConflictInstallationFound)
				.setNegativeButton(R.string.utilPopStrNo, null)
				.setPositiveButton(R.string.utilPopStrYes,
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
									uninstallPackage(context, packageName,
											false, true);
								} else {
									uninstallPackage(context, packageName, true,
											false);
								}
								

							}
						});

		if (reason != null)
			alert.setMessage(reason);
		else
			alert.setMessage(R.string.packAppConflictInstallationExplain);

		alert.create().show();

		return true;

	}

	static public boolean uninstallPackage(Context context, String packageName,
			boolean user, boolean system) {

		if (user) {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent mIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			context.startActivity(mIntent);
		}

		if (system) {
			RootCommands.delete(STR_SYSAPP + packageName + ".apk");
			RootCommands.delete(STR_SYSAPP + packageName + ".odex");
			RootCommands.delete(STR_APPDATA + packageName);
			RootCommands.delete(STR_SYSDALVIKCACHE + packageName
					+ ".apk@classes.dex");
		}

		return system;
	}

	static public boolean moveToSystem(final Context c, boolean userRequested) {

		final ApplicationInfo ai = getApplicationInfo(c);

		final String dest = STR_SYSAPP + ai.packageName + ".apk";

		File destFile = new File(dest);

		boolean mustReboot = (destFile == null || !destFile.exists());

		Log.v(TAG, "destFile:" + destFile + ", present:" + destFile.exists()
				+ " mustReboot=" + mustReboot);

		if (mustReboot) {
			if (!userRequested)
				return false;
		}

		Log.v(TAG, "srcFile: " + ai.sourceDir);

		if (ai.sourceDir.startsWith(STR_SYSAPP)) {

			Log.v(TAG, "moveToSystem: " + ai.packageName
					+ " is already in the system path.");

			// new AlertDialog.Builder(c).setTitle("Run From System ^_^")
			// .setMessage("Run From System ^_^")
			// .setPositiveButton(R.string.utilPopStrOK, null).create()
			// .show();

			RootCommands.uninstall(ai.packageName);

			return true;
		}

		if (!RootCommands.canRunRootCommands()) {

			new AlertDialog.Builder(c).setTitle(R.string.utilPopStrError)
					.setMessage(R.string.utilCannotGetRooted)
					.setPositiveButton(R.string.utilPopStrOK, null).create()
					.show();

			return false;
		}

		if (Andrutils.isSameContent(ai.sourceDir, dest)) {

			Log.v(TAG, "App in data/app must be removed");

			new AlertDialog.Builder(c)
					.setTitle(R.string.utilPopStrMultiInstallationFound)
					.setMessage(
							R.string.utilPopStrMultiInstallationFoundExplain)
					.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {

							Log.v(TAG, "uninstall");
							RootCommands.uninstall(ai.packageName);

						}
					})

					.setPositiveButton(R.string.utilPopStrOK,
							new AlertDialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									Log.v(TAG, "uninstall");
									RootCommands.uninstall(ai.packageName);
								}
							}).create().show();

		} else {

			Log.v(TAG, "Sys Not Found or sys <> data!");

			if (mustReboot) {
				RootCommands.copy(ai.sourceDir, dest);

				if (!destFile.exists()) {
					new AlertDialog.Builder(c)
							.setTitle(R.string.utilBusyBoxNotFound)
							.setMessage(R.string.utilBusyBoxIntro)
							.setNegativeButton(R.string.utilPopStrCancel, null)
							.setNeutralButton(
									R.string.utilPopStrDownloadFromBrowser,
									new AlertDialog.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											searchViaBrowser(c, PKG_BUSYBOX);
										}
									})
							.setPositiveButton(
									R.string.utilPopStrDownloadFromMarket,
									new AlertDialog.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											openInMarket(c, PKG_BUSYBOX);
										}
									}).create().show();
					return false;
				}

				new AlertDialog.Builder(c)
						.setTitle(R.string.utilPopStrReboot)
						.setMessage(R.string.utilPopStrRebootReason)
						.setNegativeButton(R.string.utilPopStrNo, null)
						.setPositiveButton(R.string.utilPopStrYes,
								new AlertDialog.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										RootCommands.reboot();
									}
								}).create().show();

			} else {
				Log.v(TAG, "New version Needs copy to System!");

				new AlertDialog.Builder(c)
						.setTitle(R.string.packAppUpdateToSys)
						// "New version Needs copy to System.")
						.setMessage(R.string.packAppUpdateToSysExplain)
						.setOnCancelListener(new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {

								RootCommands.copy(ai.sourceDir, dest);

							}
						})

						.setPositiveButton(R.string.utilPopStrOK,
								new AlertDialog.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										RootCommands.copy(ai.sourceDir, dest);
									}
								}).create().show();
			}

		}

		return true;
	}

	private static ApplicationInfo getApplicationInfo(Context c) {
		try {
			PackageManager manager = c.getPackageManager();
			PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
			return info.applicationInfo;
		} catch (NameNotFoundException e) {
		}
		return null;
	}

	public static void openInMarket(Context c, String packageName) {

		Intent viewIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://search?q=pname:" + packageName));

		try {
			c.startActivity(viewIntent);
		} catch (Exception e) {
			searchViaBrowser(c, packageName);
		}

	}

	private static void searchViaBrowser(Context c, String packageName) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://www.bing.com/search?q=" + packageName));

		try {
			c.startActivity(viewIntent);
		} catch (Exception e2) {
			// do nothing
		}
	}

	static public String getAppTitle(Context c) {
		PackageManager manager = c.getPackageManager();

		try {
			return (String) manager.getApplicationLabel(manager
					.getApplicationInfo(c.getPackageName(), 0));
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	public static String getAppVersionName(Context c) {
		PackageManager manager = c.getPackageManager();

		try {
			PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			return "1.0";
		}
	}

	public static int getAppVersionCode(Context c) {
		PackageManager manager = c.getPackageManager();

		try {
			PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}

}

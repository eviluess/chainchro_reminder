package eviluess.pkg.Utilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class SelfUpdater {

	private static final int UPDATE_ID = 1;
	private static final String PREF_FILENAME = "SelfUpdate";
	private Context context;
	private String url;
	private NoUpdateNotifier onUpdateNoteFound;
	private OnRecommendedDownloadUrlUpdated onRecommendedDownloadUrlUpdated;

	public Preferences preferences;
	protected boolean checkedByUser = false;

	public interface NoUpdateNotifier {
		boolean onUpdateNotFound();
	}

	public interface OnRecommendedDownloadUrlUpdated {
		boolean onRecommendedDownloadUrlUpdated(final String url);
	}

	public void setOnRecommendedDownloadUrlUpdatedNotifier(
			OnRecommendedDownloadUrlUpdated notifier) {
		onRecommendedDownloadUrlUpdated = notifier;
	}

	public void setNoUpdateNotifier(NoUpdateNotifier notifier) {
		onUpdateNoteFound = notifier;
	}

	public SelfUpdater(Context c) {
		context = c;

		preferences = new Preferences(c, PREF_FILENAME);

		loadSettings();
	}

	private void loadSettings() {
		preferences.load();
	}

	private void saveSettings() {
		preferences.save();
	}

	public NoUpdateNotifier defaultNoUpdateNotifier =

	new NoUpdateNotifier() {

		@Override
		public boolean onUpdateNotFound() {
			Toast.makeText(context,
					context.getString(R.string.selfUpdaterUpdateNotFound),
					Toast.LENGTH_SHORT).show();
			return true;
		}
	};

	public class Preferences extends AutoPreferences {

		public boolean[] connections = { true, true, false };
		public int period = 2; // Daily
		public long previousCheckedTime;

		public Preferences(Context context, String filename) {
			super(context, filename);
			// TODO Auto-generated constructor stub
		}

		private static final int VIA_4GPLUS = 0;
		private static final int VIA_3G = 1;
		private static final int VIA_2G = 2;

		private static final int NEVER_CHECK = 0;
		// private static final int ALWAYS_CHECK = 1;
		private static final int CHECK_DAILY = 2;
		private static final int CHECK_PER_3DAYS = 3;
		private static final int CHECK_PER_WEEK = 4;

		public boolean checkExpired() {

			long now = System.currentTimeMillis();
			long duration = now - previousCheckedTime;

			switch (period) {
			case NEVER_CHECK:
				return false;
			case CHECK_DAILY:
				return duration >= 24 * 60 * 60 * 1000;
			case CHECK_PER_3DAYS:
				return duration >= 3 * 24 * 60 * 60 * 1000;
			case CHECK_PER_WEEK:
				return duration >= 7 * 24 * 60 * 60 * 1000;
			default: // case ALWAYS_CHECK:
				return true;
			}

		}

		public boolean connectionMaching(int subtype) {
			switch (subtype) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return connections[VIA_2G];
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return connections[VIA_3G];
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return false;
			}

			return connections[VIA_4GPLUS];
		}

	}

	public boolean checkUpdateScheduled() {
		if (preferences.checkExpired()) {

			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();

			if (wifi == State.CONNECTED) {
				check();
				return true;
			}

			NetworkInfo info = manager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			State mobile = info.getState();

			if (mobile == State.CONNECTED
					&& preferences.connectionMaching(info.getSubtype())) {
				check();
				return true;
			}
		}

		return false;
	}

	public boolean showSetup() {
		new AlertDialog.Builder(context).setTitle(R.string.checkUpdate)
				.setItems(R.array.selfUpdaterOptions, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();

						switch (whichButton) {
						case 0:
							checkedByUser = true;
							check();
							break;
						case 1:
							showPeriodList();
							break;
						case 2:
							showConnectionList();
							break;
						}

					}
				}).create().show();

		return true;
	}

	protected void showConnectionList() {
		new AlertDialog.Builder(context)
				.setTitle(R.string.selfUpdaterUpdateConditions)
				.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						showSetup();
					}
				})
				.setMultiChoiceItems(R.array.selfUpdaterConnectionList,
						preferences.connections,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								preferences.connections[which] = isChecked;
								saveSettings();
							}
						}).create().show();

	}

	protected void showPeriodList() {
		new AlertDialog.Builder(context)
				.setTitle(R.string.selfUpdaterUpdatePeriod)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						showSetup();
					}
				})
				.setSingleChoiceItems(R.array.selfUpdaterPeriodList,
						preferences.period, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								preferences.period = whichButton;
								saveSettings();
							}
						}).create().show();

	}

	private String getUpdateValue(String context, String key, String def) {

		int keypos = context.indexOf(key + "=");

		if (keypos < 0) {
			return def;
		}

		String ret = "";
		keypos += key.length() + 1;

		while (true) {
			char c = context.charAt(keypos);

			if (c == ';')
				return ret;

			ret += c;
			keypos++;
		}
	}

	// Reserved for DkyDrive
	@SuppressWarnings("unused")
	private String getUpdateValue_SkyDrive(String context, String key,
			String def) {

		int keypos = context.indexOf(key + "\\x3d"); // key=

		if (keypos < 0) {
			return def;
		}

		String ret = "";
		keypos += key.length() + 4;

		while (true) {
			char c = context.charAt(keypos);

			if ((c == '\\') && (context.charAt(keypos + 1) == 'x')) {
				String hexStr = context.substring(keypos + 2, keypos + 4);
				int hex = Integer.parseInt(hexStr, 16);
				c = (char) hex;

				if (c == ';')
					return ret;

				keypos += 3;
			}

			ret += c;
			keypos++;
		}
	}

	private class checkThread implements Runnable {

		public void run() {
			doCheck();
		}

	}

	public void check() {

		HandlerThread handlerThread = new HandlerThread("SelfUpdaterThread");
		handlerThread.start();

		new Handler(handlerThread.getLooper()).post(new checkThread());
	}

	@SuppressWarnings("deprecation")
	public void doCheck() {

		preferences.previousCheckedTime = System.currentTimeMillis();

		saveSettings();

		String json = Andrutils.getHtml(url);

		try {
			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();

			int version_code = object.getInt("version_code");

			int current_version_code = PackApp.getAppVersionCode(context);

			if (version_code > current_version_code) {

				String str_version_desc = object.getString("version_desc");

				String str_download = object.getString("download");

				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);

				int icon = R.drawable.update_notification;

				CharSequence tickerText = context
						.getString(R.string.selfUpdaterTicket);

				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText,
						when);

				CharSequence contentTitle = PackApp.getAppTitle(context) + " "
						+ str_version_desc;
				CharSequence contentText = context
						.getString(R.string.selfUpdaterDesc);

				Intent notificationIntent = new Intent(
						"android.intent.action.VIEW", Uri.parse(str_download));

				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 0, notificationIntent, 0);

				notification.defaults |= Notification.DEFAULT_SOUND
						| Notification.DEFAULT_LIGHTS;

				notification.flags |= Notification.FLAG_AUTO_CANCEL;

				notification.setLatestEventInfo(context, contentTitle,
						contentText, contentIntent);

				nm.notify(UPDATE_ID, notification);
			}

			else {
				if (checkedByUser && (onUpdateNoteFound != null))
					onUpdateNoteFound.onUpdateNotFound();

				if (version_code == current_version_code) {

					if (onRecommendedDownloadUrlUpdated != null) {
						onRecommendedDownloadUrlUpdated
								.onRecommendedDownloadUrlUpdated(object
										.getString("download"));
					}
				}

			}

		} catch (Exception e) {
			if (checkedByUser && (onUpdateNoteFound != null))
				onUpdateNoteFound.onUpdateNotFound();
		}

		checkedByUser = false;
	}

	@SuppressWarnings("deprecation")
	public void doCheckHtml() {
		String content = Andrutils.getHtml(url);

		if (null == content)
			return;

		String str_version_code = getUpdateValue(content, "version_code", "0");

		int version_code = Integer.parseInt(str_version_code);

		int current_version_code = PackApp.getAppVersionCode(context);

		if (version_code > current_version_code) {

			String str_version_desc = getUpdateValue(content, "version_desc",
					"");
			String str_download = getUpdateValue(content, "download", "");

			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			int icon = R.drawable.update_notification;

			CharSequence tickerText = context
					.getString(R.string.selfUpdaterTicket);

			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			CharSequence contentTitle = PackApp.getAppTitle(context) + " "
					+ str_version_desc;
			CharSequence contentText = context
					.getString(R.string.selfUpdaterDesc);

			Intent notificationIntent = new Intent(
					"android.intent.action.VIEW", Uri.parse(str_download));

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);

			notification.defaults |= Notification.DEFAULT_SOUND
					| Notification.DEFAULT_LIGHTS;

			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			nm.notify(UPDATE_ID, notification);
		}

	}

	public void setUrl(String url) {
		this.url = url;
	}
}

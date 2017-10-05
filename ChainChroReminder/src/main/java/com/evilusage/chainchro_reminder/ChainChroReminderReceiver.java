package com.evilusage.chainchro_reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class ChainChroReminderReceiver extends BroadcastReceiver {

	private static final String TAG = "CCR.Receiver";
	public static final String SCHEDULE_NEXT_EXPLORER = "com.evilusage.chainchro_reminder.schedule_next_explorer";
	private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";


	private ChainChroReminderPreference preferences;

	HashMap<String, Integer> map = new HashMap<String, Integer>();

	private static final String[] ccPackageNames =
	{
			"com.sega.chainchronicle",
			"com.meiyu.chainchronicle.cn",
			"net.gamon.chainchronicleTW",
			"com.actoz.ChainC" // Korean
	};

	@Override
	public void onReceive(Context context, Intent intent) {

		map.put(ChainChroReminderUtils.ALERT_AP, R.string.apFullSoon);
		map.put(ChainChroReminderUtils.ALERT_DAYBREAK,
				R.string.dayBreakSoon);
		map.put(ChainChroReminderUtils.ALERT_SOUL, R.string.soulFullSoon);
		map.put(ChainChroReminderUtils.ALERT_HALFSOUL,
				R.string.halfSoulFullSoon);
		map.put(ChainChroReminderUtils.ALERT_EXPLORER,
				R.string.explDoneSoon);


		Log.e(TAG, "ChainChroReminderReceiver");

		preferences = new ChainChroReminderPreference(context);

		preferences.load();

		try {

			if (ACTION_BOOT_COMPLETED == intent.getAction())
			{
				long now = Calendar.getInstance().getTime().getTime() / 1000;
				ChainChroReminderUtils utils = new ChainChroReminderUtils(context);

				utils.setAlarms(preferences, now);

				utils.announceAutoScheduled(0);

				try {
					final android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							context);

					mBuilder.setContentTitle(context.getString(R.string.app_name))
							.setContentText(context.getString(R.string.alarmsRecovered)).setAutoCancel(true);
					mBuilder.setSmallIcon(R.drawable.ic_launcher);
					mBuilder.setDefaults(Notification.DEFAULT_SOUND);

					mBuilder.setVibrate(new long[]{0, 800, 200, 300, 100, 300});
					mBuilder.setVisibility(VISIBILITY_PUBLIC);
					mBuilder.setLights(0x00FFFF00, 800, 400);

					final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
							new Intent(context, ChainChroReminderActivity.class), 0);

					mBuilder.setContentIntent(pendingIntent);


					final NotificationManager notificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);

					notificationManager.notify(R.string.alarmsRecovered, mBuilder.build());
				} catch (Exception e) {
					Log.e(TAG, "Cannot Create Notification");
					e.printStackTrace();
				}

			} else if (SCHEDULE_NEXT_EXPLORER == intent.getAction())
			{
				Intent launchIntent = getCCLauncher(context);

				if (launchIntent != null)
				{
					long now = Calendar.getInstance().getTime().getTime() / 1000;

                    if (now >= preferences.exploringDoneTime && preferences.exploringDoneTime >= 0) {

                        preferences.exploringDoneTime = now + (8 * 60 + 0) * 60 + 15;

                        ChainChroReminderUtils utils = new ChainChroReminderUtils(context);

                        utils.createAlarm(preferences.exploringDoneTime, ChainChroReminderUtils.ALERT_EXPLORER, now);

                        utils.announceAutoScheduled(preferences.exploringDoneTime);

                        preferences.save();
                    }


					context.startActivity(launchIntent);
				}

				return;
			}
		} catch (Exception e) {
			Log.e(TAG, "Cannot Create Notification");
			e.printStackTrace();
		}

		int id;
		
		try {
			id = map.get(intent.getAction());

			final android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context);

			mBuilder.setContentTitle(context.getString(R.string.app_name))
					.setContentText(context.getString(id)).setAutoCancel(true);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);
			mBuilder.setDefaults(Notification.DEFAULT_SOUND);

			mBuilder.setVibrate(new long[]{0, 800, 200, 300, 100, 300});
			mBuilder.setVisibility(VISIBILITY_PUBLIC);
			mBuilder.setLights(0x00FFFF00, 800, 400);

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(SCHEDULE_NEXT_EXPLORER), 0);

            mBuilder.setContentIntent(pendingIntent);


			final NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify(id, mBuilder.build());


		} catch (Exception e) {
			Log.e(TAG, "Cannot Create Notification");
			e.printStackTrace();
		}

	}

	private Intent getCCLauncher(Context context) {

		for (String pkgName :  ccPackageNames)
		{
			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(
					pkgName);

			if (launchIntent != null) {
				return launchIntent;
			}
		}

		return null;
	}

}

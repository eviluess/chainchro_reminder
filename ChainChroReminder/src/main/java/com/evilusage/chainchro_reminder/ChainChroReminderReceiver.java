package com.evilusage.chainchro_reminder;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ChainChroReminderReceiver extends BroadcastReceiver {

	private static final String TAG = "ChainChroReminderReceiver";
	public static final String SCHEDULE_NEXT_EXPLORER = "com.evilusage.chainchro_reminder.schedule_next_explorer";

	private ChainChroReminderPreference preferences;

	private long now;

	HashMap<String, Integer> map = new HashMap<String, Integer>();

	private final String[] ccPackageNames =
	{
			"com.sega.chainchronicle",
			"com.meiyu.chainchronicle.cn",
			"net.gamon.chainchronicleTW",
			"com.actoz.ChainC" // Korean
	};

	private void createAlarm(Context context, long time, String alert) {

		if (time - now <= 0)
			return;

		Intent alarmIntent = new Intent(context,
				ChainChroReminderReceiver.class);

		alarmIntent.setAction(alert);

		PendingIntent mAlarmSender = PendingIntent.getBroadcast(context, 0,
				alarmIntent, 0);

		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (time - now) * 1000, mAlarmSender);

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		map.put(ChainChroReminderActivity.ALERT_AP, R.string.apFullSoon);
		map.put(ChainChroReminderActivity.ALERT_DAYBREAK,
				R.string.dayBreakSoon);
		map.put(ChainChroReminderActivity.ALERT_SOUL, R.string.soulFullSoon);
		map.put(ChainChroReminderActivity.ALERT_HALFSOUL,
				R.string.halfSoulFullSoon);
		map.put(ChainChroReminderActivity.ALERT_EXPL,
				R.string.explDoneSoon);


		Log.e(TAG, "ChainChroReminderReceiver");

		preferences = new ChainChroReminderPreference(context);

		preferences.load();

		try {

			if (intent.getAction() == SCHEDULE_NEXT_EXPLORER)
			{

				Intent launchIntent = getCCLauncher(context);

				if (launchIntent != null)
				{
					now = Calendar.getInstance().getTime().getTime() / 1000;
					preferences.exploringDoneTime = now + (8*60+1) * 60 + 15;

					createAlarm(context, preferences.exploringDoneTime, ChainChroReminderActivity.ALERT_EXPL);

					preferences.save();

					context.startActivity(launchIntent);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Cannot Create Noti.");
			e.printStackTrace();
		}

		int id = -1;
		
		try {
			id = map.get(intent.getAction());

			final android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context);

			mBuilder.setContentTitle(context.getString(R.string.app_name))
					.setContentText(context.getString(id)).setAutoCancel(true);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);
			mBuilder.setDefaults(Notification.DEFAULT_SOUND);

			mBuilder.setVibrate(new long[]{0, 800, 200, 300, 100, 300});
			mBuilder.setLights(0x00FFFF00, 800, 400);

			if (id == R.string.explDoneSoon) {
				final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
						new Intent(SCHEDULE_NEXT_EXPLORER), 0);

				mBuilder.setContentIntent(pendingIntent);
			} else	{

				Intent launchIntent = getCCLauncher(context);

				if (launchIntent != null)
				{
					final PendingIntent pendingIntent = PendingIntent.getActivity(
							context, 0, launchIntent, 0);

					mBuilder.setContentIntent(pendingIntent);

				}

			}

			final NotificationManager notiman = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			notiman.notify(0, mBuilder.build());


		} catch (Exception e) {
			Log.e(TAG, "Cannot Create Noti.");
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

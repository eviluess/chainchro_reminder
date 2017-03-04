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
					long now = Calendar.getInstance().getTime().getTime() / 1000;

                    if (now >= preferences.exploringDoneTime && preferences.exploringDoneTime >= 0) {

                        preferences.exploringDoneTime = now + (8 * 60 + 0) * 60 + 15;

                        ChainChroReminderUtils utils = new ChainChroReminderUtils(context);

                        utils.setNow(now);

                        utils.createAlarm(preferences.exploringDoneTime, ChainChroReminderActivity.ALERT_EXPL);

                        preferences.save();
                    }

					context.startActivity(launchIntent);
				}

				return;
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

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(SCHEDULE_NEXT_EXPLORER), 0);

            mBuilder.setContentIntent(pendingIntent);


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

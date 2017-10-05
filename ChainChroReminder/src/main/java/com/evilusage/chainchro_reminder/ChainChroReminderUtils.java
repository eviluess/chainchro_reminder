package com.evilusage.chainchro_reminder;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Date;

public class ChainChroReminderUtils {

    private Context context = null;


    public static final String ALERT_AP = "com.evilusage.chainchro_reminder.alert_ap";
    public static final String ALERT_DAYBREAK = "com.evilusage.chainchro_reminder.alert_brave";
    public static final String ALERT_SOUL = "com.evilusage.chainchro_reminder.alert_soul";
    public static final String ALERT_HALFSOUL = "com.evilusage.chainchro_reminder.alert_halfsoul";
    public static final String ALERT_EXPLORER = "com.evilusage.chainchro_reminder.alert_explorer";

    public static final int SOUL_AHEAD = (int)(4.5*60);

    public ChainChroReminderUtils(Context context)
    {
        this.context = context;
    }

    public void announceAutoScheduled(long time)
    {
        if (time == 0) {
            Toast.makeText(context, R.string.alarmsRecovered, Toast.LENGTH_LONG).show();
        }
        else {
            Date future = new Date();
            future.setTime(time * 1000);

            Toast.makeText(context,  String.format(
                    context.getString(R.string.explAutoScheduled), future.toLocaleString()), Toast.LENGTH_LONG).show();

        }

    }

    public void createAlarm(long time, String alert, long now) {

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

    public void setAlarms(ChainChroReminderPreference preferences, long now)
    {
        setAlarms(preferences, now, 7);
    }

    public void setAlarms(ChainChroReminderPreference preferences, long now, int soul) {

        createAlarm(preferences.apFullTime, ALERT_AP, now);

        createAlarm(preferences.dayBreakTime, ALERT_DAYBREAK, now);

        if (preferences.soulFullTime < now && soul < 6)
        {
            createAlarm(now + 4, ALERT_SOUL, now);
        }
        else
        {
            createAlarm(preferences.soulFullTime, ALERT_SOUL, now);
        }

        createAlarm(preferences.soulFullTime + SOUL_AHEAD - 30 * 60 * 3, ALERT_HALFSOUL, now);

        createAlarm(preferences.exploringDoneTime, ALERT_EXPLORER, now);
    }
}

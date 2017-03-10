package com.evilusage.chainchro_reminder;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ChainChroReminderUtils {

    private Context context = null;

    public ChainChroReminderUtils(Context context)
    {
        this.context = context;
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
}

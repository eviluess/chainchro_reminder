package com.evilusage.chainchro_reminder;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/5.
 */

// http://www.cnblogs.com/allin/archive/2010/05/11/1732200.html

public class ChainChroReminderListActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      //  this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        SimpleAdapter adapter = new SimpleAdapter(this, getData(),
                R.layout.content_chainchro_reminder_list,
                new String[]{"time", "ap", "exploring", "soul", "brave"},
                new int[]{R.id.time, R.id.ap, R.id.exploring, R.id.soul, R.id.brave});
        setListAdapter(adapter);

    }

    private List<Map<String, Object>> getData() {

        ChainChroReminderPreference preferences = new ChainChroReminderPreference(this);

        preferences.load();

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("time", getString(R.string.time));
        map.put("ap", getString(R.string.ap));
        map.put("exploring", getString(R.string.exploring));
        map.put("soul", getString(R.string.soul));
        map.put("brave", getString(R.string.brave));
        list.add(map);

        long now = Calendar.getInstance().getTime().getTime() / 1000;

        int apOld = -1;

        // 3700
        for (int i=0;i<=60*24;i++)
        {
            boolean dirtyTime = false;

            now += 60;

            int apRemain = (int) (preferences.apFullTime - now) / 60;
            int ap = preferences.apTotal - (apRemain + 7) / 8;
            int minutesToNextAP = apRemain % 8;

            dirtyTime |= (ap != apOld) && (minutesToNextAP >= 0);
            apOld = ap;

            int explSeconds = (int)(preferences.exploringDoneTime - now);

            dirtyTime |= (explSeconds>=0) && (explSeconds < 3*60);

            int soulDuration = (int) (6 * 30 * 60 - (preferences.soulFullTime - now + ChainChroReminderUtils.SOUL_AHEAD )) / 60;

            dirtyTime |= (soulDuration>=0) && (soulDuration < 3);

            int braveDuration = (int) (6 * 30 * 60 - (preferences.dayBreakTime - now)) / 60;

            dirtyTime |= (braveDuration>=0) && (braveDuration < 3);

            if (dirtyTime)
            {
                map = new HashMap<String, Object>();

                Date future = new Date();
                future.setTime(now * 1000);

                map.put("time", String.format("%02d:%02d", future.getHours(), future.getMinutes()));

                map.put("ap", String.format("%d/%d", ap, minutesToNextAP));

                explSeconds /= 60;

                if (explSeconds < 0)
                    explSeconds = 0;

                map.put("exploring", String.format("%d:%02d",
                        explSeconds / 60, explSeconds - (explSeconds / 60) *60));

                map.put("soul", String.format("%d/%02d",
                        soulDuration / 30, 30 - (soulDuration - soulDuration / 30 * 30)));

                map.put("brave", String.format("%d/%02d",
                        braveDuration / 30, 30 - (braveDuration - braveDuration / 30 * 30)));

                list.add(map);

            }
        }

        return list;
    }
}

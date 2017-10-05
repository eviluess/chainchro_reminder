package com.evilusage.chainchro_reminder;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
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

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        SimpleAdapter adapter = new SimpleAdapter(this, getData(),
                R.layout.content_chainchro_reminder_list,
                new String[]{"time", "ap", "exploring", "soul", "brave"},
                new int[]{R.id.time, R.id.ap, R.id.exploring, R.id.soul, R.id.brave});
        setListAdapter(adapter);

    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (int i=0;i<10;i++)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("time", "12:99");
            map.put("ap", "20/4");
            map.put("exploring", "4:23");
            map.put("soul", "3/13");
            map.put("brave", "4/20");
            list.add(map);
        }


        return list;
    }
}

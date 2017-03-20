package com.evilusage.chainchro_reminder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import eviluess.pkg.Utilities.AfterTaste;
import eviluess.pkg.Utilities.LocalizedPath;
import eviluess.pkg.Utilities.PackApp;
import eviluess.pkg.Utilities.SelfUpdater;


public class ChainChroReminderActivity extends AppCompatActivity {

	public static final String ALERT_AP = "com.evilusage.chainchro_reminder.alert_ap";
	public static final String ALERT_DAYBREAK = "com.evilusage.chainchro_reminder.alert_brave";
	public static final String ALERT_SOUL = "com.evilusage.chainchro_reminder.alert_soul";
	public static final String ALERT_HALFSOUL = "com.evilusage.chainchro_reminder.alert_halfsoul";
	public static final String ALERT_EXPLORER = "com.evilusage.chainchro_reminder.alert_explorer";

	private static final String TAG = "CCR.Activity";
	private static final int SOUL_AHEAD = (int)(4.5*60);

	private ChainChroReminderPreference preferences;

	private AfterTaste afterTaste;

	private SelfUpdater updater;

	private static final String downloadUrl =
			"https://raw.githubusercontent.com/eviluess/chainchro_reminder/master/release/ChainChroReminder-%s.apk";

	private static final String updateUrl =
			"https://raw.githubusercontent.com/eviluess/chainchro_reminder/master/release/updateinfo.json";

	private static final String homepageUrl =
			"https://github.com/eviluess/chainchro_reminder/wiki";

	private static final String homepageUrlFormatted = homepageUrl + "/%s";

	private static final String homepageCacheUrl =
			"https://raw.githubusercontent.com/eviluess/chainchro_reminder/master/release/wiki.lan";

	private boolean isHomepageAssigned = false;

	private ChainChroReminderUtils utils = new ChainChroReminderUtils(this);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chainchro_reminder_main);

		assignCallbacks(getScrollView());

		preferences = new ChainChroReminderPreference(this);
		preferences.load();

		afterTaste = new AfterTaste(this);

		afterTaste.setDefaultDownloadUrl(String.format(downloadUrl,
				PackApp.getAppVersionName(this)));

		isHomepageAssigned = false;

		updater = new SelfUpdater(this);
		updater.setUrl(updateUrl);

		updater.setOnRecommendedDownloadUrlUpdatedNotifier(new SelfUpdater.OnRecommendedDownloadUrlUpdated() {

			@Override
			public boolean onRecommendedDownloadUrlUpdated(String url) {
				afterTaste.setDefaultDownloadUrl(url);
				return true;
			}
		});

		updater.checkUpdateScheduled();

		updateParams();

	}

	private void updateParams() {

		putIntToViewById(R.id.etApTotal, preferences.apTotal);

		long now = Calendar.getInstance().getTime().getTime() / 1000;

		if (preferences.apFullTime == 0) {
			putTextToViewById(R.id.etAP, "0~4");
			putTextToViewById(R.id.etMinutesToNextAP, "0~7");
		} else {
			int apRemain = (int) (preferences.apFullTime - now) / 60;
			int ap = preferences.apTotal - (apRemain + 7) / 8;
			int minutesToNextAP = apRemain % 8;
			putIntToViewById(R.id.etAP, ap);
			putIntToViewById(R.id.etMinutesToNextAP, minutesToNextAP);
		}

		putTimeToViewById(R.id.tvAPFullTime, preferences.apFullTime);

		if (preferences.dayBreakTime == 0) {
			putTextToViewById(R.id.etDayBreak, "0~5");
			putTextToViewById(R.id.etDayBreakMinutes, "0~29");
		}
		else
		{
			int braveDuration = (int) (6 * 30 * 60 - (preferences.dayBreakTime - now)) / 60;

			putIntToViewById(R.id.etDayBreak, braveDuration / 30);
			putIntToViewById(R.id.etDayBreakMinutes,
					30 - (braveDuration - braveDuration / 30 * 30));
		}

		putTimeToViewById(R.id.tvDayBreakTime, preferences.dayBreakTime);

		if (preferences.soulFullTime == 0) {
			putTextToViewById(R.id.etSoul, "0~5");
			putTextToViewById(R.id.etSoulRemain, "0~29");
		}
		else
		{
			int soulDuration = (int) (6 * 30 * 60 - (preferences.soulFullTime - now + SOUL_AHEAD )) / 60;

			putIntToViewById(R.id.etSoul, soulDuration / 30);
			putIntToViewById(R.id.etSoulRemain,
					30 - (soulDuration - soulDuration / 30 * 30));
		}

		putTimeToViewById(R.id.tvSoulFullTime, preferences.soulFullTime);

		putTimeToViewById(R.id.tvHalfSoulFullTime,
				preferences.soulFullTime + SOUL_AHEAD - 30 * 60 * 3);

		if (preferences.exploringDoneTime == 0) {
			putTextToViewById(R.id.etERHours, "0~7");
			putTextToViewById(R.id.etERMinutes, "0~59");
		}
		else
		{
			int exclMinutes = (int)(preferences.exploringDoneTime - now);

			if (exclMinutes < 0)
			{
				preferences.exploringDoneTime = now + (7*60+59) * 60 + 3;

				createAlarm(preferences.exploringDoneTime, ChainChroReminderActivity.ALERT_EXPLORER, now);

				preferences.save();

				exclMinutes = (int)(preferences.exploringDoneTime - now);

				utils.announceAutoScheduled(preferences.exploringDoneTime);
			}

			exclMinutes /= 60;

			putIntToViewById(R.id.etERHours, exclMinutes / 60 );
			putIntToViewById(R.id.etERMinutes, exclMinutes - (exclMinutes / 60) *60);
		}

		putTimeToViewById(R.id.tvExploringDoneTime, preferences.exploringDoneTime);

	}

    private void putTextToViewById(int id, final String text) {

		TextView tv = (TextView) findViewById(id);

		tv.setText(text);
	}

	@SuppressWarnings("deprecation")
	private void putTimeToViewById(int id, long time) {
		Date future = new Date();
		future.setTime(time * 1000);

		TextView tv = (TextView) findViewById(id);

		tv.setText(future.toLocaleString());

	}

	@Override
	protected void onResume() {

		super.onResume();

		updateParams();
		selectAllOnFocused(getScrollView());
	}

	private ViewGroup getScrollView() {
		return ((ViewGroup) findViewById(R.id.scrollView));
	}

	private void selectAllOnFocused(ViewGroup vg) {

		int children = vg.getChildCount();

		while (children-- > 0) {
			View child = vg.getChildAt(children);

			if (child instanceof ViewGroup) {
				selectAllOnFocused((ViewGroup) child);
			} else {
				if (child.isFocused()) {
					if (child instanceof EditText) {
						Log.d(TAG, child.toString() + " SelectAll");
						((EditText) child).selectAll();
						return;
					}
				}

			}

		}

	}

	private void assignCallbacks(ViewGroup vg) {

		int children = vg.getChildCount();

		while (children-- > 0) {
			View child = vg.getChildAt(children);

			if (child instanceof ViewGroup) {
				assignCallbacks((ViewGroup) child);
			} else {
				if (child instanceof CheckBox) {
					continue;
				}
				if (child instanceof Button) {
					child.setOnClickListener(onButtonClickListener);
				}

			}

		}

	}

	private boolean executeById(int id)
	{
		switch (id) {

			case R.id.btnSet: {
				setAlarm();
				return true;
			}
			case R.id.btnAfterTaste: {

				if (!isHomepageAssigned)
				{
					ArrayList<String> cacheList = LocalizedPath.getCacheListFromUrl(homepageCacheUrl);

					if (cacheList != null)
					{
						afterTaste.setDefaultHomepage(new LocalizedPath(homepageUrlFormatted,
								LocalizedPath.LOWERCASE_FILENAME,
								cacheList,
								null)
								.createLocalizedUrl());

						isHomepageAssigned = true;
					}
					else
					{
						afterTaste.setDefaultHomepage(
								new LocalizedPath(homepageUrl, null, null, null)
										.createLocalizedUrl());
					}
				}

				afterTaste.showRecommendedChoices();

				return true;
			}
			case R.id.btnCheckUpdate: {
				updater.setNoUpdateNotifier(updater.defaultNoUpdateNotifier);
				updater.showSetup();
				return true;
			}
		}

		return false;
	}

	private OnClickListener onButtonClickListener = new OnClickListener() {

		public void onClick(View v) {

			executeById(v.getId());
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chain_chro_reminder_main, menu);

		return true;
	}

	private void setAlarm() {

		preferences.apTotal = getIntFromViewId(R.id.etApTotal, 1);

		int ap = getIntFromViewId(R.id.etAP, 0);
		int minutesToNextAP = getIntFromViewId(R.id.etMinutesToNextAP, 1);

		long now = Calendar.getInstance().getTime().getTime() / 1000 + 2;

		preferences.apFullTime = now
				+ ((preferences.apTotal - ap - 1) * 8 + minutesToNextAP) * 60;

		int dayBreak = getIntFromViewId(R.id.etDayBreak, 0);
		int dayBreakMinutes = getIntFromViewId(R.id.etDayBreakMinutes, 0);

		preferences.dayBreakTime = now + (dayBreakMinutes + (5 - dayBreak) * 30) * 60 - 8;

		int soul = getIntFromViewId(R.id.etSoul, 0);
		int soulRemain = getIntFromViewId(R.id.etSoulRemain, 0);

		preferences.soulFullTime = now + (soulRemain + (5 - soul) * 30 ) * 60 - SOUL_AHEAD - 8;

		int expHours = getIntFromViewId(R.id.etERHours, 7);
		int expMinutes = getIntFromViewId(R.id.etERMinutes, 58);

		preferences.exploringDoneTime = now + (expHours*60+expMinutes) * 60;

		createAlarm(preferences.apFullTime, ALERT_AP, now);

		createAlarm(preferences.dayBreakTime, ALERT_DAYBREAK, now);

		if (preferences.soulFullTime < now)
		{
			createAlarm(now + 4, ALERT_SOUL, now);
		}
		else
		{
			createAlarm(preferences.soulFullTime, ALERT_SOUL, now);
		}

		createAlarm(preferences.soulFullTime + SOUL_AHEAD - 30 * 60 * 3, ALERT_HALFSOUL, now);

		createAlarm(preferences.exploringDoneTime, ALERT_EXPLORER, now);

		preferences.save();
		updateParams();
	}

	private void createAlarm(long time, String alert, long now) {

		utils.createAlarm(time, alert, now);
	}

	private int getIntFromViewId(int id, int defVal) {
		EditText text = (EditText) findViewById(id);

		try {
			return Integer.parseInt(text.getText().toString());
		} catch (Exception e) {
			return defVal;
		}
	}

	private void putIntToViewById(int id, int value) {
		EditText text = (EditText) findViewById(id);

		try {
			if (value < 0)
				value = 0;
			text.setText(((Integer) value).toString());
		} catch (Exception e) {
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return executeById(id) || super.onOptionsItemSelected(item);

	}

}

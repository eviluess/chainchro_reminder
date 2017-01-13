package com.evilusage.chainchro_reminder;

import java.util.Calendar;
import java.util.Date;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import eviluess.pkg.Utilities.Andrutils;
import eviluess.pkg.Utilities.AfterTaste;
import eviluess.pkg.Utilities.Andrutils.SkippableTipListener;
import eviluess.pkg.Utilities.LocalizedPath;
import eviluess.pkg.Utilities.SelfUpdater;
import eviluess.pkg.Utilities.PackApp;

public class ChainChroReminderActivity extends ActionBarActivity {

	public static final String ALERT_AP = "com.evilusage.chainchro_reminder.ALERT_AP";
	public static final String ALERT_DAYBREAK = "com.evilusage.chainchro_reminder.ALERT_DAYBREAK";
	public static final String ALERT_SOUL = "com.evilusage.chainchro_reminder.ALERT_SOUL";
	public static final String ALERT_HALFSOUL = "com.evilusage.chainchro_reminder.ALERT_HALFSOUL";
	public static final String ALERT_EXPL = "com.evilusage.chainchro_reminder.ALERT_EXPL";
	protected static final String TAG = "ChainChroReminderActivity";

	private ChainChroReminderPreference preferences;

	private AfterTaste afterTaste;

	private SelfUpdater updater;

	private static final String downloadUrl =
			"https://raw.githubusercontent.com/eviluess/chainchro_reminder/master/release/ChainChroReminder-%s.apk";

	private static final String updateUrl =
			"https://raw.githubusercontent.com/eviluess/chainchro_reminder/master/release/updateinfo.json";

	private static final String homepageUrl =
			"https://github.com/eviluess/chainchro_reminder/wiki/Chain-Chronicle-Reminder";

	private static final String donateUrl = "https://www.paypal.me/Eviluess/1usd";

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

		afterTaste.setDefaultDonateUrl(new LocalizedPath(donateUrl).createLocalizedUrl());

		afterTaste.setDefaultHomepage(new LocalizedPath(homepageUrl,
				LocalizedPath.LOWERCASE_FILENAME, null, null)
				.createLocalizedUrl());

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

		UpdateParams();

	}

	private void UpdateParams() {

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

		int dayBreakRemain = (int) (preferences.dayBreakTime - now) / 60;

		if (dayBreakRemain < 0) {
			dayBreakRemain = 0;
		}

		putIntToViewById(R.id.etDayBreak, dayBreakRemain);

		putTimeToViewById(R.id.tvDayBreakTime, preferences.dayBreakTime);

		if (preferences.soulFullTime == 0) {
			putTextToViewById(R.id.etSoul, "0~5");
			putTextToViewById(R.id.etSoulRemain, "0~29");
		}
		else
		{
			int soulDuration = (int) (6 * 30 * 60 - (preferences.soulFullTime - now)) / 60;

			putIntToViewById(R.id.etSoul, soulDuration / 30);
			putIntToViewById(R.id.etSoulRemain,
					30 - (soulDuration - soulDuration / 30 * 30));
		}


		putTimeToViewById(R.id.tvSoulFullTime, preferences.soulFullTime);

		putTimeToViewById(R.id.tvHalfSoulFullTime,
				preferences.soulFullTime - 30 * 60 * 3);

		int explMinites = (int) (preferences.exploringDoneTime - now) / 60;

		putIntToViewById(R.id.etERHours, explMinites / 60 );
		putIntToViewById(R.id.etERMinutes, explMinites - (explMinites / 60) *60);

		putTimeToViewById(R.id.tvExploringDoneTime, preferences.exploringDoneTime);

	}

	private void putTextToViewById(int id, int text) {
		Andrutils.putTextToViewById(this, id, text);
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

		UpdateParams();
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

	private OnClickListener onButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.btnSet: {
				setAlarm();
				break;
			}
			case R.id.btnAfterTaste: {
				afterTaste.showRecommendedChoices();
				break;
			}
			case R.id.btnCheckUpdate: {
				updater.setNoUpdateNotifier(updater.defaultNoUpdateNotifier);
				updater.showSetup();
				break;
			}
			}
		}
	};
	private long now;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chain_chro_reminder_main, menu);
		return true;
	}

	protected void setAlarm() {

		preferences.apTotal = getIntFromViewId(R.id.etApTotal, 1);

		int ap = getIntFromViewId(R.id.etAP, 0);
		int minutesToNextAP = getIntFromViewId(R.id.etMinutesToNextAP, 1);

		now = Calendar.getInstance().getTime().getTime() / 1000;

		preferences.apFullTime = now
				+ ((preferences.apTotal - ap - 1) * 8 + minutesToNextAP) * 60;

		int dayBreak = getIntFromViewId(R.id.etDayBreak, 0);
		preferences.dayBreakTime = now + dayBreak * 60;

		int soul = getIntFromViewId(R.id.etSoul, 0);
		int soulRemain = getIntFromViewId(R.id.etSoulRemain, 0);

		preferences.soulFullTime = now + (soulRemain + (5 - soul) * 30) * 60;

		int expHours = getIntFromViewId(R.id.etERHours, 7);
		int expMinutes = getIntFromViewId(R.id.etERMinutes, 58);

		preferences.exploringDoneTime = now + (expHours*60+expMinutes) * 60;

		createAlarm(preferences.apFullTime, ALERT_AP);

		createAlarm(preferences.dayBreakTime, ALERT_DAYBREAK);

		createAlarm(preferences.soulFullTime, ALERT_SOUL);

		createAlarm(preferences.soulFullTime - 30 * 60 * 3, ALERT_HALFSOUL);

		createAlarm(preferences.exploringDoneTime, ALERT_EXPL);

		preferences.save();
		UpdateParams();
	}

	private void createAlarm(long time, String alert) {

		if (time - now <= 0)
			return;

		Intent alarmIntent = new Intent(this,
				ChainChroReminderReceiver.class);

		alarmIntent.setAction(alert);

		PendingIntent mAlarmSender = PendingIntent.getBroadcast(this, 0,
				alarmIntent, 0);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (time - now) * 1000, mAlarmSender);

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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

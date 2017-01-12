package com.evilusage.chainchro_reminder;

import android.content.Context;
import eviluess.pkg.Utilities.AutoPreferences;

public class ChainChroReminderPreference extends AutoPreferences {

	private static final String FILENAME = "ChainChroReminder";

	public int apTotal = 5;
	public long apFullTime = 0;
	public long dayBreakTime = 0;
	public long soulFullTime = 0;
	public long exploringDoneTime = 0;

	public ChainChroReminderPreference(Context context) {
		super(context, FILENAME);
	}

}

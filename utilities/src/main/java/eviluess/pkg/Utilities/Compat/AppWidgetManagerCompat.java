package eviluess.pkg.Utilities.Compat;

import java.lang.reflect.Method;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

class AppWidgetManagerCompat3 implements
		AppWidgetManagerCompat.AppWidgetManagerInterfaces {

	private static final String TAG = "AppWidgetManagerCompat";

	protected Context context;

	protected AppWidgetManager awm;

	private boolean bindFailed = false;

	public AppWidgetManagerCompat3(Context context) {
		this.context = context;
		awm = AppWidgetManager.getInstance(context);
	}

	@Override
	public boolean bindAppWidgetIdIfAllowed(int appWidgetId,
			ComponentName provider) {

		bindFailed = false;

		Method m = null;

		try {
			m = AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed",
					new Class[] { Integer.TYPE, ComponentName.class });
		} catch (NoSuchMethodException e) {

			try {
				m = AppWidgetManager.class.getMethod("bindAppWidgetId",
						new Class[] { Integer.TYPE, ComponentName.class });
			} catch (NoSuchMethodException e2) {
				bindFailed = true;
				Log.v(TAG, "bindAppWidgetId Doesn't Exist: " + e2.toString());
			}
			if (m != null) {
				try {
					m.invoke(awm, appWidgetId, provider);
				} catch (Exception e3) {
					bindFailed = true;
					Log.v(TAG, "bindAppWidgetId Failed: " + e3.toString());

				}

				return true;
			}

		}
		if (m != null) {
			try {
				return (Boolean) m.invoke(awm, appWidgetId, provider);
			} catch (Exception e) {
				bindFailed = true;
				Log.v(TAG, "bindAppWidgetIdIfAllowed Failed: " + e.toString());
			}
		}

		return true;
	}

	@Override
	public Bundle getAppWidgetOptions(int appWidgetId) {
		Method m = null;

		try {
			m = AppWidgetManager.class.getMethod("getAppWidgetOptions",
					new Class[] { Integer.TYPE });
		} catch (NoSuchMethodException e) {

		}
		if (m != null) {
			try {
				return (Bundle) m.invoke(awm, appWidgetId);
			} catch (Exception e) {
			}
		}

		return null;
	}

	@Override
	public void updateAppWidgetOptions(int appWidgetId, Bundle options) {

		if (options == null)
			return;

		Method m = null;

		try {
			m = AppWidgetManager.class.getMethod("updateAppWidgetOptions",
					new Class[] { Integer.TYPE, Bundle.class });
		} catch (NoSuchMethodException e) {

		}
		if (m != null) {
			try {
				m.invoke(awm, appWidgetId, options);
			} catch (Exception e) {
			}
		}

	}

	@Override
	public boolean supportUserBind() {
		return !bindFailed;
	}

	@Override
	public AppWidgetManager getUnresolved() {
		return awm;
	}

	@Override
	public boolean bindAppWidgetId(int appWidgetId, ComponentName provider) {

		Method m = null;

		try {
			m = AppWidgetManager.class.getMethod("bindAppWidgetId",
					new Class[] { Integer.TYPE, ComponentName.class });
		} catch (NoSuchMethodException e) {

			return bindAppWidgetIdIfAllowed(appWidgetId, provider);

		}
		if (m != null) {
			try {
				return (Boolean) m.invoke(awm, appWidgetId, provider);
			} catch (Exception e) {
				Log.v(TAG, "bindAppWidgetId Failed:" + e.toString());
				return bindAppWidgetIdIfAllowed(appWidgetId, provider);
			}
		}

		return true;
	}

}

class AppWidgetManagerCompat16 extends AppWidgetManagerCompat3 {

	@Override
	public boolean supportUserBind() {
		return true;
	}

	public AppWidgetManagerCompat16(Context context) {
		super(context);
	}

	@Override
	public boolean bindAppWidgetIdIfAllowed(int appWidgetId,
			ComponentName provider) {

		return awm.bindAppWidgetIdIfAllowed(appWidgetId, provider);
	}

	@Override
	public Bundle getAppWidgetOptions(int appWidgetId) {
		return awm.getAppWidgetOptions(appWidgetId);
	}

	@Override
	public void updateAppWidgetOptions(int appWidgetId, Bundle options) {
		awm.updateAppWidgetOptions(appWidgetId, options);
	}

}

public class AppWidgetManagerCompat {

	public interface AppWidgetManagerInterfaces {

		public AppWidgetManager getUnresolved();

		public boolean supportUserBind();

		public boolean bindAppWidgetId(int appWidgetId, ComponentName provider);

		public boolean bindAppWidgetIdIfAllowed(int appWidgetId,
				ComponentName provider);

		public Bundle getAppWidgetOptions(int appWidgetId);

		void updateAppWidgetOptions(int appWidgetId, Bundle options);
	}

	public AppWidgetManagerInterfaces compat;

	@SuppressWarnings("deprecation")
	public AppWidgetManagerCompat(Context c) {

		int sdkv = Integer.parseInt(Build.VERSION.SDK);

		if (sdkv >= 16)
			compat = new AppWidgetManagerCompat16(c);
		else
			compat = new AppWidgetManagerCompat3(c);

	}

}

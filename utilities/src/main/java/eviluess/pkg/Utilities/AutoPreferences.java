package eviluess.pkg.Utilities;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AutoPreferences {
	protected String filename;
	protected Context context;

	public AutoPreferences(Context context, final String filename) {
		this.context = context;
		this.filename = filename;
	}

	final public void save() {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				filename, Context.MODE_MULTI_PROCESS).edit();

		onSave(editor);

		editor.commit();
	}

	protected void onSave(Editor editor) {

		Field[] fields = getClass().getFields();

		try {

			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];

				if (Modifier.isFinal(f.getModifiers())) {
					continue;
				}

				Class<?> clz = f.getType();

				String name = f.getName();

				if (clz.isArray()) {

					clz = clz.getComponentType();

					Object obj = f.get(this);

					for (int n = 0; n < Array.getLength(obj); n++) {

						if (clz == boolean.class) {
							editor.putBoolean(name + "-" + n,
									Array.getBoolean(obj, n));
						} else if (clz == int.class) {
							editor.putInt(name + "-" + n, Array.getInt(obj, n));
						} else if (clz == float.class) {
							editor.putFloat(name + "-" + n,
									Array.getFloat(obj, n));
						} else if (clz == long.class) {
							editor.putLong(name + "-" + n,
									Array.getLong(obj, n));
						} else if (clz == String.class) {
							editor.putString(name + "-" + n,
									(String) Array.get(obj, n));
						}
					}

				} else {
					if (clz == boolean.class) {
						editor.putBoolean(name, f.getBoolean(this));
					} else if (clz == int.class) {
						editor.putInt(name, f.getInt(this));
					} else if (clz == float.class) {
						editor.putFloat(name, f.getFloat(this));
					} else if (clz == long.class) {
						editor.putLong(name, f.getLong(this));
					} else if (clz == String.class) {
						editor.putString(name, (String) f.get(this));
					}
				}

			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	final public void load() {
		SharedPreferences prefs = context.getSharedPreferences(filename,
				Context.MODE_MULTI_PROCESS);

		onLoad(prefs);
	}

	protected void onLoad(SharedPreferences prefs) {

		Field[] fields = getClass().getFields();

		try {

			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];

				if (Modifier.isFinal(f.getModifiers())) {
					continue;
				}

				Class<?> clz = f.getType();

				String name = f.getName();

				if (clz.isArray()) {

					clz = clz.getComponentType();

					Object obj = f.get(this);

					for (int n = 0; n < Array.getLength(obj); n++) {

						if (clz == boolean.class) {

							Array.setBoolean(
									obj,
									n,
									prefs.getBoolean(name + "-" + n,
											Array.getBoolean(obj, n)));
						} else if (clz == int.class) {
							Array.setInt(
									obj,
									n,
									prefs.getInt(name + "-" + n,
											Array.getInt(obj, n)));
						} else if (clz == float.class) {
							Array.setFloat(
									obj,
									n,
									prefs.getFloat(name + "-" + n,
											Array.getFloat(obj, n)));
						} else if (clz == long.class) {
							Array.setLong(
									obj,
									n,
									prefs.getLong(name + "-" + n,
											Array.getLong(obj, n)));
						} else if (clz == String.class) {
							Array.set(obj, n, prefs.getString(name + "-" + n,
									(String) Array.get(obj, n)));
						}
					}

				} else {

					if (clz == boolean.class) {
						f.setBoolean(this,
								prefs.getBoolean(name, f.getBoolean(this)));
					} else if (clz == int.class) {
						f.setInt(this, prefs.getInt(name, f.getInt(this)));
					} else if (clz == float.class) {
						f.setFloat(this, prefs.getFloat(name, f.getFloat(this)));
					} else if (clz == long.class) {
						f.setLong(this, prefs.getLong(name, f.getLong(this)));
					} else if (clz == String.class) {
						f.set(this, prefs.getString(name, (String) f.get(this)));
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

}

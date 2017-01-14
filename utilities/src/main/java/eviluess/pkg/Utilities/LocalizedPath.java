package eviluess.pkg.Utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class LocalizedPath {

	public LocalizedPath createLocalizedUrl() {
		return setPathChecker(new PathChecker() {

			@Override
			public boolean check(final String path) {
				return Andrutils.checkUrl(path);
			}
		});
	}

	public interface PathChecker {
		public boolean check(final String path);
	}

	interface ElementFetcher {
		public String fetch();
	}

	private class LocalFunction {

		public Locale locale;
		public ElementFetcher elementFetcher;

		char elementCode;
		private String data;

		public LocalFunction(Locale lc) {
			locale = lc;
			data = "";
		}

		boolean construct(char c) {

			if (elementFetcher == null) {
				switch (c) {
				case 'C':
				case 'L':

					elementCode = c;
					return true;
				case '2':
					if (elementCode == 'L') {

						elementFetcher = new ElementFetcher() {

							@Override
							public String fetch() {
								return locale.getLanguage();
							}
						};

						return createData('I');
					}
					// 'C'
					elementFetcher = new ElementFetcher() {

						@Override
						public String fetch() {
							return locale.getCountry();
						}
					};
					return true;
				case '3':
					if (elementCode == 'L') {

						elementFetcher = new ElementFetcher() {

							@Override
							public String fetch() {
								return locale.getISO3Language();
							}
						};

						return createData('I');
					} // 'C'

					elementFetcher = new ElementFetcher() {

						@Override
						public String fetch() {
							return locale.getISO3Country();
						}
					};
					return true;
				case '4':
					if (elementCode == 'C') {

						elementFetcher = new ElementFetcher() {

							@Override
							public String fetch() {

								if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
									return "Hans";
								}

								if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
									return "Hant";
								}

								return locale.getCountry();
							}
						};
					}

					return true;
				case 'V':
					elementFetcher = new ElementFetcher() {

						@Override
						public String fetch() {
							return locale.getVariant();
						}
					};

					return true;
				}
			} else {

				switch (c) {
				case 'L':
				case 'U':
				case 'I':
				case 'N':
					return createData(c);

				}
			}

			return true;
		}

		private boolean createData(char c) {
			if (elementFetcher != null) {
				data = elementFetcher.fetch();

				switch (c) {
				case 'U':
					data = data.toUpperCase(Locale.US);
					break;
				case 'L':
					data = data.toLowerCase(Locale.US);
					break;
				case 'N':
					data = data.substring(0, 1).toUpperCase(Locale.US)
							+ data.substring(1).toLowerCase(Locale.US);
					break;
				}
				return false;
			}

			return true;
		}

		public String toString() {
			return data;
		}
	}

	/*
	 * format desc: $$: $
	 * 
	 * $L: Language
	 * 
	 * 2: ISO 639-1 (2 letters)
	 * 
	 * 3: ISO3
	 * 
	 * $C: Country
	 * 
	 * 2: ISO 3166-1-alpha-2
	 * 
	 * 3: ISO3
	 * 
	 * 4: GoogleCode-Style
	 * 
	 * U: Upper Case
	 * 
	 * L: Lower Case
	 * 
	 * I: Ignore Case
	 * 
	 * N: Normal
	 * 
	 * $V: Variant
	 * 
	 * U/L/I/N: --> $C
	 *
	 * $[ XXX $]: When the checker failed, remove these text to match again (match language only)
	 */

	public static final String GOOGLECODE_WIKI = "?wl=$L2$[-$C4I$]"; // ?wl=zh-Hans
	public static final String ANDROID_VALUE = "$L2$[-r$C2I$]"; // zh-rTW
	public static final String ANDROID_WIKI_VALUE = "?wl=$L2$[-r$C2I$]"; // ?wl=-zh-rTW
	public static final String LOCALE_STRING = "$L2_$C2I$[_$VI$]"; // en__POSIX
	public static final String LOWERCASE_FILENAME = "$L2$[_$C2L$]"; // zh_tw

	private String path;
	private String localePattern;
	private ArrayList<String> cacheList;
	private PathChecker checker;

	public String getLocalizedPath() {
		if (localePattern == null)
			return path;

		char[] pattern = localePattern.toCharArray();

		return getLocalizedPathByPattern(pattern, true);
	}

	private String getLocalizedPathByPattern(final char[] patternOriginal, boolean full) {

		boolean ignoreBegin = false;

		String patternComposer = "";

		int i = 0;

		while (i < patternOriginal.length ) {
			char c = patternOriginal[i];

			if (c == '$') {
				if (patternOriginal[i + 1] == '[') {
					ignoreBegin = true;
					i += 2;
					continue;
				}

				if (patternOriginal[i + 1] == ']') {
					ignoreBegin = false;
					i += 2;
					continue;
				}

			}

			if (full || !ignoreBegin)
			{
				patternComposer += c;
			}

			i++;
		}

		final char[] pattern = patternComposer.toCharArray();

		final Locale lc = Locale.getDefault();
		int pIndex = 0;

		String[] prefixes = { "", "", "" };
		LocalFunction[] functions = new LocalFunction[3];

		i = 0;

		while (i < pattern.length && pIndex < 3) {
			char c = pattern[i];

			if (c == '$') {
				if (pattern[i + 1] == '$') {
					prefixes[pIndex] += '$';
					i += 2;
					continue;
				}

				functions[pIndex] = new LocalFunction(lc);

				i++;

				while (functions[pIndex].construct(pattern[i])) {
					i++;
				}

				i++;
				pIndex++;

				continue;
			} else {
				prefixes[pIndex] += c;
				i++;
			}
		}

		while (pIndex-- > 0) {

			i = pIndex;
			String ret = "";

			do {

				ret = functions[i].toString() + ret;

				if (!ret.equals("")) {
					ret = prefixes[i] + ret;
				}

			} while (--i >= 0);

			if (cacheList != null && cacheList.size() > 0) {

				String first = cacheList.get(0);
				boolean mapping = first.equals("default");

				if (mapping) {

					int total = cacheList.size() / 2;

					for (i=0;i<total;i++) // i is safe to be re-used here
					{
						if (ret.equals(cacheList.get(i*2))) {
							return String.format(path, cacheList.get(i*2+1));
						}

					}


				} else
				{
					Iterator<String> it = cacheList.iterator();

					while (it.hasNext()) {
						if (ret.equals(it.next())) {
							{
								return String.format(path, ret);
							}
						}
					}

				}

			}
			else
			{
				ret = String.format(path, ret);

				if (checker.check(ret)) {
					return ret;
				}
			}
		}

		String ret = String.format(path, "default");

		if (cacheList != null && cacheList.size() > 0) {
			String first = cacheList.get(0);
			boolean mapping = first.equals("default");

			if (mapping)
			{
				ret = String.format(path, cacheList.get(1));
			}
		}

		if (checker.check(ret)) {
			return ret;
		}

		ret = String.format(path, "");

		if (checker.check(ret)) {
			return ret;
		}

		return null;

	}

	public LocalizedPath(final String pathFormatted, final String localeFormat,
			final ArrayList<String> localeCache, final PathChecker pathChecker) {
		path = pathFormatted;
		localePattern = localeFormat;
		cacheList = localeCache;

		checker = pathChecker;
	}

	public LocalizedPath() {

	}

	public LocalizedPath(final String unpatternedPath) {
		path = unpatternedPath;
		localePattern = null;
		cacheList = null;

		checker = null;
	}

	public LocalizedPath setPathFormatted(final String pathFormatted) {
		path = pathFormatted;
		return this;
	}

	public LocalizedPath setLocaleFormat(final String localeFormat) {
		localePattern = localeFormat;
		return this;
	}

	public LocalizedPath setLocaleCache(final ArrayList<String> localeCache) {
		cacheList = localeCache;
		return this;
	}

	public LocalizedPath setPathChecker(final PathChecker pathChecker) {
		checker = pathChecker;
		return this;
	}

	public static ArrayList<String> getCacheListFromUrl(final String url) {
		return Andrutils.getHtmlLines(url, 3000);
	}

	public static ArrayList<String> getCacheListFromArray(final String[] array) {

		if (array == null)
			return null;

		ArrayList<String> ret = new ArrayList<String>();

		for (int i = 0; i < array.length; i++) {
			ret.add(array[i]);
		}

		if (ret.isEmpty())
			return null;

		return ret;
	}

}

/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class DateUtils {


	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

	private static SimpleDateFormat createFormat() {
		SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
		format.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		return format;
	}

	private static SimpleDateFormat dateInFormat = createFormat();
	private static SimpleDateFormat dateOutFormat = createFormat();
	private static SimpleDateFormat localDateFormat = new SimpleDateFormat(DATE_PATTERN);

	public static String formatDate(Date date) {
		if(date==null) {
			return null;
		}
		synchronized (dateOutFormat) {
			return dateOutFormat.format(date);
		}
	}

	public static String formatLocalDate(Date date) {
		if(date==null) {
			return null;
		}
		synchronized (localDateFormat) {
			return localDateFormat.format(date);
		}
	}

	public static String formatDuration(long time) {
		if(time<=0)
			return null;

		long s = time/1000;
		long m = s/60;
		long h = m/60;
		long d = h/24;

		s = s%60;
		m = m%60;
		h = h%24;

		StringBuilder sb = new StringBuilder();
		if(d>0) {
			sb.append(' ').append(d).append('D');
		}
		if(h>0) {
			sb.append(' ').append(h).append('H');
		}
		if(m>0) {
			sb.append(' ').append(m).append('M');
		}
		if(s>0) {
			sb.append(' ').append(s).append('S');
		}

		StringUtil.trim(sb);

		return sb.length()==0 ? "<1S" : sb.toString();
	}

	public static long getTime(Date date) {
		return date==null ? 0L : date.getTime();
	}

	public static Date parseDate(String s) throws ParseException {
		synchronized (dateInFormat) {
			return dateInFormat.parse(s);
		}
	}
}

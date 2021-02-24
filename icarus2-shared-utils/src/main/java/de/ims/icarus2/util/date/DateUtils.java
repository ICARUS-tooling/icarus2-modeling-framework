/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import javax.annotation.Nullable;

import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class DateUtils {


	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

	private static DateTimeFormatter createFormat() {
		return DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZoneId.of("UTC"));
	}

	private static DateTimeFormatter dateInFormat = createFormat();
	private static DateTimeFormatter dateOutFormat = createFormat();
	private static DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN);

	public static Optional<String> formatDate(@Nullable TemporalAccessor temporal) {
		if(temporal==null) {
			return Optional.empty();
		}
		synchronized (dateOutFormat) {
			return Optional.of(dateOutFormat.format(temporal));
		}
	}

	public static Optional<String> formatLocalDate(@Nullable TemporalAccessor temporal) {
		if(temporal==null) {
			return Optional.empty();
		}
		synchronized (localDateFormat) {
			return Optional.of(localDateFormat.format(temporal));
		}
	}

	public static Optional<String> formatDuration(long time) {
		if(time<=0)
			return Optional.empty();

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

		return Optional.of(sb.length()==0 ? "<1S" : sb.toString());
	}

	public static LocalDateTime parseDate(String s) throws DateTimeParseException {
		synchronized (dateInFormat) {
			return dateInFormat.parse(s, LocalDateTime::from);
		}
	}
}

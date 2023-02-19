/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.util.strings;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.strings.StringUtil.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class VariableResolver {

	private final Map<String, String> lookup = new Object2ObjectOpenHashMap<>();

	private static final char DOLLAR = '$';
	private static final char OPEN = '{';
	private static final char CLOSE = '}';

	private final Matcher matcher = Pattern.compile("\\$\\{([A-Za-z0-9.-:_]+)\\}").matcher("");

	public void set(String key, String value) {
		checkNotEmpty(key);
		requireNonNull(value);
		lookup.put(key, value);
	}

	public void unset(String key) {
		checkNotEmpty(key);
		lookup.remove(key);
	}

	public void unset(String key, String value) {
		checkNotEmpty(key);
		lookup.remove(key, value);
	}

	/**
	 *
	 * @param key
	 * @return
	 * @see #expand(String)
	 */
	public String resolve(String key) {
		checkNotEmpty(key);
		String value = lookup.get(key);
		if(value == null) {
			value = System.getProperty(key);
		}
		if(value == null) {
			value = System.getenv(key);
		}
		if(value == null) {
			value = key;
		}
		return value;
	}

	/**
	 * Expands variables in the given input string with the following
	 * strategy for resolving individual keys:
	 * <b>
	 * <ol>
	 * <li>If a value has been {@link #set(String, String) set explicitly}, use it.</li>
	 * <li>If a {@link System#getProperty(String) system property} has been set, use it.</li>
	 * <li>Otherwise resort to {@link System#getenv(String) environment variables}.</li>
	 * </ol>
	 * If none of the above yields a result, the input string is returned.
	 * If the input string is {@code null} or empty, it is returned as is. The same goes
	 * for strings that are too short to be resolved anyway.
	 *
	 * @param s
	 * @return
	 */
	public @Nullable String expand(String s) {
		if(isNullOrEmpty(s)) {
			return s;
		}
		int len = s.length();
		if(len<4) {
			return s;
		}

		StringBuilder sb = null;
		int lastAppendPosition = 0;

		matcher.reset(s);
		while(matcher.find()) {
			if(sb==null) {
				sb = new StringBuilder(s.length()*2);
			}
			String value = resolve(matcher.group(1));
	        // Append the intervening text
	        sb.append(s, lastAppendPosition, matcher.start());
	        // Append the resolved variable content
	        sb.append(value);

	        lastAppendPosition = matcher.end();
		}
		if(sb==null) {
			return s;
		}
        sb.append(s, lastAppendPosition, len);
		return sb.toString();
	}
}

/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus Gärtner
 *
 */
public class Options extends HashMap<String, Object> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6318648432239062316L;

	/**
	 * An immutable and empty option instances.
	 */
	public static final Options NONE = new Options() {

		/**
		 *
		 */
		private static final long serialVersionUID = -6172790615021617955L;

		@Override
		public Object put(String key, Object value) {
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}
	};

	public static Options of(String key, Object value) {
		return new Options().set(key, value);
	}

	public Options() {
		// no-op
	}

	public Options(Options source) {
		putAll(source);
	}

	public Options(Map<String, Object> source) {
		putAll(source);
	}

	public Options(Object... args) {
		putAll(args);
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> O get(String key, O defaultValue) {
		Object value = get(key);
		return value==null ? defaultValue : (O) value;
	}

	public Object getOptional(String key, Object defaultValue) {
		Object value = get(key);
		return value==null ? defaultValue : value;
	}

	public Options set(String key, Object value) {
		put(key, value);
		return this;
	}

	@Override
	public Object put(String key, Object value) {
		if(value==null) {
			return remove(key);
		}

		return super.put(key, value);
	}

	public void putAll(Object... args) {
		if (args == null || args.length % 2 != 0) {
			return;
		}

		for (int i = 0; i < args.length; i += 2) {
			put(String.valueOf(args[i]), args[i + 1]);
		}
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		// Silently fail when argument is null
		if(m==null) {
			return;
		}

		super.putAll(m);
	}

	public void dump(PrintStream out) {
		out.println("Options: "); //$NON-NLS-1$
		for(Entry<String, Object> entry : entrySet())
			out.printf("  -key=%s value=%s%n",  //$NON-NLS-1$
					entry.getKey(), String.valueOf(entry.getValue()));
	}

	public Object firstSet(String...keys) {
		Object value = null;

		for(String key : keys) {
			if((value = get(key)) != null) {
				break;
			}
		}

		return value;
	}

	public int getInteger(String key, int defaultValue) {
		Object result = get(key);
		if(result instanceof String) {
			try {
				result = _int(Integer.parseInt((String) result));
			} catch(NumberFormatException e) {
				// ignore
			}
		}

		return result instanceof Number ? ((Number)result).intValue() : defaultValue;
	}

	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	public long getLong(String key, long defaultValue) {
		Object result = get(key);
		if(result instanceof String) {
			try {
				result = _long(Long.parseLong((String) result));
			} catch(NumberFormatException e) {
				// ignore
			}
		}

		return result instanceof Number ? ((Number)result).longValue() : defaultValue;
	}

	public long getLong(String key) {
		return getLong(key, 0L);
	}

	public double getDouble(String key, double defaultValue) {
		Object result = get(key);
		if(result instanceof String) {
			try {
				result = _double(Double.parseDouble((String) result));
			} catch(NumberFormatException e) {
				// ignore
			}
		}

		return result instanceof Number ? ((Number)result).doubleValue() : defaultValue;
	}

	public double getDouble(String key) {
		return getDouble(key, 0d);
	}

	public float getFloat(String key, float defaultValue) {
		Object result = get(key);
		if(result instanceof String) {
			try {
				result = _float(Float.parseFloat((String) result));
			} catch(NumberFormatException e) {
				// ignore
			}
		}

		return result instanceof Number ? ((Number)result).floatValue() : defaultValue;
	}

	public float getFloat(String key) {
		return getFloat(key, 0f);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Object result = get(key);
		if(result instanceof String) {
			try {
				result = _boolean(Boolean.parseBoolean((String) result));
			} catch(NumberFormatException e) {
				// ignore
			}
		}

		return result instanceof Boolean ? ((Boolean)result).booleanValue() : defaultValue;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	@Override
	public Options clone() {
		return (Options) super.clone();
	}

	// Collection of commonly used option keys

	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String LABEL = "label"; //$NON-NLS-1$
	public static final String TITLE = "title"; //$NON-NLS-1$
	public static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	public static final String CONVERTER = "converter"; //$NON-NLS-1$
	public static final String CONTEXT = "context"; //$NON-NLS-1$
	public static final String LOCATION = "location"; //$NON-NLS-1$
	public static final String LANGUAGE = "language"; //$NON-NLS-1$
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String FILTER = "filter"; //$NON-NLS-1$
	public static final String EXTENSION = "extension"; //$NON-NLS-1$
	public static final String PLUGIN = "plugin"; //$NON-NLS-1$
	public static final String DATA = "data"; //$NON-NLS-1$
	public static final String OWNER = "owner"; //$NON-NLS-1$
	public static final String INDEX = "index"; //$NON-NLS-1$
}

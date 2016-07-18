/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.registry;

import java.util.function.BiConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface MetadataRegistry {

	void open();

	String getValue(String key);

	default byte getByteValue(String key, byte noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Byte.parseByte(value);
	}

	default short getShortValue(String key, short noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Short.parseShort(value);
	}

	default int getIntValue(String key, int noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Integer.parseInt(value);
	}

	default long getLongValue(String key, long noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Long.parseLong(value);
	}

	default float getFloatValue(String key, float noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Float.parseFloat(value);
	}

	default double getDoubleValue(String key, double noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Double.parseDouble(value);
	}

	default boolean getBooleanValue(String key, boolean noEntryValue) {
		String value = getValue(key);

		return value==null ? noEntryValue : Boolean.parseBoolean(value);
	}

	/**
	 * Changes the entry for the given {@code key} so that it maps to the
	 * specified {@code value}. Providing a value of {@code null} indicates that
	 * the mapping for {@code key} should be erased from the registry.
	 *
	 * @param key
	 * @param value
	 */
	void setValue(String key, String value);

	default void setByteValue(String key, byte value) {
		setValue(key, String.valueOf(value));
	}

	default void setShortValue(String key, short value) {
		setValue(key, String.valueOf(value));
	}

	default void setIntValue(String key, int value) {
		setValue(key, String.valueOf(value));
	}

	default void setLongValue(String key, long value) {
		setValue(key, String.valueOf(value));
	}

	default void setFloatValue(String key, float value) {
		setValue(key, String.valueOf(value));
	}

	default void setDoubleValue(String key, double value) {
		setValue(key, String.valueOf(value));
	}

	default void setBooleanValue(String key, boolean value) {
		setValue(key, String.valueOf(value));
	}

	default void changeByteValue(String key, byte value, byte noEntryValue) {
		if(value==noEntryValue) {
			setValue(key, null);
		} else {
			setByteValue(key, value);
		}
	}

	default void changeShortValue(String key, short value, byte noEntryValue) {
		if(value==noEntryValue) {
			setValue(key, null);
		} else {
			setShortValue(key, value);
		}
	}

	default void changeIntValue(String key, int value, int noEntryValue) {
		if(value==noEntryValue) {
			setValue(key, null);
		} else {
			setIntValue(key, value);
		}
	}

	default void changeLongValue(String key, long value, long noEntryValue) {
		if(value==noEntryValue) {
			setValue(key, null);
		} else {
			setLongValue(key, value);
		}
	}

	default void changeFloatValue(String key, float value, float noEntryValue) {
		if(Float.compare(value, noEntryValue)==0) {
			setValue(key, null);
		} else {
			setFloatValue(key, value);
		}
	}

	default void changeDoubleValue(String key, double value, double noEntryValue) {
		if(Double.compare(value, noEntryValue)==0) {
			setValue(key, null);
		} else {
			setDoubleValue(key, value);
		}
	}

	default void changeBooleanValue(String key, boolean value, boolean noEntryValue) {
		if(value==noEntryValue) {
			setValue(key, null);
		} else {
			setBooleanValue(key, value);
		}
	}

	void beginUpdate();

	void endUpdate();

	void delete();

	void close();

	void forEachEntry(BiConsumer<? super String, ? super String> action);

	default void forEachEntry(String prefix, BiConsumer<? super String, ? super String> action) {
		forEachEntry((key, value) -> {
			if(key.startsWith(prefix)) {
				action.accept(key, value);
			}
		});
	}
}

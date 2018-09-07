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
package de.ims.icarus2.model.api.registry;

import java.util.function.BiConsumer;

/**
 * Models a storage for maintenance data of various framework members.
 *
 *
 * @author Markus Gärtner
 *
 */
public interface MetadataRegistry extends AutoCloseable {

	/**
	 * Initialize the connection to this registry and
	 * prepare it for usage.
	 */
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
	 * <p>
	 * Note that implementations should make sure that a lone call to this or
	 * any of the more specialized {@code setXXX(String,xxx)} methods triggers
	 * a full transaction cycle via the {@link #beginUpdate()} and {@link #endUpdate()}
	 * methods!
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

	default void changeShortValue(String key, short value, short noEntryValue) {
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

	/**
	 * Starts a transaction.
	 * <p>
	 * The actual transaction semantics are implementation specific.
	 */
	void beginUpdate();

	/**
	 * Ends a transaction and potentially persists the changes into whatever backing storage is used.
	 */
	void endUpdate();

	/**
	 * Erases the current content of this registry permanently.
	 */
	void delete();

	//FIXME check if we should expand the method signature to throw a general Exception
	@Override
	void close();

	/**
	 * Executes the given {@code action} for all entries in this registry in unspecified order.
	 * @param action
	 */
	void forEachEntry(BiConsumer<? super String, ? super String> action);

	/**
	 * Executes the given {@code action} for all entries in this registry whose {@code keys} start
	 * with the specified {@code prefix}. The default implementation delegates to {@link #forEachEntry(BiConsumer)}
	 * using a specialized {@link BiConsumer} and checking for each raw entry if its key is a valid candidate.
	 * Subclasses are encouraged to provide a more efficient solution!
	 *
	 * @param prefix
	 * @param action
	 */
	default void forEachEntry(String prefix, BiConsumer<? super String, ? super String> action) {
		forEachEntry((key, value) -> {
			if(key.startsWith(prefix)) {
				action.accept(key, value);
			}
		});
	}
}

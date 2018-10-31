/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class IcarusUtils {

	public static final Runnable NO_OP = () -> {/* no-op */};

	/**
	 * Maximum value for use in arrays.
	 * Some VMs reserve a couple bytes as array headers and as such
	 * {@link Integer#MAX_VALUE} would result in an exception.
	 */
	public static final long MAX_INTEGER_INDEX = Integer.MAX_VALUE-8;

	public static String toLoggableString(Object value) {
		//TODO ensure the generated string is short enough and does not contain line breaks
		return String.valueOf(value);
	}

	/**
	 * Checks if the provided {@code value} exceeds {@link #MAX_INTEGER_INDEX}.
	 * Throws {@link IcarusException} with code {@link GlobalErrorCode#INDEX_OVERFLOW}
	 * if that's the case and otherwise returns the supplied value.
	 *
	 * @param value
	 * @return
	 */
	public static int ensureIntegerValueRange(long value) {
		if(value>MAX_INTEGER_INDEX)
			throw new IcarusException(GlobalErrorCode.INDEX_OVERFLOW, "Not a legal value in integer range: "+value);

		return (int) value;
	}

	public static int limitToIntegerValueRange(long value) {
		return (int) Math.min(MAX_INTEGER_INDEX, value);
	}

	public static void close(Object obj) throws Exception {
		if(obj instanceof Closeable) {
			((Closeable)obj).close();
		} else if(obj instanceof AutoCloseable) {
			((AutoCloseable)obj).close();
		}
	}

	public static void closeSilently(Object obj) {
		if(obj instanceof Closeable) {
			try {
				((Closeable)obj).close();
			} catch (IOException e) {
				// ignore
			}
		} else if(obj instanceof AutoCloseable) {
			try {
				((AutoCloseable)obj).close();
			} catch (Exception e) {
				//ignore
			}
		}
	}

	public static void close(Object obj, Logger log, String label) {
		try {
			close(obj);
		} catch(Exception e) {
			log.error("Failed to close {1}", label, e);
		}
	}

	/**
	 * Value representing an unset long variable (-1L).
	 */
	public static final long UNSET_LONG = -1L;
	/**
	 * Value representing an unset int variable (-1).
	 */
	public static final int UNSET_INT = -1;
	/**
	 * Value representing an unset double variable (-1D).
	 */
	public static final double UNSET_DOUBLE = -1D;
	/**
	 * Value representing an unset float variable (-1F).
	 */
	public static final float UNSET_FLOAT = -1F;

	@SafeVarargs
	public static <V extends Object> Optional<V> or(Optional<V>...optionals) {
		for(Optional<V> optional : optionals) {
			if(optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	@SafeVarargs
	public static <V extends Object> Optional<V> filter(Supplier<? extends Optional<V>>...suppliers) {
		for(Supplier<? extends Optional<V>> supplier : suppliers) {
			Optional<V> optional = supplier.get();
			if(optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	public static boolean allPresent(Optional<?>...optionals) {
		return Arrays.asList(optionals).stream().allMatch(Optional::isPresent);
	}

	public static boolean nonePresent(Optional<?>...optionals) {
		return Arrays.asList(optionals).stream().noneMatch(Optional::isPresent);
	}

	public static <V extends Object> boolean equals(Optional<V> opt, V value) {
		return opt.isPresent() && value.equals(opt.get());
	}
}

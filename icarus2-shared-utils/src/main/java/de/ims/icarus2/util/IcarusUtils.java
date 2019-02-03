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
package de.ims.icarus2.util;

import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.Mutable.MutableObject;

/**
 * @author Markus Gärtner
 *
 */
public final class IcarusUtils {

	public static final Runnable NO_OP = () -> {/* no-op */};

	public static final Consumer<Object> DO_NOTHING = x -> { /* no-op */};

	/**
	 * Returns {@code true} if the JVM currently in use is reportedly
	 * using a 64 bit architecture model.
	 *
	 * @return
	 */
	public static final boolean IS_64BIT_VM;

	/**
	 * Size of object header.
	 */
	public static final int OBJ_HEADER_BYTES;

	/**
	 * Size of object header for arrays.
	 */
	public static final int ARRAY_HEADER_BYTES;

	/**
	 * Estimated size of a reference pointer under the current JVM settings.
	 * <p>
	 * This value represents a best guess, as it is not possible via public
	 * APIs to consistently access this information from within a running
	 * VM without additional externla help.
	 * <p>
	 * The returned value will be either {@code 4} or {@code 8}.
	 */
	public static final int OBJ_REF_SIZE;

	/**
	 * Maximum value for use in arrays.
	 * Some VMs reserve a couple bytes as array headers and as such
	 * {@link Integer#MAX_VALUE} would result in an exception.
	 */
	public static final int MAX_INTEGER_INDEX = Integer.MAX_VALUE-8;

	static {
		int objHeaderBytes = 16;
		int arrayHeaderOverhead = 8;
		int refSize = 4;

		IS_64BIT_VM = Optional.ofNullable(System.getProperty("sun.arch.data.model"))
				.orElse(System.getProperty("os.arch"))
				.contains("64");

		if(IS_64BIT_VM) {
			refSize = 8;
		}

		boolean compressedOop = false; // -XX:UseCompressedOops
		boolean compressedClassesPointer = false; // -XX:UseCompressedClassesPointers

		try {
			@SuppressWarnings("unchecked")
			Class<PlatformManagedObject> beanClazz =
					(Class<PlatformManagedObject>) Class.forName("com.sun.management.HotSpotDiagnosticMXBean");

			// Fetch MXBean of the HotSpot VM
			PlatformManagedObject vmBean = ManagementFactory.getPlatformMXBean(beanClazz);

			Method getVMOptionMethod = beanClazz.getMethod("getVMOption", String.class);
			try {
				Object vmOption = getVMOptionMethod.invoke(vmBean, "UseCompressedOops");
				compressedOop = Boolean.parseBoolean(vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
			} catch (ReflectiveOperationException | RuntimeException e) {
				compressedOop = false;
			}
			try {
				Object vmOption = getVMOptionMethod.invoke(vmBean, "UseCompressedClassesPointers");
				compressedOop = Boolean.parseBoolean(vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
			} catch (ReflectiveOperationException | RuntimeException e) {
				compressedClassesPointer = false;
			}
		} catch (RuntimeException | ReflectiveOperationException e) {
			// ignore
		}

		if(compressedOop) {
			refSize = 4;
		}

		OBJ_HEADER_BYTES = objHeaderBytes;
		ARRAY_HEADER_BYTES = OBJ_HEADER_BYTES + arrayHeaderOverhead;
		OBJ_REF_SIZE = refSize;
	}

	public static String toLoggableString(Object value) {
		//TODO ensure the generated string is short enough and does not contain line breaks
		return String.valueOf(value);
	}

	/**
	 * Checks if the provided {@code value} exceeds {@link #MAX_INTEGER_INDEX}.
	 * Throws {@link IcarusRuntimeException} with code {@link GlobalErrorCode#INDEX_OVERFLOW}
	 * if that's the case and otherwise returns the supplied value.
	 *
	 * @param value
	 * @return
	 */
	public static int ensureIntegerValueRange(long value) {
		if(value>MAX_INTEGER_INDEX)
			throw new IcarusRuntimeException(GlobalErrorCode.INDEX_OVERFLOW, "Not a legal value in integer range: "+value);

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

	public static <T extends Object> Predicate<? super T> notEq(T target) {
		return item -> !item.equals(target);
	}

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

	public static <R> R extractSupplied(Consumer<Consumer<? super R>> method) {
		MutableObject<R> buffer = new MutableObject<>();

		method.accept(buffer::set);

		return buffer.get();
	}

	public static <T> void consumeIfAble(T data, Consumer<? super T> action) {
		if(action!=null) {
			action.accept(data);
		}
	}
}

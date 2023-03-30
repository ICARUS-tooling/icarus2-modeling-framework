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
package de.ims.icarus2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * Allows multiple exception that occurred in a common context to be collected and
 * thrown together. This is useful for example when a manager that holds several closable
 * components is shutting down and needs to at least attempt a close operation of each
 * independent component. In a typical sequential approach the entire shutdown would break
 * when the first exception from a component's close method is thrown, leaving subsequent
 * components unclosed, despite them potentially having the ability to close without errors.
 * Using the {@link AccumulatingException.Buffer} utility class an application can collect
 * exceptions from multiple sequential or parallel operations and then throw a single
 * exception which combines all the others.
 *
 * @author Markus Gärtner
 *
 */
public class AccumulatingException extends IcarusApiException {

	private static final long serialVersionUID = 8864281967856686141L;

	public static Buffer buffer() {
		return new Buffer();
	}

	private final Throwable[] exceptions;

	private static final Throwable[] EMPTY = {};

	public AccumulatingException(Buffer buffer) {
		super(GlobalErrorCode.ACCUMULATED_ERRORS, buffer.getMessage());

		this.exceptions = buffer.getExceptions();
	}

	public AccumulatingException(String message, Throwable...exceptions) {
		super(GlobalErrorCode.ACCUMULATED_ERRORS, message);

		this.exceptions = exceptions==null ? EMPTY : exceptions.clone();
	}

	public AccumulatingException(String message, Collection<Throwable> exceptions) {
		super(GlobalErrorCode.ACCUMULATED_ERRORS, message);

		this.exceptions = exceptions==null ? EMPTY : CollectionUtils.toArray(exceptions, Throwable[]::new);
	}

	public int getExceptionCount() {
		return exceptions.length;
	}

	public Throwable getExceptionAt(int index) {
		return exceptions[index];
	}

	//TODO think about overriding the printStackTrace methods to output all accumulated exceptions

	public void forEachException(Consumer<? super Throwable> action) {

		for(Throwable t : exceptions) {
			action.accept(t);
		}
	}

	public <T extends Throwable> Collection<T> getExceptions(Predicate<T> filter) {
		LazyCollection<T> result = LazyCollection.lazyList();

		for(Throwable t : exceptions) {
			@SuppressWarnings("unchecked")
			T exception = (T) t;
			if(filter.test(exception)) {
				result.add(exception);
			}
		}

		return result.getAsList();
	}

	public <T extends Throwable> Collection<T> getExceptionsOfType(Class<T> clazz) {
		return getExceptions(t -> clazz.isInstance(t));
	}

	/**
	 *
 	 * Not thread-safe! (for use in multi-threaded environments use external synchronization!)
 	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Buffer implements Consumer<Throwable> {
		private String message;
		private List<Throwable> exceptions;

		public AccumulatingException toException() {
			return new AccumulatingException(this);
		}

		public String getMessage() {
			return message;
		}

		public Buffer setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the message for this buffer, using the given format
		 * and the number of exceptions currently stored as parameters
		 * for {@link String#format(String, Object...) formatting}.
		 *
		 * @param format
		 */
		public Buffer setFormattedMessage(String format, Object... args) {
			this.message = String.format(format, args);
			return this;
		}

		public boolean isEmpty() {
			return exceptions==null || exceptions.isEmpty();
		}

		public int getExceptionCount() {
			return exceptions==null ? 0 : exceptions.size();
		}

		public Buffer addException(Throwable t) {
			if(exceptions==null) {
				exceptions = new ArrayList<>();
			}

			exceptions.add(t);
			return this;
		}

		public Buffer addExceptionsFrom(AccumulatingException e) {
			e.forEachException(this);
			return this;
		}

		public Buffer addExceptions(Throwable...ex) {
			if(exceptions==null) {
				exceptions = new ArrayList<>();
			}

			CollectionUtils.feedItems(exceptions, ex);
			return this;
		}

		public Buffer addExceptions(Collection<? extends Throwable> ex) {
			if(exceptions==null) {
				exceptions = new ArrayList<>();
			}

			exceptions.addAll(ex);
			return this;
		}

		public Throwable[] getExceptions() {
			Throwable[] result = EMPTY;

			if(exceptions!=null) {
				result = exceptions.toArray(EMPTY);
			}

			return result;
		}

		/**
		 * @see java.util.function.Consumer#accept(java.lang.Object)
		 */
		@Override
		public void accept(Throwable t) {
			addException(t);
		}
	}
}

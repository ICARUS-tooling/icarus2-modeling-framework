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
package de.ims.icarus2.util;

import static de.ims.icarus2.util.classes.Primitives._int;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
public class AccumulatingException extends Exception {

	private static final long serialVersionUID = 8864281967856686141L;

	private final Throwable[] exceptions;

	private static final Throwable[] EMPTY = {};

	public AccumulatingException(Buffer buffer) {
		super(buffer.getMessage());

		this.exceptions = buffer.getExceptions();
	}

	public AccumulatingException(String message, Throwable...exceptions) {
		super(message);

		this.exceptions = exceptions==null ? EMPTY : exceptions.clone();
	}

	public int getExceptionCount() {
		return exceptions.length;
	}

	public Throwable getExceptionAt(int index) {
		return exceptions[index];
	}

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

		public void setMessage(String message) {
			this.message = message;
		}

		/**
		 * Sets the message for this buffer, using the given format
		 * and the number of exceptions currently stored as parameters
		 * for {@link String#format(String, Object...) formatting}.
		 *
		 * @param format
		 */
		public void setFormattedMessage(String format, Object... args) {
			this.message = String.format(format, _int(getExceptionCount()), args);
		}

		public boolean isEmpty() {
			return exceptions==null || exceptions.isEmpty();
		}

		public int getExceptionCount() {
			return exceptions==null ? 0 : exceptions.size();
		}

		public void addException(Throwable t) {
			if(exceptions==null) {
				exceptions = new ArrayList<>();
			}

			exceptions.add(t);
		}

		public void addExceptionsFrom(AccumulatingException e) {
			e.forEachException(this);
		}

		public void addExceptions(Throwable...ex) {
			if(exceptions==null) {
				exceptions = new ArrayList<>();
			}

			CollectionUtils.feedItems(exceptions, ex);
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

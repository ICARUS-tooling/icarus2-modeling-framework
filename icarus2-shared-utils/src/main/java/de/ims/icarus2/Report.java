/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.util.id.Identity;

/**
 *
 * @author Markus Gärtner
 *
 * @param <R>
 */
public interface Report<R extends ReportItem> {

	/**
	 *
	 * @return source for which this report has been created. Can be {@code null}.
	 */
	Identity getSource();

    /**
     * @return number of items with severity {@link Severity#ERROR} in this report
     */
    int countErrors();

    default boolean hasErrors() {
    	return countErrors()>0;
    }

    /**
     * @return number of items with severity {@link Severity#WARNING} in this report
     */
    int countWarnings();

    default boolean hasWarnings() {
    	return countWarnings()>0;
    }

    /**
     * @return collection of {@link ReportItem} objects
     */
    Collection<R> getItems();

    default boolean isEmpty() {
    	return getItems().isEmpty();
    }

    /**
     * @author Markus Gärtner
     *
     */
    @FunctionalInterface
    public interface ReportItemCollector {

    	/**
    	 * Adds a new report item.
    	 * <p>
    	 * This is the main collection method, all other predefined
    	 * methods in this interface delegate to this method with
    	 * their respective fixed values for {@link Severity} etc...
    	 *
    	 * @param severity
    	 * @param source
    	 * @param code
    	 * @param message
    	 * @param data
    	 */
    	void addItem(Severity severity, Identity source, ErrorCode code, String message, Object...data);

    	default void addItem(Severity severity, ErrorCode code, String message) {
    		addItem(severity, null, code, message, (Object[])null);
    	}

    	default void addError(ErrorCode code, String message) {
    		addItem(Severity.ERROR, null, code, message, (Object[])null);
    	}

    	default void addError(ErrorCode code, String message, Object...data) {
    		addItem(Severity.ERROR, null, code, message, data);
    	}

    	default void addWarning(ErrorCode code, String message) {
    		addItem(Severity.WARNING, null, code, message, (Object[])null);
    	}

    	default void addWarning(ErrorCode code, String message, Object...data) {
    		addItem(Severity.WARNING, null, code, message, data);
    	}

    	default void addInfo(ErrorCode code, String message) {
    		addItem(Severity.INFO, null, code, message, (Object[])null);
    	}

    	default void addInfo(ErrorCode code, String message, Object...data) {
    		addItem(Severity.INFO, null, code, message, data);
    	}

    	default void addInfo(String message) {
    		addItem(Severity.INFO, null, GlobalErrorCode.INFO, message, (Object[])null);
    	}

    	default void addInfo(String message, Object...data) {
    		addItem(Severity.INFO, null, GlobalErrorCode.INFO, message, data);
    	}
    }

    /**
     * Single report element. Holds all information about particular event.
     *
     * @author Markus Gärtner
     */
    public interface ReportItem {
        /**
         * @return severity code for this report item
         */
        Severity getSeverity();

        /**
         * @return source for this report item, can be <code>null</code>
         */
        Identity getSource();

        /**
         * @return error code for this report item
         */
        ErrorCode getCode();

        /**
         * @return message, associated with this report item for the system
         *         default locale
         */
        String getMessage();

        /**
         * Returns the local date and time at which the report item has been originally created
         * @return
         */
        LocalDateTime getTimestamp();

        /**
         * @param locale locale to get message for
         * @return message, associated with this report item for given locale
         */
        String getMessage(Locale locale);

        default void appendTo(StringBuilder sb) {
        	sb.append(getTimestamp()).append(" [").append(getSeverity()).append('-').append(getCode()).append(']');

        	Identity identity = getSource();
        	if(identity!=null) {
        		sb.append(' ').append(identity.getName());
        	}

        	sb.append(' ').append(getMessage());
        }
    }

	/**
	 *
     * Report item severity constants.
	 *
	 * @author Markus Gärtner
	 *
	 */
    public enum Severity {
        /**
         * Report item severity constant for severe failure events.
         */
        ERROR,

        /**
         * Report item severity constant for warning events.
         */
        WARNING,

        /**
         * Report item severity constant for informative or debugging events.
         */
        INFO
    }

    default String toString(String title) {
		StringBuilder sb = new StringBuilder();
		appendTo(sb, title);
		return sb.toString();
    }

    default void appendTo(StringBuilder sb, String title) {
    	sb.append("======[errors: ").append(countErrors())
    	.append(" - warnings: ").append(countWarnings()).append("]======").append('\n');

    	if(title!=null) {
    		sb.append(title).append('\n').append('\n');
    	}

    	for(R item : getItems()) {
    		item.appendTo(sb);
    		sb.append('\n');
    	}

    	sb.append("==========================================");
    }

	public static final Report<ReportItem> EMPTY_REPORT = new EmptyReport<>();

	/**
	 * Type-safe alternative to the public field {@link #EMPTY_REPORT}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <R extends ReportItem> Report<R> emptyReport() {
		return (Report<R>) EMPTY_REPORT;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <R>
	 */
	public static class EmptyReport<R extends ReportItem> implements Report<R> {

		/**
		 * @see de.ims.icarus2.Report#getSource()
		 */
		@Override
		public Identity getSource() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.Report#countErrors()
		 */
		@Override
		public int countErrors() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.Report#countWarnings()
		 */
		@Override
		public int countWarnings() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.Report#getItems()
		 */
		@Override
		public Collection<R> getItems() {
			return Collections.emptyList();
		}

	}
}
